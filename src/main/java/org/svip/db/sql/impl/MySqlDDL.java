package org.svip.db.sql.impl;

import org.svip.db.annotation.meta.Column;
import org.svip.db.annotation.meta.Constraint;
import org.svip.db.annotation.meta.Index;
import org.svip.db.annotation.meta.Table;
import org.svip.db.sql.DDL;
import org.svip.db.sql.SQL;
import org.svip.util.StrUtil;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Data Definition Language
 *
 * @author Chan
 * @version 1.0
 * Created on 2014/08/20
 */
public final class MySqlDDL implements DDL {

    /**
     *
     */
    private static final long serialVersionUID = 2955496431949973831L;

    @Override
    public String getCreateSql(Class<?> clazz) throws SQLException {
        if (clazz != null) {
            return parseBean(clazz);
        }
        return null;
    }

    /**
     * Based on po produce define sql
     *
     * @param clazz bean object
     */
    private String parseBean(Class<?> clazz) throws SQLException {
        Table ann = clazz.getAnnotation(Table.class);
        if (ann == null) {
            return null;
        }
        String className = StrUtil.substring(clazz.getName(), ".", true);
        StringBuffer sql = new StringBuffer(SQL.C_T);
        sql.append(SQL.S_Q).append(StrUtil.getDbName(className)).append(SQL.S_Q).append(SQL.L);

        //all fields produce columns
        String cols = getColumns(clazz.getDeclaredFields());
        if (StrUtil.isNull(cols)) {
            throw new SQLException("A table must have at least one column.");
        } else {
            sql.append(cols);
        }
        String idxs = getIndexs(ann.index());
        if (!StrUtil.isNull(idxs)) {
            sql.append(idxs);
        }
        sql.deleteCharAt(sql.length() - 1).append(SQL.R).
                append(SQL.ENGINE).append(SQL.M).append(ann.engine()).append(SQL.DEFAULT).
                append(SQL.CHARSET).append(SQL.M).append(ann.charset());
        return sql.toString();
    }

    private String getIndexs(Index[] idxs) {
        if (idxs.length < 1) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (Index idx : idxs) {
            sb.append(SQL.IDX).append(SQL.S_Q).append(idx.name()).append(SQL.S_Q);
            sb.append(SQL.SPACE).append(SQL.L).append(StrUtil.concat(idx.column(), SQL.S_Q, SQL.D)).append(SQL.R);
            sb.append(SQL.USING).append(idx.mode().toString()).append(SQL.S_Q);
        }
        return sb.toString();
    }

    private String getColumns(Field[] fields) throws SQLException {
        if (fields == null || fields.length < 1) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        StringBuffer primary = new StringBuffer();
        for (Field field : fields) {
            Column ann = field.getAnnotation(Column.class);
            if (ann == null) {
                continue;
            }
            sb.append(SQL.S_Q).append(StrUtil.getDbName(field.getName())).append(SQL.S_Q).append(SQL.SPACE);
            if (ann.length() > 0) {
                sb.append(ann.type().toString()).append(SQL.L).append(ann.length()).append(SQL.R);
            } else {
                sb.append(ann.type().toString());
            }
            Constraint constraint = ann.constraint();
            if (constraint == null) {
                throw new SQLException("Constraint is null.");
            }
            if (constraint.primary()) {
                primary.append(SQL.PRIMARY).append(SQL.L).append(SQL.S_Q).append(field.getName()).append(SQL.S_Q).append(SQL.R).append(SQL.D);
            }
            if (constraint.nullAble()) {
                sb.append(SQL.N_N);
            } else {
                sb.append(SQL.NULL);
            }
            if (constraint.autoIncrement()) {
                sb.append(SQL.A_I);
            }
            sb.append(SQL.D);
        }
        sb.append(primary);
        return sb.toString();
    }

}
