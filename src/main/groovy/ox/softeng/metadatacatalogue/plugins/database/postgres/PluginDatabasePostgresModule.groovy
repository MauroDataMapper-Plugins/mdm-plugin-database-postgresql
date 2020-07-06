package ox.softeng.metadatacatalogue.plugins.database.postgres

import ox.softeng.metadatacatalogue.core.spi.module.AbstractModule

/**
 * @since 17/08/2017
 */
class PluginDatabasePostgresModule extends AbstractModule {
    @Override
    String getName() {
        return "Plugin:Database - Postgres"
    }

    @Override
    Closure doWithSpring() {
        {->
            postgresDatabaseImporterService(PostgresDatabaseImporterService)
            postgresDefaultDatatypeProvider(PostgresDefaultDatatypeProvider)
        }
    }
}