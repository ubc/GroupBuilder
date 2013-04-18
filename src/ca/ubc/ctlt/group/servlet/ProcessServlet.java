package ca.ubc.ctlt.group.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.ubc.ctlt.group.Manager;
import ca.ubc.ctlt.group.UploadMultipartRequestWrapper;

public class ProcessServlet extends HttpServlet
{
	/** Auto generated serial */
	private static final long serialVersionUID = 4673192559069595348L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		// Special processing for file uploads. File uploads are marked as a multipart request.
		if (request.getHeader("content-type") != null && 
			request.getHeader("content-type").indexOf("multipart/form-data") != -1)
		{
			request = new UploadMultipartRequestWrapper(request);
		}
		
		Manager manager = new Manager(request, response);
		manager.process();
		request.setAttribute("manager", manager);
		request.setAttribute("errors", manager.getErrors());
		
		// pass on request to process.jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/view/process.jsp");
		// have to check that one of the consumers hasn't redirected already before forwarding the request
		if (dispatcher != null && !response.isCommitted()) 
		{
			dispatcher.forward(request, response);
		}
	}
}
