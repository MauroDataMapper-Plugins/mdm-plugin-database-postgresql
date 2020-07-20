package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportGroupConfig
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportParameterConfig
import uk.ac.ox.softeng.maurodatamapper.plugin.database.DatabaseDataModelImporterProviderServiceParameters

import org.postgresql.ds.PGSimpleDataSource

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class PostgresDatabaseDataModelImporterProviderServiceParameters extends DatabaseDataModelImporterProviderServiceParameters<PGSimpleDataSource> {

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
    PGSimpleDataSource getDataSource(String databaseName) {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource().tap {
            setServerNames databaseHost as String[]
            setPortNumbers databasePort as int[]
            setDatabaseName databaseName
            if (databaseSSL) {
                setSsl true
                setSslMode 'require'
            }
        }
        log.info 'DataSource connection url: {}', dataSource.getUrl()
        dataSource
    }

    @Override
    String getUrl(String databaseName) {
        getDataSource(databaseName).getUrl()
    }

    @Override
    String getDatabaseDialect() {
        'Postgres'
    }

    @Override
    int getDefaultPort() {
        5432
    }
}
