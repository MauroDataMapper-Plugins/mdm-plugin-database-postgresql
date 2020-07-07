package uk.ac.ox.softeng.maurodatamapper.plugin.database.postgres

import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.PrimitiveType
import ox.softeng.metadatacatalogue.core.traits.spi.datatype.DefaultDataTypeProvider

/**
 * @since 19/04/2018
 */
class PostgresDefaultDatatypeProvider implements DefaultDataTypeProvider {
    @Override
    List<DataType> getDefaultListOfDataTypes() {
        [
            new PrimitiveType(label: 'bigint', description: 'signed eight-byte integer'),
            new PrimitiveType(label: 'bigserial', description: 'autoincrementing eight-byte integer'),
            new PrimitiveType(label: 'bit [ (n) ]', description: 'fixed-length bit string'),
            new PrimitiveType(label: 'bit varying [ (n) ]', description: 'variable-length bit string'),
            new PrimitiveType(label: 'boolean', description: 'logical Boolean (true/false)'),
            new PrimitiveType(label: 'box', description: 'rectangular box on a plane'),
            new PrimitiveType(label: 'bytea', description: 'binary data ("byte array")'),
            new PrimitiveType(label: 'character [ (n) ]', description: 'fixed-length character string'),
            new PrimitiveType(label: 'character varying', description: 'variable-length character string'),
            new PrimitiveType(label: 'character varying [ (n) ]', description: 'variable-length character string'),
            new PrimitiveType(label: 'cidr', description: 'IPv4 or IPv6 network address'),
            new PrimitiveType(label: 'circle', description: 'circle on a plane'),
            new PrimitiveType(label: 'date', description: 'calendar date (year, month, day)'),
            new PrimitiveType(label: 'double precision', description: 'double precision floating-point number (8 bytes)'),
            new PrimitiveType(label: 'inet', description: 'IPv4 or IPv6 host address'),
            new PrimitiveType(label: 'integer', description: 'signed four-byte integer'),
            new PrimitiveType(label: 'interval [ fields ] [ (p) ]', description: 'time span'),
            new PrimitiveType(label: 'json', description: 'textual JSON data'),
            new PrimitiveType(label: 'jsonb', description: 'binary JSON data, decomposed'),
            new PrimitiveType(label: 'line', description: 'infinite line on a plane'),
            new PrimitiveType(label: 'lseg', description: 'line segment on a plane'),
            new PrimitiveType(label: 'macaddr', description: 'MAC (Media Access Control) address'),
            new PrimitiveType(label: 'money', description: 'currency amount'),
            new PrimitiveType(label: 'numeric [ (p, s) ]', description: 'exact numeric of selectable precision'),
            new PrimitiveType(label: 'path', description: 'geometric path on a plane'),
            new PrimitiveType(label: 'pg_lsn', description: 'PostgreSQL Log Sequence Number'),
            new PrimitiveType(label: 'point', description: 'geometric point on a plane'),
            new PrimitiveType(label: 'polygon', description: 'closed geometric path on a plane'),
            new PrimitiveType(label: 'real', description: 'single precision floating-point number (4 bytes)'),
            new PrimitiveType(label: 'smallint', description: 'signed two-byte integer'),
            new PrimitiveType(label: 'smallserial', description: 'autoincrementing two-byte integer'),
            new PrimitiveType(label: 'serial', description: 'autoincrementing four-byte integer'),
            new PrimitiveType(label: 'text', description: 'variable-length character string'),
            new PrimitiveType(label: 'time without time zone', description: 'time of day (no time zone)'),
            new PrimitiveType(label: 'time with time zone', description: 'time of day, including time zone'),
            new PrimitiveType(label: 'timestamp without time zone', description: 'date and time (no time zone)'),
            new PrimitiveType(label: 'timestamp with time zone', description: 'date and time, including time zone'),
            new PrimitiveType(label: 'time [ (p) ] [ without time zone ]', description: 'time of day (no time zone)'),
            new PrimitiveType(label: 'time [ (p) ] with time zone', description: 'time of day, including time zone'),
            new PrimitiveType(label: 'timestamp [ (p) ] [ without time zone ]', description: 'date and time (no time zone)'),
            new PrimitiveType(label: 'timestamp [ (p) ] with time zone', description: 'date and time, including time zone'),
            new PrimitiveType(label: 'tsquery', description: 'text search query'),
            new PrimitiveType(label: 'tsvector', description: 'text search document'),
            new PrimitiveType(label: 'txid_snapshot', description: 'user-level transaction ID snapshot'),
            new PrimitiveType(label: 'uuid', description: 'universally unique identifier'),
            new PrimitiveType(label: 'xml', description: 'XML data'),
        ]
    }

    @Override
    String getDisplayName() {
        'PostgreSQL 9 DataTypes'
    }
}
