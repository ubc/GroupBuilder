package ca.ubc.ctlt.group.consumer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import blackboard.cms.filesystem.CSContext;
import blackboard.cms.filesystem.CSFileSystemException;

import au.com.bytecode.opencsv.CSVWriter;

import ca.ubc.ctlt.group.Consumer;
import ca.ubc.ctlt.group.Group;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.GroUser;

public class CsvConsumer extends Consumer {
	// reusable buffer for stripping control characters, provides a mild speedup
    private char[] oldChars = new char[10];
    
	@Override
	public void setGroupSets(HashMap<String, GroupSet> sets) throws Exception {
		if (sets.isEmpty()) {
			log("No group to save!");
			return;
		}
		
		// process the data
		List<String[]> data = new ArrayList<String[]>();
		String[] header = {"GroupSet", "Group", "Student ID"};
		data.add(header);
		
		for (Entry<String, GroupSet> entryGroupSet : sets.entrySet()) {
			GroupSet set = entryGroupSet.getValue();
			String setName = set.getName().equals(GroupSet.EMPTY_NAME) ? "" : set.getName();
			
			for (Entry<String, Group> entryGroup : set.getGroups().entrySet()) {
				Group group = entryGroup.getValue();
			
				for (Entry<String, GroUser> entryMember : group.getMemberList().entrySet()) {
					GroUser user = entryMember.getValue();
					String[] row = {setName, group.getName(), user.getStudentID()};
					data.add(row);
				}
			}
		}
		
		// find out if the user wants to download the file or save it to the content collection system
		String op = request.getParameter("csvExportOperation");
		if (op.equals("download"))
		{
			downloadCSV(data);
		}
		else
		{
			saveToCS(data);
		}
	}
	
	/**
	 * Save the CSV data as a file in the Content Collection System
	 * @param data
	 * @throws IOException 
	 */
	private void saveToCS(List<String[]> data) throws IOException
	{
		log("Saving to Content Collection");
		// Process the data into a CSV format. We use the CSVWriter to write to an OutputStream
		// which we later put into an InputStream for the CSContext to use in file creation.
		ByteArrayOutputStream csvOutput = new ByteArrayOutputStream();
		OutputStreamWriter streamWriter = new OutputStreamWriter(csvOutput, "UTF-8");
		CSVWriter writer = new CSVWriter(streamWriter); // saves us the trouble of creating CSVs ourselves
		writer.writeAll(data);
		writer.close();
		ByteArrayInputStream input = new ByteArrayInputStream(csvOutput.toByteArray());
		
		try 
		{
			String fileName = request.getParameter("csvExportName");
			String path = request.getParameter("csvExportCSFolder_CSFile");
			CSContext csCtx = CSContext.getContext();
			csCtx.createFile(path, fileName, input, true);
		}
		catch (CSFileSystemException e) 
		{
			// Couldn't create the file for some reason
			error("Couldn't creat the file, did you select a directory?");
			error(e.getMessage());
		}
	}
	
	/**
	 * Tell the User's browser to expect a file, then take the line by line CSV data and write it 
	 * to the response stream, which means the user downloads the CSV data as a file.
	 * 
	 * @param data
	 * @throws IOException
	 */
	private void downloadCSV(List<String[]> data) throws IOException 
	{
		log("Downloading Export");
		byte[] rawFileName = request.getParameter("csvExportName").getBytes();
		// IE8 and below doesn't support UTF-8 filenames, downconvert to latin-1
		String fileName = new String(rawFileName, "ISO-8859-1");
		// add the .csv extension if it's not there
		if (!fileName.endsWith(".csv")) {
			fileName += ".csv";
		}
		// strip non-printable characters & http header special characters
		fileName = sanitizeFileName(fileName);

		OutputStreamWriter streamWriter = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		CSVWriter writer = new CSVWriter(streamWriter);
		
		response.setContentType("text/plain");
		// If only IE8 & below supported standards that every single other browser supports, we'd
		// be able to use the more elegant solution:
		//		Content-Disposition: attachment; filename*=UTF-8''some_file_name_here
		// Which, once URLEncoded, has no problems with special characters in filenames.
		response.addHeader("Content-Disposition","attachment; filename=\"" + fileName + '"');
		//response.setContentLength();
		
		writer.writeAll(data);
		writer.close();
	}

    /**
     * Strips unprintable characters and other special characters that might allow the user to
     * manipulate the http header.
     * 
     * Code from:
     * http://stackoverflow.com/questions/7161534/fastest-way-to-strip-all-non-printable-characters-from-a-java-string
     * @param s
     * @return
     * @throws Exception
     */
    private String sanitizeFileName(String s) {
        final int length = s.length();
        if (oldChars.length < length) {
            oldChars = new char[length];
        }
        s.getChars(0, length, oldChars, 0);
        int newLen = 0;
        for (int j = 0; j < length; j++) {
            char ch = oldChars[j];
            if (ch >= ' ' && ch != ';') {
                oldChars[newLen] = ch;
                newLen++;
            }
        }
        if (newLen != length) {
            return new String(oldChars, 0, newLen);
        } else {
            return s;
        }
    }

	@Override
	public String getOptionsPage()
	{
		return "consumers/csv/csvconsumers.jsp";
	}

	@Override
	public String getName()
	{
		return "CSV";
	}

	@Override
	public String getDescription()
	{
		return "Exports group information to a CSV file.";
	}

}
