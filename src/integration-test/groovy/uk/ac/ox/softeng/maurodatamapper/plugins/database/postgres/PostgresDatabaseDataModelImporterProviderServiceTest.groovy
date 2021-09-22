/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.plugins.testing.utils.BaseDatabasePluginTest

import groovy.json.JsonSlurper
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

// @CompileStatic
class PostgresDatabaseDataModelImporterProviderServiceTest extends BaseDatabasePluginTest<PostgresDatabaseDataModelImporterProviderServiceParameters,
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
            databaseNames = 'maurodatamapper_test'
            databaseUsername = 'maurodatamapper'
            databasePassword = 'MauroDataMapper1234'
        }
    }

    @Test
    void testImportSimpleDatabase() {
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
            createDatabaseImportParameters(databaseHost, databasePort).tap {databaseNames = 'metadata_simple'})

        checkBasic(dataModel)
        checkOrganisationNotEnumerated(dataModel)
        checkSampleNoSummaryMetadata(dataModel)
        checkBiggerSampleNoSummaryMetadata(dataModel)

        assertEquals 'Number of columntypes/datatypes', 15, dataModel.dataTypes?.size()
        assertEquals 'Number of primitive types', 13, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType'}.size()
        assertEquals 'Number of reference types', 2, dataModel.dataTypes.findAll {it.domainType == 'ReferenceType'}.size()
        assertEquals 'Number of enumeration types', 0, dataModel.dataTypes.findAll {it.domainType == 'EnumerationType'}.size()
        assertEquals 'Number of char datatypes', 1, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType' && it.label == 'character'}.size()
    }

    @Test
    void testImportSimpleDatabaseWithEnumerations() {
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
                createDatabaseImportParameters(databaseHost, databasePort).tap {
                    databaseNames = 'metadata_simple';
                    detectEnumerations = true;
                    maxEnumerations = 20})

        checkBasic(dataModel)
        checkOrganisationEnumerated(dataModel)
        checkSampleNoSummaryMetadata(dataModel)
        checkBiggerSampleNoSummaryMetadata(dataModel)

        assertEquals 'Number of columntypes/datatypes', 18, dataModel.dataTypes?.size()
        assertEquals 'Number of primitive types', 12, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType'}.size()
        assertEquals 'Number of reference types', 2, dataModel.dataTypes.findAll {it.domainType == 'ReferenceType'}.size()
        assertEquals 'Number of enumeration types', 4, dataModel.dataTypes.findAll {it.domainType == 'EnumerationType'}.size()
        assertEquals 'Number of char datatypes', 0, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType' && it.label == 'character'}.size()

    }

    @Test
    void 'testImportSimpleDatabaseWithSummaryMetadata'() {
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
                createDatabaseImportParameters(databaseHost, databasePort).tap {
                    databaseNames = 'metadata_simple';
                    detectEnumerations = true;
                    maxEnumerations = 20;
                    calculateSummaryMetadata = true;
                })

        checkBasic(dataModel)
        checkOrganisationEnumerated(dataModel)
        checkSampleSummaryMetadata(dataModel)
        checkBiggerSampleSummaryMetadata(dataModel)
    }

    @Test
    void 'testImportSimpleDatabaseWithSummaryMetadataWithSampling'() {
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
            createDatabaseImportParameters(databaseHost, databasePort).tap {
                databaseNames = 'metadata_simple'
                detectEnumerations = true
                maxEnumerations = 20
                calculateSummaryMetadata = true
                sampleThreshold = 1000
                samplePercent = 10
            }
        )

        checkBasic(dataModel)
        checkOrganisationEnumerated(dataModel)
        checkSampleSummaryMetadata(dataModel)

        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'bigger_sample'}

        assertEquals 'Sample Number of columns/dataElements', 4, sampleTable.dataElements.size()

        final DataElement sample_bigint = sampleTable.dataElements.find{it.label == "sample_bigint"}
        assertEquals 'description of summary metadata for sample_bigint',
                'Estimated Value Distribution (calculated by sampling 10% of rows)',
                sample_bigint.summaryMetadata[0].description

        final DataElement sample_decimal = sampleTable.dataElements.find{it.label == "sample_decimal"}
        assertEquals 'description of summary metadata for sample_decimal',
                'Estimated Value Distribution (calculated by sampling 10% of rows)',
                sample_decimal.summaryMetadata[0].description

        final DataElement sample_date = sampleTable.dataElements.find{it.label == "sample_date"}
        assertEquals 'description of summary metadata for sample_date',
                'Estimated Value Distribution (calculated by sampling 10% of rows)',
                sample_date.summaryMetadata[0].description

        /**
         * Enumeration type determined using a sample, so we can't be certain that there will be exactly 15 results.
         * But there should be between 1 and 15 values, and any values must be in our expected list.
         */
        final EnumerationType sampleVarcharEnumerationType = sampleTable.findDataElement('sample_varchar').dataType
        assertTrue 'One or more enumeration values', sampleVarcharEnumerationType.enumerationValues.size() >= 1
        assertTrue '15 or fewer enumeration values', sampleVarcharEnumerationType.enumerationValues.size() <= 15
        sampleVarcharEnumerationType.enumerationValues.each {
            assertTrue 'Enumeration key in expected set',
                    ['ENUM0', 'ENUM1', 'ENUM2', 'ENUM3', 'ENUM4', 'ENUM5', 'ENUM6', 'ENUM7', 'ENUM8', 'ENUM9', 'ENUM10', 'ENUM11', 'ENUM12', 'ENUM13', 'ENUM14'].contains(it.key)
        }
    }

    private void checkBasic(DataModel dataModel) {
        String expectedDataBase = "metadata_simple"
        String expectedSchema = "public";
        List<String> expectedTables = ["catalogue_item", "catalogue_user", "metadata", "organisation", "sample", "bigger_sample"]

        assertEquals 'Database/Model name', expectedDataBase, dataModel.label

        assertTrue 'COMMENT is present', dataModel.getMetadata().any{ Metadata md ->
            md.key == 'COMMENT' && md.value == 'A database called metadata_simple which is used for integration testing'
        }

        //Number of DataClasses is 1 for the schema, plus the number of tables in the schema
        assertEquals 'Number of tables/dataclasses', 1 + expectedTables.size(), dataModel.dataClasses?.size()

        //Number of child DataClasses is 1 i.e. just the public schema
        assertEquals 'Number of child tables/dataclasses', 1, dataModel.childDataClasses?.size()

        final DataClass publicSchema = dataModel.childDataClasses.first()
        assertEquals 'Number of child tables/dataclasses', expectedTables.size(), publicSchema.dataClasses?.size()

        final Set<DataClass> dataClasses = publicSchema.dataClasses

        expectedTables.each {expectedTableName ->
            assertTrue "Table ${expectedTableName} exists", dataClasses.any{it.label == expectedTableName}
        }

        final DataClass metadataTable = dataClasses.find {it.label == 'metadata'}
        assertEquals 'Metadata Number of columns/dataElements', 10, metadataTable.dataElements.size()
        assertEquals 'Metadata Number of metadata', 3, metadataTable.metadata.size()

        assertTrue 'MD All metadata values are valid', metadataTable.metadata.every {it.value && it.key != it.value}

        List<Map> indexesInfo = new JsonSlurper().parseText(metadataTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        assertEquals('MD Index count', 4, indexesInfo.size())

        assertEquals 'MD Primary key', 1, metadataTable.metadata.count {it.key == 'primary_key_name'}
        assertEquals 'MD Primary key', 1, metadataTable.metadata.count {it.key == 'primary_key_columns'}
        assertEquals 'MD Primary indexes', 1, indexesInfo.findAll {it.primaryIndex}.size()
        assertEquals 'MD Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'MD indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        final Map multipleColIndex =indexesInfo.find {it.name ==  'unique_item_id_namespace_key'}
        assertNotNull 'Should have multi column index', multipleColIndex
        assertEquals 'Correct order of columns', 'catalogue_item_id, namespace, key', multipleColIndex.columns

        final DataClass ciTable = dataClasses.find {it.label == 'catalogue_item'}
        assertEquals 'CI Number of columns/dataElements', 10, ciTable.dataElements.size()
        assertEquals 'CI Number of metadata', 3, ciTable.metadata.size()

        assertTrue 'CI All metadata values are valid', ciTable.metadata.every {it.value && it.key != it.value}

        indexesInfo = new JsonSlurper().parseText(ciTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        assertEquals('CI Index count', 3, indexesInfo.size())

        assertEquals 'CI Primary key', 1, ciTable.metadata.count {it.key == 'primary_key_name'}
        assertEquals 'CI Primary key', 1, ciTable.metadata.count {it.key == 'primary_key_columns'}
        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex}.size()
        assertEquals 'CI indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        final DataClass cuTable = dataClasses.find {it.label == 'catalogue_user'}
        assertEquals 'CU Number of columns/dataElements', 18, cuTable.dataElements.size()
        assertEquals 'CU Number of metadata', 5, cuTable.metadata.size()

        assertTrue 'CU All metadata values are valid', cuTable.metadata.every {it.value && it.key != it.value}

        indexesInfo = new JsonSlurper().parseText(cuTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        assertEquals('CU Index count', 3, indexesInfo.size())

        assertEquals 'CU Primary key', 1, cuTable.metadata.count {it.key == 'primary_key_name'}
        assertEquals 'CU Primary key', 1, cuTable.metadata.count {it.key == 'primary_key_columns'}
        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex}.size()
        assertEquals 'CI Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'CI indexes', 1, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()
        assertEquals 'CU constraint', 1, cuTable.metadata.count {it.key == 'unique_name'}
        assertEquals 'CU constraint', 1, cuTable.metadata.count {it.key == 'unique_columns'}

        // Columns
        assertTrue 'Metadata all elements required', metadataTable.dataElements.every {it.minMultiplicity == 1}
        assertEquals 'CI mandatory elements', 9, ciTable.dataElements.count {it.minMultiplicity == 1}
        assertEquals 'CI optional element description', 0, ciTable.findDataElement('description').minMultiplicity
        assertEquals 'CU mandatory elements', 10, cuTable.dataElements.count {it.minMultiplicity == 1}

    }

    private void checkOrganisationMetadata(DataClass organisationTable) {
        // Expect 4 metadata - 2 for the primary key and 1 for indexes, 1 for extended property
        assertEquals 'Organisation Number of metadata', 4, organisationTable.metadata.size()

        assertTrue 'COMMENT exists on organisation', organisationTable.getMetadata().any {Metadata md ->
            md.key == 'COMMENT' && md.value == 'A table about organisations'
        }

        DataElement org_code = organisationTable.findDataElement('org_code')
        assertTrue "COMMENT exists on org_code", org_code.getMetadata().any{ Metadata md ->
            md.key == 'COMMENT' && md.value == 'A column of organisation codes'
        }
    }

    private void checkOrganisationNotEnumerated(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass organisationTable = dataClasses.find {it.label == 'organisation'}

        Map<String, String> expectedColumns = [
                'org_code': 'PrimitiveType',
                'org_name': 'PrimitiveType',
                'org_char': 'PrimitiveType',
                'description': 'PrimitiveType',
                'org_type': 'PrimitiveType',
                'id': 'PrimitiveType'
        ]

        assertEquals 'Organisation Number of columns/dataElements', expectedColumns.size(), organisationTable.dataElements.size()
        //Expect all types to be Primitive, because we are not detecting enumerations
        expectedColumns.each {
            columnName, columnType ->
                assertEquals "DomainType of the DataType for ${columnName}", columnType, organisationTable.findDataElement(columnName).dataType.domainType
        }

        checkOrganisationMetadata(organisationTable)
    }

    private void checkOrganisationEnumerated(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass organisationTable = dataClasses.find {it.label == 'organisation'}

        Map<String, String> expectedColumns = [
                'org_code': 'EnumerationType',
                'org_name': 'PrimitiveType',
                'org_char': 'EnumerationType',
                'description': 'PrimitiveType',
                'org_type': 'EnumerationType',
                'id': 'PrimitiveType'
        ]

        assertEquals 'Organisation Number of columns/dataElements', expectedColumns.size(), organisationTable.dataElements.size()
        //Expect all types to be Primitive, because we are not detecting enumerations
        expectedColumns.each {
            columnName, columnType ->
                assertEquals "DomainType of the DataType for ${columnName}", columnType, organisationTable.findDataElement(columnName).dataType.domainType
        }

        checkOrganisationMetadata(organisationTable)


        final EnumerationType orgCodeEnumerationType = organisationTable.findDataElement('org_code').dataType
        assertEquals 'Number of enumeration values for org_code', 4, orgCodeEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration value found', orgCodeEnumerationType.enumerationValues.find{it.key == 'CODEZ'}
        assertNotNull 'Enumeration value found',orgCodeEnumerationType.enumerationValues.find{it.key == 'CODEY'}
        assertNotNull 'Enumeration value found',orgCodeEnumerationType.enumerationValues.find{it.key == 'CODEX'}
        assertNotNull 'Enumeration value found',orgCodeEnumerationType.enumerationValues.find{it.key == 'CODER'}
        assertNull 'Not an expected value', orgCodeEnumerationType.enumerationValues.find{it.key == 'CODEP'}

        final EnumerationType orgTypeEnumerationType = organisationTable.findDataElement('org_type').dataType
        assertEquals 'Number of enumeration values for org_type', 3, orgTypeEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find{it.key == 'TYPEA'}
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find{it.key == 'TYPEB'}
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find{it.key == 'TYPEC'}
        assertNull 'Not an expected value', orgTypeEnumerationType.enumerationValues.find{it.key == 'TYPEZ'}

        final EnumerationType orgCharEnumerationType = organisationTable.findDataElement('org_char').dataType
        assertEquals 'Number of enumeration values for org_char', 3, orgCharEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration value found', orgCharEnumerationType.enumerationValues.find{it.key == 'CHAR1'}
        assertNotNull 'Enumeration value found', orgCharEnumerationType.enumerationValues.find{it.key == 'CHAR2'}
        assertNotNull 'Enumeration value found', orgCharEnumerationType.enumerationValues.find{it.key == 'CHAR3'}
        assertNull 'Not an expected value', orgCharEnumerationType.enumerationValues.find{it.key == 'CHAR4'}
    }

    private checkSampleNoSummaryMetadata(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'sample'}

        List<String> expectedColumns = [
            "id",
            "sample_smallint",
            "sample_bigint",
            "sample_int",
            "sample_decimal",
            "sample_numeric",
            "sample_date",
            "sample_timestamp_without_tz",
            "sample_timestamp_with_tz"
        ]

        assertEquals 'Sample Number of columns/dataElements', expectedColumns.size(), sampleTable.dataElements.size()

        expectedColumns.each {columnName ->
            DataElement de = sampleTable.dataElements.find{it.label == columnName}
            assertEquals 'Zero summaryMetadata', 0, de.summaryMetadata.size()
        }
    }

    private void checkSampleSummaryMetadata(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'sample'}

        List<String> expectedColumns = [
                "id",
                "sample_smallint",
                "sample_bigint",
                "sample_int",
                "sample_decimal",
                "sample_numeric",
                "sample_date",
                "sample_timestamp_without_tz",
                "sample_timestamp_with_tz"
        ]

        assertEquals 'Sample Number of columns/dataElements', expectedColumns.size(), sampleTable.dataElements.size()

        expectedColumns.each {columnName ->
            DataElement de = sampleTable.dataElements.find{it.label == columnName}
            assertEquals 'One summaryMetadata', 1, de.summaryMetadata.size()
        }

        //sample_smallint
        final DataElement sample_smallint = sampleTable.dataElements.find{it.label == "sample_smallint"}
        assertEquals 'reportValue for sample_smallint',
                '{"-100 - -80":20,"-80 - -60":20,"-60 - -40":20,"-40 - -20":20,"-20 - 0":20,"0 - 20":20,"20 - 40":20,"40 - 60":20,"60 - 80":20,"80 - 100":20,"100 - 120":1}',
                sample_smallint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_bigint
        final DataElement sample_bigint = sampleTable.dataElements.find{it.label == "sample_bigint"}
        assertEquals 'reportValue for sample_bigint',
                '{"-1000000 - -800000":8,"-800000 - -600000":8,"-600000 - -400000":11,"-400000 - -200000":15,"-200000 - 0":58,"0 - 200000":59,"200000 - 400000":15,"400000 - 600000":11,"600000 - 800000":8,"800000 - 1000000":7,"1000000 - 1200000":1}',
                sample_bigint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_int
        final DataElement sample_int = sampleTable.dataElements.find{it.label == "sample_int"}
        assertEquals 'reportValue for sample_int',
                '{"0 - 1000":63,"1000 - 2000":26,"2000 - 3000":20,"3000 - 4000":18,"4000 - 5000":14,"5000 - 6000":14,"6000 - 7000":12,"7000 - 8000":12,"8000 - 9000":10,"9000 - 10000":10,"10000 - 11000":2}',
                sample_int.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_decimal
        final DataElement sample_decimal = sampleTable.dataElements.find{it.label == "sample_decimal"}
        assertEquals 'reportValue for sample_decimal',
                '{"0.000 - 1000000.000":83,"1000000.000 - 2000000.000":36,"2000000.000 - 3000000.000":26,"3000000.000 - 4000000.000":22,"4000000.000 - 5000000.000":20,"5000000.000 - 6000000.000":14}',
                sample_decimal.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_numeric
        final DataElement sample_numeric = sampleTable.dataElements.find{it.label == "sample_numeric"}
        assertEquals 'reportValue for sample_numeric',
                '{"-5.000000 - 0.000000":80,"0.000000 - 5.000000":81,"5.000000 - 10.000000":20}',
                sample_numeric.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_date
        final DataElement sample_date = sampleTable.dataElements.find{it.label == "sample_date"}
        assertEquals 'reportValue for sample_date',
                '{"May 2020":8,"Jun 2020":30,"Jul 2020":31,"Aug 2020":31,"Sep 2020":30,"Oct 2020":31,"Nov 2020":30,"Dec 2020":10}',
                sample_date.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_timestamp_without_tz
        final DataElement sample_timestamp_without_tz = sampleTable.dataElements.find{it.label == "sample_timestamp_without_tz"}
        assertEquals 'reportValue for sample_timestamp_without_tz',
                '{"27/08/2020 - 28/08/2020":4,"28/08/2020 - 29/08/2020":24,"29/08/2020 - 30/08/2020":24,"30/08/2020 - 31/08/2020":24,"31/08/2020 - 01/09/2020":24,"01/09/2020 - 02/09/2020":24,"02/09/2020 - 03/09/2020":24,"03/09/2020 - 04/09/2020":24,"04/09/2020 - 05/09/2020":24,"05/09/2020 - 06/09/2020":5}',
                sample_timestamp_without_tz.summaryMetadata[0].summaryMetadataReports[0].reportValue

        //sample_timestamp_with_tz
        //Timestamp wth timezone will give different results depending on the client timezone, so use a less strict test
        final DataElement sample_timestamp_with_tz = sampleTable.dataElements.find{it.label == "sample_timestamp_with_tz"}
        assertTrue 'reportValue for sample_timestamp_with_tz contains expected string',
                sample_timestamp_with_tz.summaryMetadata[0].summaryMetadataReports[0].reportValue.contains('"28/08/2020 - 29/08/2020":24,"29/08/2020 - 30/08/2020":24,"30/08/2020 - 31/08/2020":24,"31/08/2020 - 01/09/2020":24,"01/09/2020 - 02/09/2020":24,"02/09/2020 - 03/09/2020":24,"03/09/2020 - 04/09/2020":24,"04/09/2020 - 05/09/2020":24')

    }

    /**
     * Check that there is a DataClass for the bigger_sample table, with 4 columns but no
     * summary metadata on any of these columns.
     * @param dataModel
     * @return
     */
    private checkBiggerSampleNoSummaryMetadata(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'bigger_sample'}

        List<String> expectedColumns = [
                "sample_bigint",
                "sample_decimal",
                "sample_date",
                "sample_varchar"
        ]

        assertEquals 'Sample Number of columns/dataElements', expectedColumns.size(), sampleTable.dataElements.size()

        expectedColumns.each {columnName ->
            DataElement de = sampleTable.dataElements.find{it.label == columnName}
            assertEquals 'Zero summaryMetadata', 0, de.summaryMetadata.size()
        }
    }

    /**
     * Check that there is a DataClass for the bigger_sample table, with 4 columns but exact
     * summary metadata on any of these columns.
     * @param dataModel
     * @return
     */
    private checkBiggerSampleSummaryMetadata(DataModel dataModel) {
        final DataClass publicSchema = dataModel.childDataClasses.first()
        final Set<DataClass> dataClasses = publicSchema.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'bigger_sample'}

        //Map of column name to expected summary metadata description:reportValue. Expect exact counts.
        Map<String, Map<String, String>> expectedColumns = [
                "sample_bigint": ['Value Distribution':'{"0 - 100000":99999,"100000 - 200000":100000,"200000 - 300000":100000,"300000 - 400000":100000,"400000 - 500000":100000,"500000 - 600000":1}'],
                "sample_decimal": ['Value Distribution':'{"-1.000 - 0.000":249924,"0.000 - 1.000":245051,"1.000 - 2.000":5025}'],
                "sample_date": ['Value Distribution':'{"24/08/2020 - 26/08/2020":91266,"26/08/2020 - 28/08/2020":56304,"28/08/2020 - 30/08/2020":43810,"30/08/2020 - 01/09/2020":39468,"01/09/2020 - 03/09/2020":38302,"03/09/2020 - 05/09/2020":39468,"05/09/2020 - 07/09/2020":43810,"07/09/2020 - 09/09/2020":56306,"09/09/2020 - 11/09/2020":91266}'],
                "sample_varchar": []
        ]

        assertEquals 'Sample Number of columns/dataElements', expectedColumns.size(), sampleTable.dataElements.size()

        expectedColumns.each {columnName, expectedReport ->
            DataElement de = sampleTable.dataElements.find{it.label == columnName}
            assertEquals 'One summaryMetadata', expectedReport.size(), de.summaryMetadata.size()

            expectedReport.each {expectedReportDescription, expectedReportValue ->
                assertEquals "Description of summary metadatdata for ${columnName}", expectedReportDescription, de.summaryMetadata[0].description
                assertEquals "Value of summary metadatdata for ${columnName}", expectedReportValue, de.summaryMetadata[0].summaryMetadataReports[0].reportValue
            }
        }
    }
}
