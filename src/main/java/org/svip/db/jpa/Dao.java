package org.svip.db.jpa;

import java.io.Serializable;

/**
 * @author Chan
 * @version 1.0
 *          Created on 2014/8/23
 */
public interface Dao<T>{
    void delete(Serializable entiryid);
    <T> T find(Serializable entityid);
    long getCount();
}
