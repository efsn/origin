package org.svip.pool.db;

import java.sql.SQLException;

/**
 * @author Blues
 * @version 1.0
 *          Created on 2014/8/24
 */
public class DbPoolException extends SQLException{
      public DbPoolException(String msg){
        super(msg);
    }
}
