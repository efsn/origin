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
import org.blue.util.ReturnError;

public class PwdAlterServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String pname = (String) session.getAttribute("user");
        String user = (String) session.getAttribute("update");

        String authorPW0 = request.getParameter("authorPW0");
        String authorPW1 = request.getParameter("authorPW1");
        String authorPW2 = request.getParameter("authorPW2");

        String editorPW0 = request.getParameter("editorPW0");
        String editorPW1 = request.getParameter("editorPW1");
        String editorPW2 = request.getParameter("editorPW2");

        String expertPW0 = request.getParameter("expertPW0");
        String expertPW1 = request.getParameter("expertPW1");
        String expertPW2 = request.getParameter("expertPW2");

        String adminPW0 = request.getParameter("adminPW0");
        String adminPW1 = request.getParameter("adminPW1");
        String adminPW2 = request.getParameter("adminPW2");

        Map<String, String> value = new HashMap<String, String>();

        value.put("user", pname);

        if(null != request.getParameter("checkUserName")){
            value.put("userName", request.getParameter("userName"));
        }
        if(ReturnError.isLength(authorPW1)
                && ReturnError.isEquals(authorPW1, authorPW2)){
            value.put("authorPW1", authorPW1);
        }
        if(ReturnError.isLength(editorPW1)
                && ReturnError.isEquals(editorPW1, editorPW2)){
            value.put("editorPW1", editorPW1);
        }
        if(ReturnError.isLength(expertPW1)
                && ReturnError.isEquals(expertPW1, expertPW2)){
            value.put("expertPW1", expertPW1);
        }
        if(ReturnError.isLength(adminPW1)
                && ReturnError.isEquals(adminPW1, adminPW2)){
            value.put("adminPW1", adminPW1);
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

        if("author".equals(user)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao().getAuthor();
                if(!map.get(pname).equals(authorPW0)){
                    response.sendRedirect("/sys/error/authorPwdAlterError.jsp");
                }
                else{
                    if(PryFactory.getPryInfoUpdateDao().doAuthorUpdate(value)){
                        response.sendRedirect("success/authorPwdAlterSucess.jsp");
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("editor".equals(user)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getEditor();

                if(!map.get(pname).equals(editorPW0)){
                    response.sendRedirect("/sys/error/editorPwdAlterError.jsp");
                }
                else{
                    if(PryFactory.getPryInfoUpdateDao().doEditorUpdate(value)){
                        response.sendRedirect("success/editorPwdAlterSucess.jsp");
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("expert".equals(user)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getExpert();

                if(!map.get(pname).equals(expertPW0)){
                    response.sendRedirect("/sys/error/expertPwdAlterError.jsp");
                }
                else{
                    if(PryFactory.getPryInfoUpdateDao().doExpertUpdate(value)){
                        response.sendRedirect("success/expertPwdAlterSucess.jsp");
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if("admin".equals(user)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getAdmin();

                if(!map.get(pname).equals(adminPW0)){
                    response.sendRedirect("/sys/error/adminPwdAlterError.jsp");
                }
                else{
                    if(PryFactory.getPryInfoUpdateDao().doAdminUpdate(value)){
                        response.sendRedirect("success/adminPwdAlterSucess.jsp");
                    }
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
