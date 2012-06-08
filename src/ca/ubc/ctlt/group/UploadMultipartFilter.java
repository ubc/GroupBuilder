package ca.ubc.ctlt.group;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class UploadMultipartFilter implements Filter{

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
							throws IOException, ServletException {
	
		HttpServletRequest hRequest = (HttpServletRequest)request;
		
		//Check whether we're dealing with a multipart request
		boolean isMultipart = (hRequest.getHeader("content-type") != null && 
							   hRequest.getHeader("content-type").indexOf("multipart/form-data") != -1); 
  
		if(isMultipart == false){
			chain.doFilter(request,response);
		}else{
			//We're dealing with a multipart request - we have to wrap the request.
			UploadMultipartRequestWrapper wrapper = new UploadMultipartRequestWrapper(hRequest);
			chain.doFilter(wrapper,response);
		}
	}

	public void destroy() {}
	public void init(FilterConfig config) throws ServletException {}
	
}