package org.svip.db.enumeration.mysql;

/**
 * Declare database column type enumeration
 *
 * @author Blues
 */
public enum DbType {
    /**
     * fixed length string, at most 255 characters
     * char(length)
     */
    CHAR,

    /**
     * variable length string, at most 255 characters
     * varchar(length)
     */
    VARCHAR,

    /**
     * store at most 255 characters string
     */
    TINYTEXT,

    /**
     * store at most 65535 characters string
     */
    TEXT,

    /**
     * binary large object, store at most 65536 bytes
     */
    BLOB,

    /**
     * store at most 16777215 characters string
     */
    MEDIUMTEXT,

    /**
     * store at most 16777215 bytes
     */
    MEDIUMBLOB,

    /**
     * store at most 4294967295 characters string
     */
    LONGTEXT,

    /**
     * store at most 4294967295 bytes
     */
    LONGBLOB,

    /**
     * enable enter in 65535 value, if none store null
     * enum(a, b, etc)
     */
    EMUN,

    /**
     * same as EMUN  at most 64 lists
     * enum(a, b, etc)
     */
    SET,

    /**
     * -128 to 127 or 0 to 255
     * tinyint(length)
     */
    TINYINT,

    /**
     * -32768 to 32767 or 0 to 65535
     * smallint(length)
     */
    SMALLINT,

    /**
     * -8388608 to 8388607 or 0 to 16777215
     * mediumint(length)
     */
    MEDIUMINT,

    /**
     * -2147483648 to 2147483647 or 0 to 4294967295
     * int(length)
     */
    INT,

    /**
     * -9223372036854775808 to 9223372036854775807 or 0 to 18446744073709551615
     * bigint(length)
     */
    BIGINT,

    /**
     * float(length, d)
     */
    FLOAT,

    /**
     * double(length, d)
     */
    DOUBLE,

    /**
     * decimal(length, d)
     */
    DECIMAL,

    /**
     * format: YYYY-MM-DD, 1000-01-01 to 9999-12-31
     */
    DATE,

    /**
     * format: YYYY-MM-DD HH:MM:SS
     * 1000-01-01 00:00:00 to 9999-12-31 23:59:59
     */
    DATETIME,

    /**
     * format: YYYY-MM-DD HH:MM:SS
     * unix 1970-01-01 00:00:00 UTC to 2038-01-09 03:14:07 UTC
     */
    TIMESTAMP,

    /**
     * format: HH:MM:SS
     * -838:59:59 to 838:59:59
     */
    TIME,

    /**
     * format: two or four number
     * four = 1901:2155
     * two = 70:69 (1970:2069)
     */
    YEAR,

    /**
     * normal index
     */
    IDX_NORMAL,

    /**
     * unique index, allow null
     */
    IDX_UNIQUE,

    /**
     * primary index, not allow null
     */
    IDX_PRIMARY,

    /**
     * combined index, from with left
     */
    IDX_COMBINED,

    /**
     * binary tree mode
     */
    BTREE,

    /**
     * index hash code mode
     */
    HASH

}
