package ca.ubc.ctlt.group.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.ubc.ctlt.group.Consumer;
import ca.ubc.ctlt.group.Manager;
import ca.ubc.ctlt.group.Provider;

public class IndexServlet extends HttpServlet
{
	/** Auto generated serial */
	private static final long serialVersionUID = 212295007347983026L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		Manager manager =  new Manager();

		ArrayList<Provider> providers = new ArrayList<Provider>();
		ArrayList<Consumer> consumers = new ArrayList<Consumer>();

		try
		{
			for (Class<Provider> p : manager.getProviders())
			{
				providers.add(p.newInstance());
			}

			for (Class<Consumer> c : manager.getConsumers())
			{
				consumers.add(c.newInstance());
			}
		} catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		request.setAttribute("providers", providers);
		request.setAttribute("consumers", consumers);
		request.setAttribute("courseId", request.getParameter("course_id"));
		
		// pass on request to index.jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/view/index.jsp");
		if (dispatcher != null) 
		{
			dispatcher.forward(request, response);
		}
	}
}
