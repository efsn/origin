package org.blue.sys.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blue.sys.factory.PryFactory;

public class MessageFromEditorServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        String person = request.getParameter("person");

        String author_pname = request.getParameter("author_pname");
        String admin_pname = request.getParameter("admin_pname");
        String expert_pname = request.getParameter("expert_pname");

        String message = request.getParameter("message");

        Map<String, String> value = new HashMap<String, String>();

        if("author".equals(person)){
            value.put("user", author_pname);
            value.put("message", message);

            try{
                if(PryFactory.getPryInfoUpdateDao().doAuthorUpdate(value)){
                    response.sendRedirect("/sys/success/messageSucess.jsp");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("expert".equals(person)){
            value.put("user", expert_pname);
            value.put("message", message);
            try{
                if(PryFactory.getPryInfoUpdateDao().doExpertUpdate(value)){
                    response.sendRedirect("/sys/success/messageSucess.jsp");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("admin".equals(person)){
            value.put("user", admin_pname);
            value.put("message", message);
            try{
                if(PryFactory.getPryInfoUpdateDao().doAdminUpdate(value)){
                    response.sendRedirect("/sys/message/messageSucess.jsp");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
