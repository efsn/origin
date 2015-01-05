package com.blue.sys.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.blue.sys.factory.PryFactory;

public class FindPWDServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        String pname = request.getParameter("pname");
        String user = request.getParameter("user");

        List<String> list = new ArrayList<String>();

        try{
            if(null != pname && !"".equals(pname)){
                list = PryFactory.getPryQueryDao().getPwd(pname, user);

                request.setAttribute("list", list);
                if("author".equals(user)){
                    user = "Ͷ����";
                }
                if("editor".equals(user)){
                    user = "�༭";
                }
                if("expert".equals(user)){
                    user = "ר��";
                }
                if("admin".equals(user)){
                    user = "����";
                }
                request.setAttribute("user", user);

                if(2 == list.size()){
                    request.getRequestDispatcher("/findPassword.jsp").forward(
                            request, response);
                }
                else{
                    response.sendRedirect("error/notUser.jsp");
                }
            }
            else{
                response.sendRedirect("error/notUser.jsp");
            }
        } catch(SQLException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        } catch(ServletException e){
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
