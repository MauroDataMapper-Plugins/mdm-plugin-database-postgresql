package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.provider.plugin.AbstractMauroDataMapperPlugin

/**
 * @since 17/08/2017
 */
class PostgresDatabasePlugin extends AbstractMauroDataMapperPlugin {
    @Override
    String getName() {
        return "Plugin:Database - Postgres"
    }

    @Override
    Closure doWithSpring() {
        {->
            postgresDatabaseDataModelImporterProviderService(PostgresDatabaseDataModelImporterProviderService)
            postgresDefaultDatatypeProvider(PostgresDefaultDatatypeProvider)
        }
    }
}
