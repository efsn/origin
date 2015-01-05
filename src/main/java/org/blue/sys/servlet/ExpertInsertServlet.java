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
import com.blue.sys.vo.Expert;
import com.blue.util.ReturnError;
import com.blue.util.VerifyInfo;

public class ExpertInsertServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Expert expert = new Expert();

        String pname = request.getParameter("userName");
        String name = request.getParameter("name");
        String pwd = request.getParameter("expertPW1");
        String rePwd = request.getParameter("expertPW2");
        String title = request.getParameter("title");
        String telephone = request.getParameter("phone");
        String email = request.getParameter("email");
        String remark = request.getParameter("remark");

        if(ReturnError.isLength(pname)){
            expert.setExpert_pname(pname);
        }
        else{
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(ReturnError.isLength(pwd) && ReturnError.isEquals(pwd, rePwd)){
            expert.setExpert_pwd(pwd);
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

        expert.setExpert_name(name);
        expert.setExpert_email(email);
        expert.setExpert_telephone(telephone);
        expert.setExpert_remark(remark);
        expert.setExpert_title(title);

        PryInsertDao pid = PryFactory.getPryInsertDao();

        try{
            if(!VerifyInfo.isRepeat(expert)){
                if(pid.doExpert(expert)){
                    RequestDispatcher dp = request
                            .getRequestDispatcher("/host/admin/adminHost.jsp");
                    dp.forward(request, response);
                }
            }
            else{
                response.sendRedirect("/error/expertNameRepeat.jsp");
            }
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
