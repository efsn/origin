package org.blue.sys.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.blue.sys.factory.PryFactory;

public class MessageFromAdminServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession();
        String pname = (String) session.getAttribute("user");
        String messagee = request.getParameter("messagee");

        Map<String, String> value = new HashMap<String, String>();

        value.put("user", pname);
        value.put("messagee", messagee);

        try{
            if(PryFactory.getPryInfoUpdateDao().doAdminUpdate(value)){
                response.sendRedirect("/sys/success/messageSuccessAdmin.jsp");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
