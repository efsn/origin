package org.efsn.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardController extends HttpServlet{
    
    private static final long serialVersionUID = -2431247805625017867L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{
        resp.getWriter().write("Contrller forward to servlet!");
    }
}
