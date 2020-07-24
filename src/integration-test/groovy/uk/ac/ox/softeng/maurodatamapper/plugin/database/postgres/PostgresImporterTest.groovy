package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.plugin.testing.utils.BaseDatabasePluginTest

import org.junit.Test

import groovy.transform.CompileStatic

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@CompileStatic
class PostgresImporterTest extends BaseDatabasePluginTest<PostgresDatabaseDataModelImporterProviderServiceParameters,
        PostgresDatabaseDataModelImporterProviderService> {

    @Override
    String getDatabasePortPropertyName() {
        'jdbc.port'
    }

    @Override
    int getDefaultDatabasePort() {
        5432
    }

    @Override
    PostgresDatabaseDataModelImporterProviderServiceParameters createDatabaseImportParameters() {
        new PostgresDatabaseDataModelImporterProviderServiceParameters().tap {
            setDatabaseNames 'maurodatamapper_test'
            setDatabaseUsername 'maurodatamapper'
            setDatabasePassword 'MauroDataMapper1234'
        }
    }

    @Test
    void testImportSimpleDatabase() {
        DataModel dataModel = importDataModelAndRetrieveFromDatabase(
                createDatabaseImportParameters(databaseHost, databasePort).tap { setDatabaseNames 'metadata_simple' })
        assertEquals 'Database/Model name', 'metadata_simple', dataModel.getLabel()
        assertEquals 'Number of columntypes/datatypes', 10, dataModel.getDataTypes()?.size()
        assertEquals 'Number of primitive types', 8, dataModel.getDataTypes().findAll { it.domainType == 'PrimitiveType' }.size()
        assertEquals 'Number of reference types', 2, dataModel.getDataTypes().findAll { it.domainType == 'ReferenceType' }.size()
        assertEquals 'Number of tables/dataclasses', 4, dataModel.getDataClasses()?.size()
        assertEquals 'Number of child tables/dataclasses', 1, dataModel.getChildDataClasses()?.size()

        DataClass publicSchema = dataModel.getChildDataClasses().first()
        assertEquals 'Number of child tables/dataclasses', 3, publicSchema.getDataClasses()?.size()

        Set<DataClass> dataClasses = publicSchema.dataClasses

        // Tables
        DataClass metadataTable = dataClasses.find { it.label == 'metadata' }
        assertEquals 'Metadata Number of columns/dataElements', 10, metadataTable.dataElements.size()
        assertEquals 'Metadata Number of metadata', 5, metadataTable.metadata.size()
        assertTrue 'MD All metadata values are valid', metadataTable.metadata.every { it.value && it.key != it.value }
        assertEquals 'MD Primary key', 1, metadataTable.metadata.count { it.key.startsWith 'primary_key' }
        assertEquals 'MD Primary indexes', 1, metadataTable.metadata.count { it.key.startsWith 'primary_index' }
        assertEquals 'MD Unique indexes', 1, metadataTable.metadata.count { it.key.startsWith 'unique_index' }
        assertEquals 'MD Indexes', 2, metadataTable.metadata.count { it.key.startsWith 'index' }

        Metadata multipleColIndex = metadataTable.metadata.find { it.key.contains 'unique_item_id_namespace_key' }
        assertNotNull 'Should have multi column index', multipleColIndex
        assertEquals 'Correct order of columns', 'catalogue_item_id, namespace, key', multipleColIndex.value

        DataClass ciTable = dataClasses.find { it.label == 'catalogue_item' }
        assertEquals 'CI Number of columns/dataElements', 10, ciTable.dataElements.size()
        assertEquals 'CI Number of metadata', 4, ciTable.metadata.size()
        assertTrue 'CI All metadata values are valid', ciTable.metadata.every { it.value && it.key != it.value }
        assertEquals 'Primary key', 1, ciTable.metadata.count { it.key.startsWith 'primary_key' }
        assertEquals 'Primary indexes', 1, ciTable.metadata.count { it.key.startsWith 'primary_index' }
        assertEquals 'Indexes', 2, ciTable.metadata.count { it.key.startsWith 'index' }

        DataClass cuTable = dataClasses.find { it.label == 'catalogue_user' }
        assertEquals 'CU Number of columns/dataElements', 18, cuTable.dataElements.size()
        assertEquals 'CU Number of metadata', 5, cuTable.metadata.size()
        assertTrue 'CU All metadata values are valid', cuTable.metadata.every { it.value && it.key != it.value }
        assertEquals 'Primary key', 1, cuTable.metadata.count { it.key.startsWith 'primary_key' }
        assertEquals 'Primary indexes', 1, cuTable.metadata.count { it.key.startsWith 'primary_index' }
        assertEquals 'Unique indexes', 1, cuTable.metadata.count { it.key.startsWith 'unique_index' }
        assertEquals 'Indexes', 1, cuTable.metadata.count { it.key.startsWith 'index' }
        assertEquals 'Unique Constraint', 1, cuTable.metadata.count { it.key.startsWith 'unique[' }

        // Columns
        assertTrue 'Metadata all elements required', metadataTable.dataElements.every { it.minMultiplicity == 1 }
        assertEquals 'CI mandatory elements', 9, ciTable.dataElements.count { it.minMultiplicity == 1 }
        assertEquals 'CI optional element description', 0, ciTable.findDataElement('description').minMultiplicity
        assertEquals 'CU mandatory elements', 10, cuTable.dataElements.count { it.minMultiplicity == 1 }
    }
}