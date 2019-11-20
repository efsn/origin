package org.jspsmart.upload;

import java.util.Enumeration;
import java.util.Hashtable;

public class Request {

    private Hashtable<String, Hashtable<Integer, String>> hts = new Hashtable<String, Hashtable<Integer, String>>();
    private int count;

    protected void putParameter(String name, String content) {
        if (name == null)
            throw new IllegalArgumentException("The name of an element cannot be null");
        Hashtable<Integer, String> ht;
        if (hts.containsKey(name)) {
            ht = hts.get(name);
            ht.put(ht.size(), content);
        } else {
            ht = new Hashtable<Integer, String>();
            ht.put(0, content);
            hts.put(name, ht);
            count += 1;
        }
    }

    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Form's name is invalid or does not exist");
        }
        Hashtable<Integer, String> ht = hts.get(name);
        if (ht == null) return null;
        return ht.get(0);
    }

    public Enumeration<String> getParameterNames() {
        return hts.keys();
    }

    public String[] getParameterValues(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Form's name is invalid or does not exist");
        }
        Hashtable<Integer, String> ht = hts.get(name);
        if (ht == null) return null;
        String[] fileContents = new String[ht.size()];
        for (int i = 0; i < ht.size(); i++)
            fileContents[i] = ht.get(i);
        return fileContents;
    }
}