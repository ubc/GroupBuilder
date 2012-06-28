package ca.ubc.ctlt.group;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import blackboard.platform.filesystem.MultipartRequest;
import blackboard.platform.filesystem.UploadUtil;

public class UploadMultipartRequestWrapper extends HttpServletRequestWrapper{
	
	private MultipartRequest mReq;
	
	public UploadMultipartRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		mReq = UploadUtil.processUpload(request);
	}

	@Override
	public String getParameter(String name) {
		return mReq.getParameter(name);
	}
	
	public File getFile(String name){
		return mReq.getFile(name);
	}

	@Override
	public Map<String, List<String>> getParameterMap() {
		return mReq.getParameterMap();
	}

	@Override
	public String[] getParameterValues(String name) {
		return mReq.getParameterValues(name);
	}
	
	public File getFileFromParameterName(String name) {
		return mReq.getFileFromParameterName(name);
	}
}