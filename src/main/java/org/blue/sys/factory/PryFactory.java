package com.blue.sys.factory;

import com.blue.sys.dao.proxy.PryDeleteDao;
import com.blue.sys.dao.proxy.PryEssayCheckDao;
import com.blue.sys.dao.proxy.PryInfoUpdateDao;
import com.blue.sys.dao.proxy.PryInsertDao;
import com.blue.sys.dao.proxy.PryQueryAllDao;
import com.blue.sys.dao.proxy.PryQueryDao;
import com.blue.sys.dao.proxy.PryTypeDao;

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
