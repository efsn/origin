package org.codeyn.file.test;

import java.io.IOException;

import org.codeyn.file.Ini;

public class IniTest{
    public static void main(String[] args) throws IOException{
        System.out.println(new Ini("E:/codeyn/file/test.ini"));
    }
}
