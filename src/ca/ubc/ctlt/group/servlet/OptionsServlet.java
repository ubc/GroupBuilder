package ca.ubc.ctlt.group.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.ubc.ctlt.group.Manager;

public class OptionsServlet extends HttpServlet
{
	/** Auto generated serial */
	private static final long serialVersionUID = -8726387263666685657L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		Manager manager = new Manager(request, response);
		request.setAttribute("provider", manager.getProvider());
		request.setAttribute("consumer", manager.getConsumer());
		request.setAttribute("enctype", "application/x-www-form-urlencoded");
		if (manager.getProvider().hasFileUpload())
		{
			request.setAttribute("enctype", "multipart/form-data");
		}
		
		request.setAttribute("courseId", request.getParameter("course_id"));
		
		// pass on request to options.jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/view/options.jsp");
		if (dispatcher != null) 
		{
			dispatcher.forward(request, response);
		}
	}
}
