package org.blue.sys.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blue.sys.dao.proxy.PryInsertDao;
import org.blue.sys.factory.PryFactory;
import org.blue.sys.vo.Expert;
import org.blue.util.ReturnError;
import org.blue.util.VerifyInfo;

public class ExpertInsertServlet extends HttpServlet {
    
    @Override
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
            expert.setExpertPname(pname);
        }
        else{
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(ReturnError.isLength(pwd) && ReturnError.isEquals(pwd, rePwd)){
            expert.setExpertPwd(pwd);
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

        expert.setExpertName(name);
        expert.setExpertEmail(email);
        expert.setExpertTelephone(telephone);
        expert.setExpertRemark(remark);
        expert.setExpertTitle(title);

        PryInsertDao pid = PryFactory.getPryInsertDao();

        try{
            if(!VerifyInfo.isRepeat(expert)){
                if(pid.doExpert(expert)){
                    RequestDispatcher dp = request.getRequestDispatcher("/host/admin/adminHost.jsp");
                    dp.forward(request, response);
                }
            }else{
                response.sendRedirect("/error/expertNameRepeat.jsp");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
