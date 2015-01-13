package org.blue.sys.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.blue.sys.factory.PryFactory;

public class InfoUpdateServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        Map<String, String> value = new HashMap<String, String>();

        HttpSession session = request.getSession();
        String pname = (String) session.getAttribute("user");

        value.put("user", pname);

        if(null != request.getParameter("checkUserName")){
            value.put("userName", request.getParameter("userName"));
        }
        if(null != request.getParameter("checkAddress")){
            value.put("address", request.getParameter("address"));
        }
        if(null != request.getParameter("checkEmail")){
            value.put("email", request.getParameter("email"));
        }
        if(null != request.getParameter("checkPhone")){
            value.put("phone", request.getParameter("phone"));
        }
        if(null != request.getParameter("checkMark")){
            value.put("mark", request.getParameter("mark"));
        }

        if("author".equals(session.getAttribute("update"))){
            try{
                if(PryFactory.getPryInfoUpdateDao().doAuthorUpdate(value)){
                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getAuthor(pname));
                    response.sendRedirect("success/authorInfoUpdateSucess.jsp");
                }
                else{
                    response.sendRedirect("error/authorInfoUpdateError.jsp");
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("editor".equals(session.getAttribute("update"))){
            try{
                if(PryFactory.getPryInfoUpdateDao().doEditorUpdate(value)){
                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getEditor(pname));
                    response.sendRedirect("success/editorInfoUpdateSucess.jsp");
                }
                else{
                    response.sendRedirect("error/editorInfoUpdateError.jsp");
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("expert".equals(session.getAttribute("update"))){
            try{
                if(PryFactory.getPryInfoUpdateDao().doExpertUpdate(value)){
                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getExpert(pname));
                    response.sendRedirect("success/expertInfoUpdateSucess.jsp");
                }
                else{
                    response.sendRedirect("error/expertInfoUpdateError.jsp");
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
