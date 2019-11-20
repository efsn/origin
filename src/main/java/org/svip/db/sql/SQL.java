package org.svip.db.sql;

import java.io.Serializable;

/**
 * @author Chan
 * @version 1.0
 * Created on 2014/08/18
 */
public interface SQL extends Serializable {

    String C_T = " CREATE TABLE ";
    String N_N = " NOT NULL ";
    String A_I = " AUTO_INCREMENT ";
    String NULL = " NULL ";
    String L = "(";
    String R = ")";
    String S_Q = "`";
    String D = ",";
    String SPACE = " ";
    String PRIMARY = " PRIMARY KEY ";
    String IDX = " INDEX ";
    String USING = " USING ";
    String HASH = " HASH ";
    String M = "=";
    String DEFAULT = " DEFAULT ";
    String CHARSET = "CHARSET";
    String ENGINE = " ENGINE";


}
