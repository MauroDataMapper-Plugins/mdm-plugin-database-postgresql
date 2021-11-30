--Metadata for the dialect is migrated to the .postgres namespace
UPDATE core.metadata
SET namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres'
WHERE namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database'
AND multi_facet_aware_item_domain_type = 'DataModel'
AND key = 'dialect'
AND value = 'Postgres';

--Metadata on DataElement is migrated to the .postgres.column namespace
UPDATE core.metadata
SET namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.column'
WHERE namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres'
AND multi_facet_aware_item_domain_type = 'DataElement';

--Metadata on DataClass where the DataClass is a table rather than a schema is migrated to the .postgres.table namespace
UPDATE core.metadata
SET namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.table'
WHERE namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres'
AND multi_facet_aware_item_domain_type = 'DataClass'
AND multi_facet_aware_item_id IN (
    SELECT id FROM datamodel.data_class
	WHERE parent_data_class_id IS NOT NULL
);

--Metadata on DataClass where the DataClass is a schema is migrated to the .postgres.schema namespace
UPDATE core.metadata
SET namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres.schema'
WHERE namespace = 'uk.ac.ox.softeng.maurodatamapper.plugins.database.postgres'
AND multi_facet_aware_item_domain_type = 'DataClass'
AND multi_facet_aware_item_id IN (
    SELECT id FROM datamodel.data_class
	WHERE parent_data_class_id IS NULL
);