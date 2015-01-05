package com.blue.sys.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.blue.sys.dao.proxy.PryInsertDao;
import com.blue.sys.factory.PryFactory;
import com.blue.sys.vo.Editor;
import com.blue.util.ReturnError;
import com.blue.util.VerifyInfo;

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
            editor.setEditor_pname(pname);
        }
        else{
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(ReturnError.isLength(pwd) && ReturnError.isEquals(pwd, rePwd)){
            editor.setEditor_pwd(pwd);
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

        editor.setEditor_name(name);
        editor.setEditor_email(email);
        editor.setEditor_telephone(telephone);

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
        } catch(SQLException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
