package org.svip.util.proxy.impl;

import org.svip.util.proxy.Advice;

public class AdviceImpl implements Advice{

    @Override
    public String before(){
        return "Before";
    }

    @Override
    public String after(){
        return "After";
    }

}
