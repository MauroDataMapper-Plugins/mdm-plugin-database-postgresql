/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.query

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.query.QueryStringProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.summarymetadata.AbstractIntervalHelper

import java.time.format.DateTimeFormatter

/**
 * @since 11/03/2022
 */
class PostgresQueryStringProvider extends QueryStringProvider {

    @Override
    String getIndexInformationQueryString() {
        '''
        SELECT
          table_name,
          index_name,
          unique_index,
          primary_index,
          clustered,
          array_to_string(array_agg(column_name), ', ') AS column_names
        FROM (
               SELECT
                 t.relname         AS table_name,
                 i.relname         AS index_name,
                 a.attname         AS column_name,
                 ix.indisunique    AS unique_index,
                 ix.indisprimary   AS primary_index,
                 ix.indisclustered AS clustered,
                 unnest(ix.indkey) AS unn,
                 a.attnum
               FROM pg_catalog.pg_index ix
                 LEFT JOIN pg_catalog.pg_class t ON ix.indrelid = t.oid
                 LEFT JOIN pg_catalog.pg_class i ON ix.indexrelid = i.oid
                 LEFT JOIN pg_catalog.pg_attribute a ON (t.oid = a.attrelid AND a.attnum = ANY (ix.indkey))
                 LEFT JOIN pg_catalog.pg_namespace ns ON t.relnamespace = ns.oid
               WHERE ns.nspname = ?
               ORDER BY
                 t.relname,
                 i.relname,
                 generate_subscripts(ix.indkey, 1)) sb
        WHERE unn = attnum
        GROUP BY table_name, index_name, unique_index, primary_index, clustered;
        '''.stripIndent()
    }

    @Override
    String getForeignKeyInformationQueryString() {
        '''
        SELECT
          tc.constraint_name,
          tc.table_name,
          kcu.column_name,
          ccu.table_name  AS reference_table_name,
          ccu.column_name AS reference_column_name
        FROM
          information_schema.table_constraints AS tc
          JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
          JOIN information_schema.constraint_column_usage AS ccu
            ON ccu.constraint_name = tc.constraint_name
        WHERE constraint_type = 'FOREIGN KEY' AND tc.constraint_schema = ?;
        '''.stripIndent()
    }

    @Override
    String getDatabaseStructureQueryString() {
        '''
        SELECT *
        FROM information_schema.columns
        WHERE table_schema NOT IN ('pg_catalog','information_schema');
        '''.stripIndent()
    }

    /**
     * PostgreSQL identifiers escaped in double quotes.
     */
    @Override
    String escapeIdentifier(String identifier) {
        "\"${identifier}\""
    }

    /**
     * Return a query that will select an approximate row count from the specified table.
     * See https://wiki.postgresql.org/wiki/Count_estimate
     *
     * @param tableName
     * @param schemaName
     * @return
     */
    @Override
    List<String> approxCountQueryString(String tableName, String schemaName = null) {
        List<String> queryStrings = super.approxCountQueryString(tableName, schemaName)
        String oid = tableName
        if (schemaName) {
            oid = schemaName + '.' + oid
        }
        // If analyze or vacuum havent been run this query will return 0 which is not helpful
        // A standard full count(*) in PG is fast, and if there really are 0 rows then the second query will be just as fast
        // By returning null we force the second/default query to run
        String query = """WITH fast AS (
    SELECT reltuples::BIGINT AS approx_count
    FROM pg_class
    WHERE oid = TO_REGCLASS('${oid}')
)
SELECT CASE
           WHEN approx_count > 0
               THEN approx_count
       END as approx_count
FROM fast"""

        queryStrings.push(query.toString())
        queryStrings
    }

    @Override
    String columnRangeDistributionQueryString(SamplingStrategy samplingStrategy, DataType dataType, AbstractIntervalHelper intervalHelper, String columnName, String tableName,
                                              String schemaName) {
        List<String> selects = intervalHelper.intervals.collect {
            "SELECT '${it.key}' AS interval_label, ${formatDataType(dataType, it.value.aValue)} AS interval_start, ${formatDataType(dataType, it.value.bValue)} AS " +
            "interval_end"
        }

        rangeDistributionQueryString(samplingStrategy, selects, columnName, tableName, schemaName)
    }

    /**
     * Return a string which uses the PostgreSQL TO_TIMESTAMP function for Dates, otherwise string formatting
     *
     * @param dataType
     * @param value
     * @return Fragment of query string, either TO_TIMESTAMP or value
     */
    String formatDataType(DataType dataType, Object value) {
        if (isColumnForDateSummary(dataType)) {
            "TO_TIMESTAMP('${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)}', 'YYYY-MM-DDTHH:MI:SS')"
        } else {
            "${value}"
        }
    }

    /**
     * Returns a String that looks, for example, like this:
     * WITH interval AS (
     *   SELECT '0 - 100' AS interval_label, 0 AS interval_start, 100 AS interval_end
     *   UNION
     *   SELECT '100 - 200' AS interval_label, 100 AS interval_start, 200 AS interval_end
     * )
     * SELECT interval_label, COUNT("my_column") AS interval_count
     * FROM interval
     * LEFT JOIN
     * "my_schema"."my_table" ON "my_schema"."my_table"."my_column" >= #interval.interval_start AND "my_schema"."my_table"."my_column" < #interval.interval_end
     * GROUP BY interval_label, interval_start
     * ORDER BY interval_start ASC;
     *
     * @param schemaName
     * @param tableName
     * @param columnName
     * @param selects
     * @return
     */
    private String rangeDistributionQueryString(SamplingStrategy samplingStrategy, List<String> selects, String columnName, String tableName, String schemaName) {
        String intervals = selects.join(" UNION ")

        String sql = "WITH interval AS (${intervals})" +
                     """
        SELECT interval_label, ${samplingStrategy.scaleFactor()} * COUNT(${escapeIdentifier(columnName)}) AS interval_count
        FROM interval
        LEFT JOIN
        ${escapeIdentifier(schemaName)}.${escapeIdentifier(tableName)} 
        ${samplingStrategy.samplingClause()}
        ON ${escapeIdentifier(schemaName)}.${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)}  >= interval.interval_start 
        AND ${escapeIdentifier(schemaName)}.${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)} < interval.interval_end
        GROUP BY interval_label, interval_start
        ORDER BY interval_start ASC;
        """

        sql.stripIndent()
    }

    boolean isColumnForDateSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["date", "timestamp without time zone", "timestamp with time zone"].contains(dataType.label)
    }
}
