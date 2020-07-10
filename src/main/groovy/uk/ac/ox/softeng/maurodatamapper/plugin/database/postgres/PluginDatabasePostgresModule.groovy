package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

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
