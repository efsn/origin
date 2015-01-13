package org.blue.sys.factory;

import org.blue.sys.dao.proxy.PryDeleteDao;
import org.blue.sys.dao.proxy.PryEssayCheckDao;
import org.blue.sys.dao.proxy.PryInfoUpdateDao;
import org.blue.sys.dao.proxy.PryInsertDao;
import org.blue.sys.dao.proxy.PryQueryAllDao;
import org.blue.sys.dao.proxy.PryQueryDao;
import org.blue.sys.dao.proxy.PryTypeDao;

public class PryFactory {
    public static PryInsertDao getPryInsertDao() {
        return new PryInsertDao();
    }

    public static PryQueryDao getPryQueryDao() {
        return new PryQueryDao();
    }

    public static PryQueryAllDao getPryQueryAllDao() {
        return new PryQueryAllDao();
    }

    public static PryInfoUpdateDao getPryInfoUpdateDao() {
        return new PryInfoUpdateDao();
    }

    public static PryTypeDao getPryTypeDao() {
        return new PryTypeDao();
    }

    public static PryDeleteDao getPryDeleteDao() {
        return new PryDeleteDao();
    }

    public static PryEssayCheckDao getPryEssayCheckDao() {
        return new PryEssayCheckDao();
    }

}
