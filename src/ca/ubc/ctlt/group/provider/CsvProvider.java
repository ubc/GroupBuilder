package ca.ubc.ctlt.group.provider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import com.xythos.common.api.XythosException;

import blackboard.base.InitializationException;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.platform.BbServiceException;
import blackboard.cms.filesystem.CSContext;
import blackboard.cms.filesystem.CSEntry;
import blackboard.cms.filesystem.CSFile;
import au.com.bytecode.opencsv.CSVReader;
import ca.ubc.ctlt.group.*;

public class CsvProvider extends Provider {

	@Override
	public HashMap<String, GroupSet> getGroupSets() 
			throws KeyNotFoundException, ConnectionNotAvailableException, PersistenceException, InitializationException, BbServiceException, XythosException, FileNotFoundException {
		if (null == request) {
			System.err.println("Request object is empty!");
			return null;
		}
		
		if( processFile()) {
			return sets;
		}
		
		return null;
	}

	/**
	 * There's two sources for a CSV import: A local file (one uploaded from the user's computer) and the content
	 * collections system. Once we have the csv in hand, we can pass it off to the parser, which expects the csv to
	 * be bundled in a Reader.
	 * 
	 * The local file can be passed off to the CSV parser just by grabbing the location of the uploaded file on the server.
	 * 
	 * The content system file needs to be first read into an output buffer, then converted into a string, then finally
	 * into a reader.
	 * 
	 * @return
	 * @throws KeyNotFoundException
	 * @throws InitializationException
	 * @throws ConnectionNotAvailableException
	 * @throws PersistenceException
	 * @throws BbServiceException
	 * @throws XythosException
	 * @throws FileNotFoundException
	 */
	private boolean processFile() 
			throws KeyNotFoundException, InitializationException, ConnectionNotAvailableException, PersistenceException, BbServiceException, XythosException, FileNotFoundException
	{
		UploadMultipartRequestWrapper mreq = (UploadMultipartRequestWrapper) request;
		log("We successfully got a multipart request.");
		String uploadtype = mreq.getParameter("csvfile_attachmentType");
		log("MulitpartRequest parsing successful!");
		if (uploadtype.equals("L"))
		{ // a local file is a file uploaded from the user's computer
			log("Processing a local file.");
			File file = mreq.getFileFromParameterName("csvfile_LocalFile0");
			parseCSV(new FileReader(file));
		}
		else
		{ // this means we have to get a file from the content system
			log("Processing a content collections file.");
			String path = mreq.getParameter("csvfile_CSFilePath");
			CSContext csCtx = null;
			csCtx = CSContext.getContext();
			CSEntry entry = csCtx.findEntry(path); 
			CSFile file = (CSFile) entry;
			// to get the file content, I need to pass in an output stream for
			// the CSFile to write into, then I need to convert that output stream
			// into a Reader
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			file.getFileContent(stream);
			
			parseCSV(new StringReader(stream.toString()));
		}
		log("We're done!");

		return true;
	}
	
	/**
	 * Parse the CSV import. 
	 * 
	 * @param csvfile
	 * @return
	 * @throws KeyNotFoundException
	 * @throws InitializationException
	 * @throws ConnectionNotAvailableException
	 * @throws PersistenceException
	 * @throws BbServiceException
	 */
	private boolean parseCSV(Reader csvfile) throws KeyNotFoundException, InitializationException, ConnectionNotAvailableException, PersistenceException, BbServiceException
	{
		CSVReader reader;
		sets = new HashMap<String, GroupSet>();
		try {
			reader = new CSVReader(csvfile);
			String[] nextLine;
			reader.readNext(); // ignore the first line since it's the header
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].trim().isEmpty()) {
					// use GroupSet.EMPTY_NAME for group set name for the file without group set
					nextLine[0] = GroupSet.EMPTY_NAME;
				}
				
				GroupSet set = sets.get(nextLine[0].trim());
				if (set == null) {
					set = new GroupSet(nextLine[0].trim());
					sets.put(nextLine[0], set);
				}
				
				Group group = set.getGroup(nextLine[1].trim());
				if (group == null) {
					group = new Group(nextLine[1].trim());
					set.addGroup(group);
				}
				
				GroUser user = group.getMember(nextLine[2].trim());	
				if (user == null) {
					user = new GroUser(nextLine[2].trim(), request);
					log(user.getStudentID());
					group.addMember(user);
				}
			}
		} catch (IOException e) {
			log("Unable to read CSV file: " + e.getMessage());
			return false;
		}
		log("CSV parsing successful!");
		return true;
	}

	@Override
	public String getOptionsPage()
	{
		return "providers/csv/options.jsp";
	}

	@Override
	public String getName()
	{
		return "CSV";
	}

	@Override
	public String getDescription()
	{
		return "Reads in group information from a CSV file.";
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.ctlt.group.Provider#hasFileUpload()
	 */
	@Override
	public boolean hasFileUpload()
	{
		return true;
	}
}
