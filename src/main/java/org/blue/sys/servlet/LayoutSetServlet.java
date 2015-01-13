package org.blue.sys.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blue.sys.factory.PryFactory;

public class LayoutSetServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) {
        String essayId = request.getParameter("essayId");
        String publish_time = request.getParameter("publish_time");
        String publish_money = request.getParameter("publish_money");
        String ispay = request.getParameter("ispay");

        Map<String, String> map = new HashMap<String, String>();

        if(null != essayId && !"".equals(essayId)){
            map.put("essayId", essayId);
        }
        if(null != publish_time && !"".equals(publish_time)){
            map.put("publish_time", publish_time);
        }
        if(null != publish_money && !"".equals(publish_money)){
            publish_money += "Ԫ";
            map.put("publish_money", publish_money);
        }
        if(null != ispay && !"".equals(ispay)){
            map.put("ispay", ispay);
        }

        try{
            if(PryFactory.getPryQueryAllDao().getEssayCheckedByAdmin().keySet()
                    .contains((Integer.parseInt(essayId)))){
                if(PryFactory.getPryInfoUpdateDao().doPublishUpdate(map)){
                    response.sendRedirect("/sys/host/layoutSet.jsp");
                }
            }
            else{
                response.sendRedirect("/sys/error/essayError.jsp");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        doGet(request, response);
    }
}
