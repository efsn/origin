package org.blue.sys.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.blue.sys.factory.PryFactory;

public class EssayCheckServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession();
        String essayName = (String) session.getAttribute("essay_name");
        String checkTo = request.getParameter("checkTo");
        String checkMark = request.getParameter("checkMark");

        Map<String, String> value = new HashMap<String, String>();

        if(null != essayName && !"".equals(essayName)){
            value.put("essayName", essayName);
        }

        if(null != checkMark && !"".equals(checkMark)){
            value.put("checkMark", checkMark);
        }

        if("not".equals(checkTo)){
            value.put("not", "not pass");

            try{
                if(PryFactory.getPryEssayCheckDao().EditorCheckFirst(value)){
                    response.sendRedirect("/sys/host/editor/editorHost.jsp");
                }
                else{
                    response.sendRedirect("/sys/error/checkError.jsp");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("expert".equals(checkTo)){
            try{
                if(PryFactory.getPryEssayCheckDao().EditorCheckFirst(value)){
                    response.sendRedirect("/sys/host/editor/editorHost.jsp");
                }
                else{
                    response.sendRedirect("/sys/error/checkError.jsp");
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
