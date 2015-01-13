package org.blue.sys.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.blue.sys.dao.proxy.PryInsertDao;
import org.blue.sys.factory.PryFactory;
import org.blue.sys.vo.Author;
import org.blue.util.ReturnError;
import org.blue.util.VerifyInfo;

public class AuthorInsertServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        Author author = new Author();

        String pname = request.getParameter("userName");
        String pwd = request.getParameter("authorPW1");
        String rePwd = request.getParameter("authorPW2");
        String name = request.getParameter("authorName");
        String address = request.getParameter("address");
        String telephone = request.getParameter("phone");
        String email = request.getParameter("email");
        String mark = request.getParameter("mark");

        String path = "E:/EclipseWorkspace/CTBTSystem/essayFiles/" + pname;
        File file = new File(path);

        HttpSession session = request.getSession();
        session.setAttribute("user", pname);

        if(ReturnError.isLength(pname)){
            author.setAuthorPname(pname);
        }
        else{
            RequestDispatcher dp = request
                    .getRequestDispatcher("/error/pnameLengthError.jsp");
            dp.forward(request, response);
            return;
        }

        if(ReturnError.isLength(pwd) && ReturnError.isEquals(pwd, rePwd)){
            author.setAuthorPwd(pwd);
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

        author.setAuthorName(name);
        author.setAuthorAddress(address);
        author.setAuthorEmail(email);
        author.setAuthorTelephone(telephone);
        author.setAuthorMark(mark);

        PryInsertDao pid = PryFactory.getPryInsertDao();

        try{
            if(!VerifyInfo.isRepeat(author)){
                if(pid.doAuthor(author)){
                    file.mkdir();
                    RequestDispatcher dp = request
                            .getRequestDispatcher("host/author/authorHost.jsp");
                    dp.forward(request, response);
                }
            }
            else{
                response.sendRedirect("/sys/error/pnameRepeat.jsp");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }

}
