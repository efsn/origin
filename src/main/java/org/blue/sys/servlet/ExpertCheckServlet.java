package com.blue.sys.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.blue.sys.factory.PryFactory;
import com.blue.sys.vo.CheckEssay;

public class ExpertCheckServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession();
        int essayId = Integer
                .parseInt((String) session.getAttribute("essayId"));
        String checkTo = request.getParameter("checkTo");
        String expertName = (String) session.getAttribute("user");
        String essayName = (String) session.getAttribute("essay_name");
        String check = request.getParameter("checkMark");

        if("not".equals(checkTo)){
            check += "---ExpertCheckNotPass";
        }
        if("pass".equals(checkTo)){
            check += "---ExpertCheckPass";
        }

        CheckEssay checkEssay = new CheckEssay();
        checkEssay.setEssay_id(essayId);
        checkEssay.setExpert_name(expertName);
        checkEssay.setCheck_content(check);

        if(null != essayName && !"".equals(essayName)){
            try{
                if(PryFactory.getPryInsertDao().doCheckEssay(checkEssay)){
                    if(PryFactory.getPryEssayCheckDao().checkMarkFromExpert(
                            essayId, check)){
                        response.sendRedirect("/sys/host/expert/expertHost.jsp");
                    }
                }
                else{
                    response.sendRedirect("/sys/error/expertCheckFail.jsp");
                }
            } catch(SQLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }

    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
