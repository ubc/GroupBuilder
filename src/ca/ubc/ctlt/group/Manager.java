package ca.ubc.ctlt.group;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Manager {
	private Provider provider = null;
	private Consumer consumer = null;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	public Manager() {
	}
	
	public Manager(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		Class<?> p, c;
		System.out.println("Using provider: " + getParam("provider"));
		System.out.println("Using consumer: " + getParam("consumer"));
		try {
			p = Class.forName(getParam("provider"));
			provider = (Provider) p.newInstance();
			c = Class.forName(getParam("consumer"));
			consumer = (Consumer) c.newInstance();
		} catch (ClassNotFoundException e) {
			System.err.println("Class " + getParam("provider") + " or " + getParam("consumer") + " not found!");
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("Initializing  " +  getParam("provider") + " or " + getParam("consumer") + " failed!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("Illegal access to  " +  getParam("provider") + " or " + getParam("consumer") + "!");
			e.printStackTrace();
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
	
	public String getParam(String name) {
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
			HashMap<String, GroupSet> sets = provider.getGroupSets();
			if (sets != null) {
				System.out.println("Got group sets from provider:" + sets);
				consumer.setGroupSets(sets);

				return;
			}
		} catch (Exception e) {
			System.err.println("Setting group failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public Class<Provider>[] getProviders() {
		Class<Provider>[] classes = null;
		
		try {
			classes = getClasses("ca.ubc.ctlt.group.provider");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return classes;
	}
	
	public Class<Consumer>[] getConsumers() {
		Class<Consumer>[] classes = null;
		
		try {
			classes = getClasses("ca.ubc.ctlt.group.consumer");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6)));
			}
		}
		return classes;
	}

}
