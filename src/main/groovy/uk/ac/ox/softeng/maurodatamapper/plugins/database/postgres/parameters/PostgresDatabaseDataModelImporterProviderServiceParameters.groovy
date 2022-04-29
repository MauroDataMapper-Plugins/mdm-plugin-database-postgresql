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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.parameters

import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportGroupConfig
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportParameterConfig
import uk.ac.ox.softeng.maurodatamapper.plugins.database.DatabaseDataModelWithSamplingImporterProviderServiceParameters

import groovy.util.logging.Slf4j
import org.postgresql.ds.PGSimpleDataSource

@Slf4j
// @CompileStatic
class PostgresDatabaseDataModelImporterProviderServiceParameters extends
        DatabaseDataModelWithSamplingImporterProviderServiceParameters<PGSimpleDataSource> {

    @ImportParameterConfig(
        displayName = 'Database Schema(s)',
        description = [
            'A comma-separated list of the schema names to import.',
            'If not supplied then all schemas other than "pg_catalog" and "information_schema" will be imported.'],
        optional = true,
        group = @ImportGroupConfig(
            name = 'Database',
            order = 1
        ))
    String schemaNames

    @Override
    void populateFromProperties(Properties properties) {
        super.populateFromProperties properties
        schemaNames = properties.getProperty('import.database.schemas')
    }

    @Override
    PGSimpleDataSource getDataSource(String dbName) {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource().tap {
            // Need to use setters here as they handle empty/null elements for us
            setServerNames getDatabaseServerNames()
            setPortNumbers getDatabasePortNumbers()
            setDatabaseName dbName
            if (databaseSSL) {
                setSsl true
                setSslMode 'require'
            }
        }
        log.info 'DataSource connection url: {}', dataSource.url
        dataSource
    }

    @Override
    String getUrl(String databaseName) {
        getDataSource(databaseName).url
    }

    @Override
    String getDatabaseDialect() {
        'Postgres'
    }

    @Override
    int getDefaultPort() {
        5432
    }

    String[] getDatabaseServerNames() {
        [databaseHost].toArray() as String[]
    }

    int[] getDatabasePortNumbers() {
        [databasePort].toArray() as int[]
    }
}
