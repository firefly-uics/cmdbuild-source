package org.cmdbuild.shark.util;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.SharkConnection;

public class ActivityPerformerResolverServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ActivityPerformerResolver apr = null;
	
	protected ActivityPerformerResolver getResolver() {
		if(apr == null) {
			apr = new ActivityPerformerResolver();
		}
		return apr;
	}
	
	String user;
	String password;
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		user = this.getServletConfig().getInitParameter("user");
		password = this.getServletConfig().getInitParameter("password");
		System.out.println("ActivityPerformerServlet configured: " + user + ", " + password);
	}
	
	protected String getUser() {
		return user;
	}
	protected String getPassword() {
		return password;
	}
	
	private boolean checkAuth(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String auth = req.getHeader("Authorization");
			if(auth == null) {
				resp.setHeader("WWW-Authenticate", "Basic realm=\"shark\"");
				resp.sendError(401); //unauthorized
				return false;
			}
			
			if(!"Basic".equalsIgnoreCase(auth.substring(0, 5))) {
				resp.sendError(400); //unsupported authentication method, so bad request
				return false;
			}
			
			String authInfo = "";
			try {
				authInfo = new String(Base64.decode( auth.replace("Basic ", "") ));
			} catch (Base64DecodingException e) {
				resp.sendError(400);
				return false;
			}
			
			String[] toks = authInfo.split(":");
			if( !(getUser().equals(toks[0])) || !(getPassword().equals(toks[1])) ) {
				resp.setHeader("WWW-Authenticate", "Basic realm=\"shark\"");
				resp.sendError(401); //unauthorized
				return false;
			}
			return true;
		} catch(Exception e) {
			System.out.println("error in checkAuth ActivityPerformerResolverServlet!");
			return false;
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if( !checkAuth(req,resp) )
			return;
		
		String procInstId = req.getParameter("processinstanceid");
		String actInstId = req.getParameter("activityinstanceid");
		String expr = req.getParameter("expression");
		
		try {
			System.out.println("ActivityPerformer for " + procInstId + ", " + actInstId + ", " + expr);
			String out = resolve(procInstId,actInstId,expr);
			System.out.println("ActivityPerformer resolved: " + out);
			resp.getWriter().write(out);
			resp.getWriter().flush();
		} catch (Exception e) {;
			System.out.println("cannot resolve performer (cannot write to HttpServletResponse) " + expr);
			resp.sendError(500);
		}
	}
	
	protected String resolve( String procInstId, String actInstId, String expr ) throws Exception {
		Shark shark = Shark.getInstance();
		
		UserTransaction ut = (UserTransaction)new InitialContext().lookup("java:comp/env/UserTransaction");
		ut.begin();
		
		WMConnectInfo connInfo = new WMConnectInfo("admin","enhydra","shark","");
		WAPI wapi = shark.getWAPIConnection();
		WMSessionHandle shandle = wapi.connect(connInfo);
		SharkConnection sconn = shark.getSharkConnection();
		sconn.attachToHandle(shandle);
		
		try {
			return getResolver().resolveArbitraryPerformer(sconn, shandle, procInstId, actInstId, expr);
		} catch(Exception e) {
			System.out.println("cannot resolve performer (error while resolving) " + expr);
			e.printStackTrace();
			throw e;
		} finally {
			try {
				wapi.disconnect(shandle);
				sconn.disconnect();
			} catch(Exception e1) {e1.printStackTrace();}
			ut.rollback();
		}
	}

}
