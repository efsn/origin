package org.blue.sys.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blue.sys.factory.PryFactory;
import org.blue.sys.vo.EssayType;
import org.blue.util.VerifyInfo;

public class TypeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        String typeName = request.getParameter("typeName");
        EssayType essayType = new EssayType();

        if(null != typeName && !"".equals(typeName)){
            essayType.setType_name(typeName);
            try{
                if(!VerifyInfo.isNotRepeat(typeName)){
                    response.sendRedirect("/sys/error/typeNameRepeat.jsp");
                }
                else{
                    if(PryFactory.getPryTypeDao().typeInsert(essayType)){
                        response.sendRedirect("/sys/host/typeAdmin.jsp");
                    }
                    else{
                        response.sendRedirect("/sys/error/typeInsertError.jsp");
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
