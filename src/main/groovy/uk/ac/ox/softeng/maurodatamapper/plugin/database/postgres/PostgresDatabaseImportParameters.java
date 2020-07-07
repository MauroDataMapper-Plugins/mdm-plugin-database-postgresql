package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres;

import ox.softeng.metadatacatalogue.core.spi.importer.parameter.config.ImportGroupConfig;
import ox.softeng.metadatacatalogue.core.spi.importer.parameter.config.ImportParameterConfig;
import ox.softeng.metadatacatalogue.plugins.database.DatabaseImportParameters;

import org.postgresql.ds.PGSimpleDataSource;

import java.util.Properties;

/**
 * Created by james on 31/05/2017.
 */
public class PostgresDatabaseImportParameters extends DatabaseImportParameters<PGSimpleDataSource> {

    @Override
    public int getDefaultPort() {
        return 5432;
    }

    @Override
    public String getDatabaseDialect() {
        return "Postgres";
    }

    @Override
    public String getUrl(String databaseName) {
        return getDataSource(databaseName).getUrl();
    }

    @Override
    public PGSimpleDataSource getDataSource(String databaseName) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(getDatabaseHost());
        dataSource.setPortNumber(getDatabasePort());
        dataSource.setDatabaseName(databaseName);

        if (getDatabaseSSL()) {
            dataSource.setSsl(true);
            dataSource.setSslMode("require");
        }

        getLogger().info("DataSource connection url: {}", dataSource.getUrl());

        return dataSource;
    }

    @ImportParameterConfig(
        displayName = "Database Schema/s",
        description = "A comma-separated list of the schema names to import. If not supplied then all schemas other than 'pg_catalog' and " +
                      "'information_schema' will be imported.",
        optional = true,
        group = @ImportGroupConfig(
            name = "Database",
            order = 1
        )
    )
    private String schemaNames;

    public String getSchemaNames() {
        return schemaNames;
    }

    public void setSchemaNames(String schemaNames) {
        this.schemaNames = schemaNames;
    }

    @Override
    public void populateFromProperties(Properties properties) {
        super.populateFromProperties(properties);
        schemaNames = properties.getProperty("import.database.schemas");
    }
}
