package ca.ubc.ctlt.group;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	public Map<String, String[]> getParameterMap() {
		Map<String, List<String>> paramMap = mReq.getParameterMap();
		Map<String, String[]> ret = new HashMap<String, String[]>();
		for (Entry<String, List<String>> entry : paramMap.entrySet()) {
			String[] values = entry.getValue().toArray(new String[0]);
			ret.put(entry.getKey(), values);
		}
		return ret;
	}

	@Override
	public String[] getParameterValues(String name) {
		return mReq.getParameterValues(name);
	}
	
	public File getFileFromParameterName(String name) {
		return mReq.getFileFromParameterName(name);
	}
}