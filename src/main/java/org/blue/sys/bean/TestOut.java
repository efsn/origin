package org.blue.sys.bean;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestOut extends HttpServlet {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		super.doGet(req, resp);
		
		resp.setContentType("text/plain");
		resp.getWriter().println("test java bae version 1 of app ");
		resp.getWriter().println("update 0");
			
	}

}
