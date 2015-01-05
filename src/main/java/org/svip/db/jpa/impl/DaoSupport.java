package org.svip.db.jpa.impl;

import org.svip.db.jpa.Dao;

import java.io.Serializable;

/**
 * @author Chan
 * @version 1.0
 *          Created on 2014/8/23
 */
public class DaoSupport<T> implements Dao<T>{
    @Override
    public void delete(Serializable entiryid){

    }

    @Override
    public <T1> T1 find(Serializable entityid){
        return null;
    }

    @Override
    public long getCount(){
        return 0;
    }

}
