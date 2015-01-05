package com.blue.sys.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.blue.sys.factory.PryFactory;
import com.blue.util.ReturnError;

public class LoginServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        String user = request.getParameter("user");
        String pname = request.getParameter("username");
        String pwd = request.getParameter("password");

        session.setAttribute("news", user);

        if(!ReturnError.isLength(pname)){
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(!ReturnError.isLength(pwd)){
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pwdLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if("author".equals(user) && ReturnError.isLength(pname)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getAuthor();

                if(!map.containsKey(pname)){
                    request.getRequestDispatcher("/error/userNotFound.jsp")
                            .forward(request, response);
                    return;
                }

                if(!map.get(pname).equals(pwd)){
                    request.getRequestDispatcher("/error/wrongPwd.jsp")
                            .forward(request, response);
                    return;
                }

                if(map.containsKey(pname) && pwd.equals(map.get(pname))){
                    session.setAttribute("user", pname);

                    session.setAttribute("update", user);

                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getAuthor(pname));

                    request.getRequestDispatcher("/host/author/authorHost.jsp")
                            .forward(request, response);
                    return;
                }
            } catch(SQLException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }

        if("expert".equals(user) && ReturnError.isLength(pname)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getExpert();

                if(!map.containsKey(pname)){
                    request.getRequestDispatcher("/error/userNotFound.jsp")
                            .forward(request, response);
                    return;
                }

                if(!map.get(pname).equals(pwd)){
                    request.getRequestDispatcher("/error/wrongPwd.jsp")
                            .forward(request, response);
                    return;
                }

                if(map.containsKey(pname) && pwd.equals(map.get(pname))){
                    session.setAttribute("user", pname);

                    session.setAttribute("update", user);

                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getExpert(pname));

                    request.getRequestDispatcher("/host/expert/expertHost.jsp")
                            .forward(request, response);
                    return;
                }
            } catch(SQLException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }

        if("editor".equals(user) && ReturnError.isLength(pname)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getEditor();

                if(!map.containsKey(pname)){
                    request.getRequestDispatcher("/error/userNotFound.jsp")
                            .forward(request, response);
                    return;
                }

                if(!map.get(pname).equals(pwd)){
                    request.getRequestDispatcher("/error/wrongPwd.jsp")
                            .forward(request, response);
                    return;
                }

                if(map.containsKey(pname) && pwd.equals(map.get(pname))){
                    session.setAttribute("user", pname);

                    session.setAttribute("update", user);

                    session.setAttribute("list", PryFactory.getPryQueryAllDao()
                            .getEditor(pname));

                    request.getRequestDispatcher("/host/editor/editorHost.jsp")
                            .forward(request, response);
                    return;
                }
            } catch(SQLException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }

        if("admin".equals(user) && ReturnError.isLength(pname)){
            try{
                Map<String, String> map = PryFactory.getPryQueryDao()
                        .getAdmin();

                if(!map.containsKey(pname)){
                    request.getRequestDispatcher("/error/userNotFound.jsp")
                            .forward(request, response);
                    return;
                }

                if(!map.get(pname).equals(pwd)){
                    request.getRequestDispatcher("/error/wrongPwd.jsp")
                            .forward(request, response);
                    return;
                }

                if(map.containsKey(pname) && pwd.equals(map.get(pname))){
                    session.setAttribute("user", pname);

                    session.setAttribute("update", user);

                    request.getRequestDispatcher("/host/admin/adminHost.jsp")
                            .forward(request, response);
                    return;
                }
            } catch(SQLException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
