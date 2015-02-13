package org.svip.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common regEx enumeration
 * 
 * @author Codeyn
 *
 */
public enum RegExUtil{
    
    IP("((2[0-4]/d|25[0-5]|[01]?/d/d?)/.){3}(2[0-4]/d|25[0-5]|[01]?/d/d?)"),
    EMAIL(""),
    TAG_INNERHTML("(?<=<(/w+)>).*(?=<///1>)");
    
    private static final char ENTIRE_PRE = '^';
    private static final char ENTIRE_SUF = '$';
    
    private String regEx;
    private boolean isEntire = true;
    
    private RegExUtil(String regEx, boolean isEntire){
        this(regEx);
        this.isEntire = isEntire;
    }
    
    private RegExUtil(String regEx){
        this.regEx = regEx;
    }
    
    public boolean matches(CharSequence input){
        return Pattern.matches(getEntireRegEx(), input);
    }
    
    public Matcher matcher(CharSequence input){
        return Pattern.compile(getEntireRegEx()).matcher(input);
    }
    
    public void setIsEntire(boolean isEntire){
        this.isEntire = isEntire;
    }
    
    private String getEntireRegEx(){
        StringBuffer sb = new StringBuffer(regEx);
        if(isEntire){
            sb.setCharAt(0, ENTIRE_PRE);
            sb.append(ENTIRE_SUF);
        }
        return sb.toString();
    }

}
