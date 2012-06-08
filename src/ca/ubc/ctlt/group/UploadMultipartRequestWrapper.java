package ca.ubc.ctlt.group;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadMultipartRequestWrapper extends HttpServletRequestWrapper{
	
	private Map<String,String> formParameters;
	private Map<String,FileItem> fileParameters;
	
	public UploadMultipartRequestWrapper(HttpServletRequest request) {
		super(request);
		try{
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
		
			List fileItems = upload.parseRequest(request);
			formParameters = new HashMap<String,String>();
			fileParameters = new HashMap<String,FileItem>();	
		
			for(int i=0;i<fileItems.size();i++){
				FileItem item = (FileItem)fileItems.get(i);
				if(item.isFormField() == true){
					formParameters.put(item.getFieldName(),item.getString());	
				}else{
                    fileParameters.put(item.getFieldName(),item);   
                    request.setAttribute(item.getFieldName(),item);
				}	
			}
		}catch(FileUploadException fe){
			//Request Timed out.
			//Do some logging
			//...
		}catch(Exception ne){
			throw new RuntimeException(ne);
		}
	}

	@Override
	public String getParameter(String name) {
		if(formParameters.get(name) != null){
			return formParameters.get(name);
		}
		if(fileParameters.get(name) != null){
			return "file";
		}
		return null;
	}
	
	public Enumeration getFileNames(){
		return Collections.enumeration(fileParameters.keySet());
	}

	public FileItem getFile(String name){
		return fileParameters.get(name);
	}

	@Override
	public Map getParameterMap() {
		return formParameters;
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(formParameters.keySet());
	}

	@Override
	public String[] getParameterValues(String arg0) {
		String[] values = new String[formParameters.size()];
		Iterator<String> iter = formParameters.values().iterator();
		for(int i=0;i<values.length;i++){
			values[i] = iter.next();
		}
		
		return values;
	}
	

	
	
	
	
}