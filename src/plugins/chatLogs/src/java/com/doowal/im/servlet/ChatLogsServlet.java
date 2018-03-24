package com.doowal.im.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;

import com.doowal.im.plugin.ChatLogsPlugin;

public class ChatLogsServlet extends HttpServlet {
	 
    private static final long serialVersionUID = 6981863134047161005L;
   //private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    //private static final ObjectMapper mapper = new ObjectMapper();
  //  private static DbChatLogsManager logsManager;
 
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        //  logsManager = DbChatLogsManager.getInstance();
        
        // 取消权限验证，不登陆即可访问
        AuthCheckFilter.addExclude("chatlogs");
        System.out.println("ChatLogsServlet inited!");
        AuthCheckFilter.addExclude("chatlogs/chatlogsservlet");
        AuthCheckFilter.addExclude("chatlogs/login.jsp");
    }
 
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/html;charset=utf-8");
        //response.getWriter();
        String roomName = request.getParameter("roomName");
        if(roomName!=null&&"".equals(roomName)){
        	ChatLogsPlugin.setJid(roomName);
        }
        // ChatLogsPlugin.setJid("lhp@conference.192.168.2.135");
        response.sendRedirect("login.jsp?decorator=none");
    }
 
    @Override
    public void destroy() {
        super.destroy();
        // Release the excluded URL
        AuthCheckFilter.removeExclude("chatlogs/chatlogsservlet");
        AuthCheckFilter.removeExclude("chatlogs");
    }
 
}
