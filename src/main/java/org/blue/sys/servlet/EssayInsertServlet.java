package com.blue.sys.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.blue.sys.factory.PryFactory;
import com.blue.sys.vo.Essay;
import com.blue.util.VerifyInfo;

public class EssayInsertServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        Essay essay = new Essay();
        HttpSession session = request.getSession();

        String author_pname = (String) session.getAttribute("user");
        String typeId = request.getParameter("type_id");
        String ename = request.getParameter("essay_pname");
        String keywords = request.getParameter("essay_keywords");
        String content2 = request.getParameter("essay_content2");
        String authorInfo = request.getParameter("authorInfo");

        essay.setAuthor_pname(author_pname);
        essay.setType_id(Integer.parseInt(typeId));
        essay.setEssay_name(ename);
        essay.setEssay_keywords(keywords);
        essay.setEssay_content2(content2);
        essay.setAuthor_info(authorInfo);

        session.setAttribute("essay", essay);

        try{
            if(!VerifyInfo.isRepeat(essay)){
                if(PryFactory.getPryInsertDao().doEssay(essay)){
                    response.sendRedirect("/sys/host/essay/essayUpload.jsp");
                }
            }
            else{
                response.sendRedirect("/sys/error/essayNameRepeat.jsp");
            }
        } catch(SQLException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
