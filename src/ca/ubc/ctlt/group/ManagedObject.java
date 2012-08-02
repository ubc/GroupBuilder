package ca.ubc.ctlt.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;

public abstract class ManagedObject {
	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	protected Map<String, GroupSet> sets;
	protected List<String> logs = new ArrayList<String>();
	protected List<String> errors = new ArrayList<String>();
	protected Manager manager = null;
	private static final LogService LOG = LogServiceFactory.getInstance();
	
	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

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
		LOG.logDebug(message);
		logs.add(message);
	}
	
	protected void error(String message) {
		LOG.logError(message);
		errors.add(message);
	}
	
	public abstract String getName();
	
	public abstract String getDescription();
}
