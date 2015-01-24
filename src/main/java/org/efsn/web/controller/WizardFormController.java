package org.efsn.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import template.bean.User;

public class WizardFormController extends AbstractWizardFormController{
    
    private String cancelView;
    private String finishView;
    
    public String getCancelView(){
        return cancelView;
    }

    public void setCancelView(String cancelView){
        this.cancelView = cancelView;
    }

    public String getFinishView(){
        return finishView;
    }

    public void setFinishView(String finishView){
        this.finishView = finishView;
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception{
        User user =  (User) createCommand();
        user.setUsername("Please enter your name");
        user.setPassword("Please enter your password");
        return user;
    }
    
    @Override
    protected Map referenceData(HttpServletRequest request, int page)
            throws Exception{
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("address", Arrays.asList("New York, Hong Kong"));
        return map;
    }
    

    @Override
    protected ModelAndView processFinish(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object command,
                                         BindException errors)throws Exception{
        return new ModelAndView(getFinishView());
    }

    @Override
    protected ModelAndView processCancel(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object command, 
                                         BindException errors)throws Exception{
        return new ModelAndView(getCancelView());
    }
    
    @Override
    protected void validatePage(Object command, Errors errors, int page){
    }
    
    @Override
    protected void postProcessPage(HttpServletRequest request, Object command,
            Errors errors, int page) throws Exception{
    }
    
    
}
