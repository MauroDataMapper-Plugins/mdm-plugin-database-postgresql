package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportGroupConfig
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportParameterConfig
import uk.ac.ox.softeng.maurodatamapper.plugin.database.DatabaseDataModelImporterProviderServiceParameters

import groovy.util.logging.Slf4j
import org.postgresql.ds.PGSimpleDataSource

/**
 * Created by james on 31/05/2017.
 */
@Slf4j
class PostgresDatabaseDataModelImporterProviderServiceParameters
    extends DatabaseDataModelImporterProviderServiceParameters<PGSimpleDataSource> {

    @Override
    int getDefaultPort() {
        return 5432
    }

    @Override
    String getDatabaseDialect() {
        return "Postgres"
    }

    @Override
    String getUrl(String databaseName) {
        return getDataSource(databaseName).getUrl()
    }

    @Override
    PGSimpleDataSource getDataSource(String databaseName) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource()
        dataSource.setServerName(getDatabaseHost()) //TODO @adei please find the non-deprecated methods to use
        dataSource.setPortNumber(getDatabasePort())
        dataSource.setDatabaseName(databaseName)

        if (getDatabaseSSL()) {
            dataSource.setSsl(true)
            dataSource.setSslMode("require")
        }

        log.info("DataSource connection url: {}", dataSource.getUrl())

        return dataSource
    }

    @ImportParameterConfig(
        displayName = "Database Schema/s",
        description = ['A comma-separated list of the schema names to import. ',
            'If not supplied then all schemas other than \'pg_catalog\' and \'information_schema\' will be imported.'],
        optional = true,
        group = @ImportGroupConfig(
            name = "Database",
            order = 1
        )
    )
    private String schemaNames

    String getSchemaNames() {
        return schemaNames
    }

    void setSchemaNames(String schemaNames) {
        this.schemaNames = schemaNames
    }

    @Override
    void populateFromProperties(Properties properties) {
        super.populateFromProperties(properties)
        schemaNames = properties.getProperty("import.database.schemas")
    }
}
