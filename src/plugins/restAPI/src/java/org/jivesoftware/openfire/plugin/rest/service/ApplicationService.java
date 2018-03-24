package org.jivesoftware.openfire.plugin.rest.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("restapi/v1/app")
public class ApplicationService {
	@GET
	@Path("/appMarket")
	public void appMarket(@Context HttpServletResponse response) throws IOException{
		response.sendRedirect("/plugins/restapi/view/app_market.jsp?decorator=none");
	}
	@GET
	@Path("/onlineGame")
	public void onlineGame(@Context HttpServletResponse response) throws IOException{
		response.sendRedirect("/plugins/restapi/view/online_game.jsp?decorator=none");
	}
}
