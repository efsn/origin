package org.blue.sys.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blue.sys.dao.proxy.PryInsertDao;
import org.blue.sys.factory.PryFactory;
import org.blue.sys.vo.Editor;
import org.blue.util.ReturnError;
import org.blue.util.VerifyInfo;

public class EditorInsertServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Editor editor = new Editor();

        String pname = request.getParameter("userName");
        String pwd = request.getParameter("editorPW1");
        String rePwd = request.getParameter("editorPW2");
        String name = request.getParameter("name");
        String telephone = request.getParameter("phone");
        String email = request.getParameter("email");

        if(ReturnError.isLength(pname)){
            editor.setEditorPname(pname);
        }
        else{
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(ReturnError.isLength(pwd) && ReturnError.isEquals(pwd, rePwd)){
            editor.setEditorPwd(pwd);
        }
        else if(!ReturnError.isEquals(pwd, rePwd)){
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pwdEqualsError.jsp");
            dp.forward(request, response);
            return;
        }
        else if(!ReturnError.isLength(pwd)){
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pwdLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        editor.setEditorName(name);
        editor.setEditorEmail(email);
        editor.setEditorTelephone(telephone);

        PryInsertDao pard = PryFactory.getPryInsertDao();

        try{
            if(!VerifyInfo.isRepeat(editor)){
                if(pard.doEditor(editor)){
                    RequestDispatcher dp = request
                            .getRequestDispatcher("/host/admin/adminHost.jsp");
                    dp.forward(request, response);
                }
            }
            // else
            // {
            // response.sendRedirect("/sys/*.jsp");
            // }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
