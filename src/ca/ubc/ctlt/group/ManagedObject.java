package ca.ubc.ctlt.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ManagedObject {
	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	protected HashMap<String, GroupSet> sets;
	protected List<String> logs = new ArrayList<String>();
	protected List<String> errors = new ArrayList<String>();

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	public String getParam(String name) {
		return request.getParameter(name);
	}
	
	public List<String> getLogs() {
		return logs;
	}

	public List<String> getErrors() {
		return errors;
	}

	public abstract String getOptionsPage();
	
	protected void log(String message) {
		System.out.println(message);
		logs.add(message);
	}
	
	protected void error(String message) {
		System.err.println(message);
		errors.add(message);
	}
	
	public abstract String getName();
	
	public abstract String getDescription();
}
