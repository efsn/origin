package template;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbMgr{
    private String username = "blues";
    private String password = "blues";
    private String url = "jdbc:mysql://localhost:3306/svip";
    private String driver = "com.mysql.jdbc.Driver";



    private DbMgr(){
    }

    public static DbMgr getInstance(){
        return Singleton.instance;
    }

    private static class Singleton{
        private static DbMgr instance = new DbMgr();
    }

    public Connection getConnection(){
        try{
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
