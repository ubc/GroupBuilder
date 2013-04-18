package ca.ubc.ctlt.group.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Moving to servlets resulted in moving all the JSP files into /WEB-INF, which
 * means that JSP files no longer get auto-generated servlets, which in turn
 * means that all the ajax requests we were using are now broken since we
 * can't request individual JSP pages anymore. This servlet fixes that by
 * serving the ajax pages.
 */
public class AjaxServlet extends HttpServlet
{
	/** Auto generated serial */
	private static final long serialVersionUID = 7051425213848942301L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String path = request.getPathInfo();
		String jsp = "";
		
		if (path.equals("/groupcreator/userslist.jsp"))
		{
			jsp = "/WEB-INF/view/providers/groupcreator/userslist.jsp";
		}
		else if (path.equals("/blackboard/groupview.jsp"))
		{
			jsp = "/WEB-INF/view/consumers/blackboard/groupview.jsp";
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// pass on request to process.jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher(jsp);
		// have to check that one of the consumers hasn't redirected already before forwarding the request
		if (dispatcher != null && !response.isCommitted()) 
		{
			dispatcher.forward(request, response);
		}
	}
}
