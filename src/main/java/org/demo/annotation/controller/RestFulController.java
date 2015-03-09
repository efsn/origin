package org.demo.annotation.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RestFulController{
    
    @RequestMapping(value="/ann/rest.do", headers="Content-Type=application/json")
    public String request(HttpServletRequest req) throws IOException{
        InputStream in = req.getInputStream();
        byte[] data = new byte[req.getContentLength()];
        in.read(data);
        String json = new String(data, req.getCharacterEncoding());
        System.out.println("json:" + json);
        return "rest";
    }
    
    @RequestMapping(value="/ann/resp.do", headers="Content-Type=application/json")
    public String request(HttpServletResponse resp) throws IOException{
        
        resp.setContentType("text/html;charset=utf8");
        resp.getWriter().write("<font style='color:red'>Fuck</font>");
        return null;
    }
    
    @RequestMapping(value="/ann/xml.do", headers="Accept=application/xml")
    public String requestXml(HttpServletResponse resp) throws IOException{
        resp.setContentType("application/xml;charset=utf8");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml += "<user><username>chan</username></user>";
        resp.getWriter().write(xml);
        return null;
    }
    
    public static void main(String[] args) throws Exception{
        String url = "http://localhost:8077/origin/ann/rest.do";
        ClientHttpRequest req = new SimpleClientHttpRequestFactory().createRequest(new URI(url), HttpMethod.POST);
        req.getHeaders().set("Content-Type", "application/json;charset=utf8");
        String json = "{\"username\":\"zhang\",\"password\":\"123\"}";
        req.getBody().write(json.getBytes("utf8"));
        System.out.println(req.execute().getStatusCode());
    }
    
}
