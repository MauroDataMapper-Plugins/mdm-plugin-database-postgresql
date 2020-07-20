package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.provider.plugin.AbstractMauroDataMapperPlugin

// @CompileStatic
class PostgresDatabasePlugin extends AbstractMauroDataMapperPlugin {

    @Override
    String getName() {
        'Plugin : Database - Postgres'
    }

    @Override
    Closure doWithSpring() {
        { ->
            postgresDatabaseDataModelImporterProviderService PostgresDatabaseDataModelImporterProviderService
            postgresDataTypeProvider PostgresDataTypeProvider
        }
    }
}
