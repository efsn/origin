package test.bean;

import org.svip.db.annotation.util.Parser;

import template.bean.User;

/**
 * Created by Chan on 2014/8/14.
 */
public class TestBean{
    public static final String S = "A";
    public static void main(String[] args) throws Exception{
        System.out.println(Parser.getInstance().parseBean(User.class));
        System.out.println(S);
    }
    
}
