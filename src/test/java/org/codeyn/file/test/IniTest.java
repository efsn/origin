package org.codeyn.file.test;

import org.codeyn.file.Ini;

import java.io.IOException;

public class IniTest {
    public static void main(String[] args) throws IOException {
        System.out.println(new Ini("E:/codeyn/file/test.ini"));
    }
}
