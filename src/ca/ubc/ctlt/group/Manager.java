package ca.ubc.ctlt.group;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class Manager {
	private static final String PARAM_PROVIDER = "provider";
	private static final String PARAM_CONSUMER = "consumer";
	private Provider provider = null;
	private Consumer consumer = null;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;
	private static final LogService LOG = LogServiceFactory.getInstance();
	
	public Manager() {
	}
	
	public Manager(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		Class<?> p, c;
		LOG.logDebug("Using provider: " + getParam(PARAM_PROVIDER));
		LOG.logDebug("Using consumer: " + getParam(PARAM_CONSUMER));
		try {
			p = Class.forName(getParam(PARAM_PROVIDER));
			provider = (Provider) p.newInstance();
			c = Class.forName(getParam(PARAM_CONSUMER));
			consumer = (Consumer) c.newInstance();
		} catch (ClassNotFoundException e) {
			LOG.logError("Class " + getParam(PARAM_PROVIDER) + " or " + getParam(PARAM_CONSUMER) + " not found!", e);
		} catch (InstantiationException e) {
			LOG.logError("Initializing  " +  getParam(PARAM_PROVIDER) + " or " + getParam(PARAM_CONSUMER) + " failed!", e);
		} catch (IllegalAccessException e) {
			LOG.logError("Illegal access to  " +  getParam(PARAM_PROVIDER) + " or " + getParam(PARAM_CONSUMER) + "!", e);
		}		
		
		provider.setRequest(request);
		provider.setResponse(response);
		provider.setManager(this);
		consumer.setRequest(request);
		consumer.setResponse(response);
		consumer.setManager(this);
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Consumer getConsumer() {
		return consumer;
	}

	public void setConsumer(Consumer consumer) {
		this.consumer = consumer;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	public final String getParam(String name) {
		return request.getParameter(name);
	}
	
	public List<String> getLogs() {
		List<String> logs = provider.getLogs();
		logs.addAll(consumer.getLogs());
		return logs;
	}
	
	public List<String> getErrors() {
		List<String> errors = provider.getErrors();
		errors.addAll(consumer.getErrors());
		return errors;
	}
	
	public void process() {
		try {
			Map<String, GroupSet> sets = provider.getGroupSets(new BlackboardUtil(request));
			if (sets != null) {
				LOG.logDebug("Got group sets from provider:" + sets);
				consumer.setGroupSets(sets);
			}
		} catch (Exception e) {
			LOG.logError("Setting group failed: " + e.getMessage(), e);
		}
	}
	
	public Class<Provider>[] getProviders() {
		Class<Provider>[] classes = null;
		
		try {
			classes = getClasses("ca.ubc.ctlt.group.provider");
		} catch (ClassNotFoundException e) {
			LOG.logError("Class not found!", e);
		} catch (IOException e) {
			LOG.logError("Failed to read provider list!", e);
		}
		
		return classes;
	}
	
	public Class<Consumer>[] getConsumers() {
		Class<Consumer>[] classes = null;
		
		try {
			classes = getClasses("ca.ubc.ctlt.group.consumer");
		} catch (ClassNotFoundException e) {
			LOG.logError("Class not found!", e);
		} catch (IOException e) {
			LOG.logError("Failed to read consumer list!", e);
		}
		
		return classes;
	}
	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Class[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class> findClasses(File directory, String packageName)
			throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					assert !file.getName().contains(".");
					classes.addAll(findClasses(file,
							packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class") && !file.getName().endsWith("Test.class")) {
					classes.add(Class.forName(packageName
							+ '.'
							+ file.getName().substring(0,
									file.getName().length() - 6)));
				}
			}
		}
		
		return classes;
	}

}
