package ca.ubc.ctlt.group.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import org.apache.commons.fileupload.FileItem;

import blackboard.base.InitializationException;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.platform.BbServiceException;
import au.com.bytecode.opencsv.CSVReader;
import ca.ubc.ctlt.group.*;

public class CsvProvider extends Provider {

	@Override
	public HashMap<String, GroupSet> getGroupSets() 
			throws KeyNotFoundException, ConnectionNotAvailableException, PersistenceException, InitializationException, BbServiceException {
		if (null == request) {
			System.err.println("Request object is empty!");
			return null;
		}
		
		if( processFile()) {
			return sets;
		}
		
		return null;
	}

	private boolean processFile() 
			throws KeyNotFoundException, ConnectionNotAvailableException, PersistenceException, InitializationException, BbServiceException {
		sets = new HashMap<String, GroupSet>();
		
		FileItem file = ((UploadMultipartRequestWrapper) request)
				.getFile("csvfile");

		CSVReader reader;
		try {
			reader = new CSVReader(new InputStreamReader(file.getInputStream()));
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
					group.addMember(user);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

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
		return "Providing group information from CSV file.";
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
