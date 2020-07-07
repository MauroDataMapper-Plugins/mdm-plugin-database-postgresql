package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import uk.ac.ox.softeng.maurodatamapper.plugin.database.AbstractDatabaseImporter
import uk.ac.ox.softeng.maurodatamapper.plugin.database.RemoteDatabaseImporter

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * Created by james on 31/05/2017.
 */
class PostgresDatabaseImporterService extends AbstractDatabaseImporter<PostgresDatabaseImportParameters> implements RemoteDatabaseImporter {

    public static final String IS_NOT_NULL_CONSTRAINT = 'IS NOT NULL'

    @Override
    String getDatabaseStructureQueryString() {
        '''
SELECT *
FROM information_schema.columns
WHERE table_schema NOT IN ('pg_catalog','information_schema');
'''
    }

    @Override
    String getDisplayName() {
        'PostgreSQL Importer'
    }

    @Override
    String getVersion() {
        '2.0.0'
    }

    @Override
    Set<String> getKnownMetadataKeys() {
        [
            'ordinal_position', 'column_default', 'is_nullable', 'character_maximum_length', 'character_octet_length',
            'numeric_precision', 'numeric_precision_radix', 'numeric_scale', 'datetime_precision', 'interval_type', 'interval_precision',
            'character_set_catalog', 'character_set_schema', 'character_set_name', 'collation_catalog', 'collation_schema', 'collation_name',
            'domain_catalog', 'domain_schema', 'domain_name', 'udt_catalog', 'udt_schema', 'udt_name', 'scope_catalog', 'scope_schema',
            'scope_name', 'maximum_cardinality', 'dtd_identifier', 'is_self_referencing', 'is_identity', 'identity_generation',
            'identity_start', 'identity_increment', 'identity_maximum', 'identity_minimum', 'identity_cycle', 'is_generated',
            'generation_expression', 'is_updatable', 'primary_index[]', 'unique_index[]', 'index[]', 'primary_key[]', 'unique[]', 'foreign_key[]',
        ]
    }

    @Override
    Boolean allowsExtraMetadataKeys() {
        true
    }

    @Override
    String getIndexInformationQueryString() {
        '''
SELECT
  table_name,
  index_name,
  unique_index,
  primary_index,
  clustered,
  array_to_string(array_agg(column_name), ', ') AS column_names
FROM (
       SELECT
         t.relname         AS table_name,
         i.relname         AS index_name,
         a.attname         AS column_name,
         ix.indisunique    AS unique_index,
         ix.indisprimary   AS primary_index,
         ix.indisclustered AS clustered,
         unnest(ix.indkey) AS unn,
         a.attnum
       FROM pg_catalog.pg_index ix
         LEFT JOIN pg_catalog.pg_class t ON ix.indrelid = t.oid
         LEFT JOIN pg_catalog.pg_class i ON ix.indexrelid = i.oid
         LEFT JOIN pg_catalog.pg_attribute a ON (t.oid = a.attrelid AND a.attnum = ANY (ix.indkey))
         LEFT JOIN pg_catalog.pg_namespace ns ON t.relnamespace = ns.oid
       WHERE ns.nspname = ?
       ORDER BY
         t.relname,
         i.relname,
         generate_subscripts(ix.indkey, 1)) sb
WHERE unn = attnum
GROUP BY table_name, index_name, unique_index, primary_index, clustered;
'''
    }

    @Override
    String getForeignKeyInformationQueryString() {
        '''
SELECT
  tc.constraint_name,
  tc.table_name,
  kcu.column_name,
  ccu.table_name  AS reference_table_name,
  ccu.column_name AS reference_column_name
FROM
  information_schema.table_constraints AS tc
  JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
  JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE constraint_type = 'FOREIGN KEY' AND tc.constraint_schema = ?;
'''
    }

    @Override
    PreparedStatement prepareCoreStatement(Connection connection, PostgresDatabaseImportParameters params) {
        PreparedStatement st
        if (params.schemaNames) {
            List<String> names = params.schemaNames.split(',')
            String sb = """SELECT * FROM information_schema.columns WHERE table_schema IN (${names.collect {'?'}.join(',')});"""
            st = connection.prepareStatement(sb)
            names.eachWithIndex {String entry, int i ->
                st.setString(i + 1, entry)
            }
            return st
        }
        super.prepareCoreStatement(connection, params)
    }
}
