package ca.ubc.ctlt.group;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

public class ProgressMonitorFileItemFactory extends DiskFileItemFactory { 
	
	private File temporaryDirectory;
	private WeakReference<HttpServletRequest> requestRef;
	private long requestLength;
	
	public ProgressMonitorFileItemFactory(HttpServletRequest request) {
		super();
		temporaryDirectory = (File)request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
		requestRef = new WeakReference<HttpServletRequest>(request);
		
		String contentLength = request.getHeader("content-length"); 
	
		if(contentLength != null){
			requestLength = Long.parseLong(contentLength.trim());
		}
	}

	public FileItem createItem(String fieldName, String contentType,
							   boolean isFormField, String fileName) {
		
		SessionUpdatingProgressObserver observer = null;
		
		if(isFormField == false) //This must be a file upload.
			observer = new SessionUpdatingProgressObserver(fieldName,fileName);

		ProgressMonitorFileItem item = new ProgressMonitorFileItem(fieldName,contentType, 
																   isFormField,fileName, 
																   DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, 
																   temporaryDirectory,
																   observer,
																   requestLength);
		return item;
	}

	public class SessionUpdatingProgressObserver implements ProgressObserver {
		private String fieldName;
		private String fileName;
		
		public SessionUpdatingProgressObserver(String fieldName, String fileName){
			this.fieldName = fieldName;
			this.fileName = fileName;
		}
		
		public void setProgress(double progress) {
			HttpServletRequest request = requestRef.get();
			if(request != null){
				request.getSession().setAttribute("FileUpload.Progress." + fieldName,progress);
				request.getSession().setAttribute("FileUpload.FileName." + fieldName,fileName);
			}
		}	
	}
}
