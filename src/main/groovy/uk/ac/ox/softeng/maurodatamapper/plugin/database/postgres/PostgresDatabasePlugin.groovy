package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.provider.plugin.AbstractMauroDataMapperPlugin

import groovy.transform.CompileDynamic

// @CompileStatic
@CompileDynamic
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
