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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.facet.MetadataAware
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.AbstractDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.RemoteDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.CalculationStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.calculation.PostgresCalculationStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.calculation.PostgresSamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.parameters.PostgresDatabaseDataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.query.PostgresQueryStringProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.query.QueryStringProvider

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

import java.sql.Connection
import java.sql.PreparedStatement

@CompileStatic
class PostgresDatabaseDataModelImporterProviderService
    extends AbstractDatabaseDataModelImporterProviderService<PostgresDatabaseDataModelImporterProviderServiceParameters>
    implements RemoteDatabaseDataModelImporterProviderService {

    @Autowired
    PostgresDataTypeProviderService postgresDataTypeProviderService

    @Override
    String getDisplayName() {
        'PostgreSQL Importer'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    String namespaceColumn() {
        "uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.column"
    }

    @Override
    String namespaceTable() {
        "uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.table"
    }

    @Override
    String namespaceSchema() {
        "uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.schema"
    }

    @Override
    String namespaceDatabase() {
        "uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres"
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
        false
    }

    @Override
    DefaultDataTypeProvider getDefaultDataTypeProvider() {
        postgresDataTypeProviderService
    }

    @Override
    QueryStringProvider createQueryStringProvider() {
        return new PostgresQueryStringProvider()
    }

    @Override
    SamplingStrategy createSamplingStrategy(String schema, String table, PostgresDatabaseDataModelImporterProviderServiceParameters parameters) {
        new PostgresSamplingStrategy(schema, table, parameters)
    }

    @Override
    CalculationStrategy createCalculationStrategy(PostgresDatabaseDataModelImporterProviderServiceParameters parameters) {
        new PostgresCalculationStrategy(parameters)
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
        addComment(connection, databaseQuery, dataModel, dataModel.createdBy, namespaceDatabase())

        dataModel.childDataClasses.each { DataClass schemaClass ->
            //Get comment for the schema
            String schemaQuery = """
            SELECT obj_description('${schemaClass.label}'::regnamespace, 'pg_namespace') AS "COMMENT"
            """
            addComment(connection, schemaQuery, schemaClass, dataModel.createdBy, namespaceSchema())

            schemaClass.dataClasses.each { DataClass tableClass ->
                String tableQuery = """
                SELECT pg_catalog.obj_description('${schemaClass.label}.${tableClass.label}'::regclass, 'pg_class') AS "COMMENT"
                """
                addComment(connection, tableQuery, tableClass, dataModel.createdBy, namespaceTable())
                tableClass.dataElements.each {DataElement column ->
                    Metadata ordinalPosition = column.getMetadata().find {
                        it.key == 'ordinal_position'
                    }
                    if (ordinalPosition) {
                        String columnQuery = """
                        SELECT pg_catalog.col_description('${schemaClass.label}.${tableClass.label}'::regclass, ${ordinalPosition.value}) AS "COMMENT"
                        """
                        addComment(connection, columnQuery, column, dataModel.createdBy, namespaceColumn())
                    }
                }
            }
        }
    }

    private void addComment(Connection connection, String query, MetadataAware ma, String createdBy, String metadataNamespace) {
        final PreparedStatement preparedStatement = connection.prepareStatement(query)
        final List<Map<String, Object>> results = executeStatement(preparedStatement)

        if (results && results[0].comment) {
            ma.addToMetadata(metadataNamespace, 'COMMENT', results[0].comment as String, createdBy)
        }
    }
}
