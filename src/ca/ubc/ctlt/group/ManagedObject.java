package ca.ubc.ctlt.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public abstract class ManagedObject {
	public static String NAME;
	public static String DESCRIPTION;
	
	protected HttpServletRequest request = null;
	protected HashMap<String, GroupSet> sets;
	protected List<String> logs = new ArrayList<String>();
	protected List<String> errors = new ArrayList<String>();

	public void setRequest(HttpServletRequest request) {
		this.request = request;
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

	public String renderOptions() {
		return "";
	}
	
	protected void log(String message) {
		System.out.println(message);
		logs.add(message);
	}
	
	protected void error(String message) {
		System.err.println(message);
		errors.add(message);
	}
}
