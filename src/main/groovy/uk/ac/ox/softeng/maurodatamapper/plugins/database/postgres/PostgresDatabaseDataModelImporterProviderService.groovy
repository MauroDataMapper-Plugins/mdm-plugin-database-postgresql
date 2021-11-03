/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.facet.MetadataAware
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.AbstractDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.RemoteDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.summarymetadata.AbstractIntervalHelper

import org.springframework.beans.factory.annotation.Autowired

import java.sql.Connection
import java.sql.PreparedStatement
import java.time.format.DateTimeFormatter

// @CompileStatic
class PostgresDatabaseDataModelImporterProviderService
    extends AbstractDatabaseDataModelImporterProviderService<PostgresDatabaseDataModelImporterProviderServiceParameters>
    implements RemoteDatabaseDataModelImporterProviderService {

    @Autowired
    PostgresDataTypeProvider postgresDataTypeProvider

    @Override
    SamplingStrategy getSamplingStrategy(PostgresDatabaseDataModelImporterProviderServiceParameters parameters) {
        new PostgresSamplingStrategy(parameters.sampleThreshold ?: DEFAULT_SAMPLE_THRESHOLD, parameters.samplePercent ?: DEFAULT_SAMPLE_PERCENTAGE)
    }

    @Override
    String getDisplayName() {
        'PostgreSQL Importer'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    Set<String> getKnownMetadataKeys() {
        ['character_maximum_length', 'character_octet_length', 'character_set_catalog', 'character_set_name', 'character_set_schema',
         'collation_catalog', 'collation_name', 'collation_schema', 'column_default', 'datetime_precision', 'domain_catalog', 'domain_name',
         'domain_schema', 'dtd_identifier', 'generation_expression', 'identity_cycle', 'identity_generation', 'identity_increment',
         'identity_maximum', 'identity_minimum', 'identity_start', 'interval_precision', 'interval_type', 'is_generated', 'is_identity',
         'is_nullable', 'is_self_referencing', 'is_updatable', 'maximum_cardinality', 'numeric_precision_radix', 'numeric_precision',
         'numeric_scale', 'ordinal_position', 'scope_catalog', 'scope_name', 'scope_schema', 'udt_catalog', 'udt_name', 'udt_schema',
         'primary_index[]', 'unique_index[]', 'index[]', 'unique[]', 'primary_key[]', 'foreign_key[]'] as Set<String>
    }

    @Override
    Boolean allowsExtraMetadataKeys() {
        true
    }

    @Override
    DefaultDataTypeProvider getDefaultDataTypeProvider() {
        postgresDataTypeProvider
    }

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
        String query = "SELECT reltuples::bigint AS approx_count FROM pg_class WHERE oid = '${oid}'::regclass"

        queryStrings.push(query.toString())
        queryStrings
    }

    @Override
    boolean isColumnPossibleEnumeration(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && (dataType.label == "character" || dataType.label == "character varying")
    }

    @Override
    boolean isColumnForDateSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["date", "timestamp without time zone", "timestamp with time zone"].contains(dataType.label)
    }

    @Override
    boolean isColumnForDecimalSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["decimal", "numeric"].contains(dataType.label)
    }

    @Override
    boolean isColumnForIntegerSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["smallint", "integer", "bigint"].contains(dataType.label)
    }

    String columnRangeDistributionQueryString(DataType dataType,
                                              AbstractIntervalHelper intervalHelper,
                                              String columnName, String tableName, String schemaName) {
        SamplingStrategy samplingStrategy = new SamplingStrategy()
        columnRangeDistributionQueryString(samplingStrategy, dataType, intervalHelper, columnName, tableName, schemaName)
    }

    @Override
    String columnRangeDistributionQueryString(SamplingStrategy samplingStrategy, DataType dataType, AbstractIntervalHelper intervalHelper, String columnName, String tableName, String schemaName) {
        List<String> selects = intervalHelper.intervals.collect {
            "SELECT '${it.key}' AS interval_label, ${formatDataType(dataType, it.value.aValue)} AS interval_start, ${formatDataType(dataType, it.value.bValue)} AS interval_end"
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
        if (isColumnForDateSummary(dataType)){
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

    @Override
    PreparedStatement prepareCoreStatement(Connection connection, PostgresDatabaseDataModelImporterProviderServiceParameters parameters) {
        if (!parameters.schemaNames) return super.prepareCoreStatement(connection, parameters)
        final List<String> names = parameters.schemaNames.split(',') as List<String>
        final PreparedStatement statement = connection.prepareStatement(
            """SELECT * FROM information_schema.columns WHERE table_schema IN (${names.collect {'?'}.join(',')});""")
        names.eachWithIndex {String name, int i -> statement.setString(i + 1, name)}
        statement
    }

    /**
     * Use PostgreSQL functions to get comments. There can only be one comment per object.
     * See https://www.postgresql.org/docs/13/functions-info.html#FUNCTIONS-INFO-COMMENT-TABLE
     * @param dataModel
     * @param connection
     */
    @Override
    void addMetadata(DataModel dataModel, Connection connection) {
        //Get comment for the database
        String databaseQuery = """
        SELECT pg_catalog.shobj_description(d.oid, 'pg_database') AS "COMMENT"
        FROM pg_catalog.pg_database d
        WHERE datname = '${dataModel.label}'
        """
        addComment(connection, databaseQuery, dataModel, dataModel.createdBy)

        dataModel.childDataClasses.each { DataClass schemaClass ->
            //Get comment for the schema
            String schemaQuery = """
            SELECT obj_description('${schemaClass.label}'::regnamespace, 'pg_namespace');
            """
            addComment(connection, schemaQuery, schemaClass, dataModel.createdBy)

            schemaClass.dataClasses.each { DataClass tableClass ->
                String tableQuery = """
                SELECT pg_catalog.obj_description('${schemaClass.label}.${tableClass.label}'::regclass, 'pg_class') AS "COMMENT"
                """
                addComment(connection, tableQuery, tableClass, dataModel.createdBy)
                tableClass.dataElements.each {DataElement column ->
                    Metadata ordinalPosition = column.getMetadata().find {
                        it.key == 'ordinal_position'
                    }
                    if (ordinalPosition) {
                        String columnQuery = """
                        SELECT pg_catalog.col_description('${schemaClass.label}.${tableClass.label}'::regclass, ${ordinalPosition.value}) AS "COMMENT"
                        """
                        addComment(connection, columnQuery, column, dataModel.createdBy)
                    }
                }
            }
        }
    }

    private void addComment(Connection connection, String query, MetadataAware ma, String createdBy) {
        final PreparedStatement preparedStatement = connection.prepareStatement(query)
        final List<Map<String, Object>> results = executeStatement(preparedStatement)

        if (results && results[0].comment) {
            ma.addToMetadata(namespace, 'COMMENT', results[0].comment as String, createdBy)
        }
    }
}
