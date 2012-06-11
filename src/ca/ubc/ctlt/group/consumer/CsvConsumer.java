package ca.ubc.ctlt.group.consumer;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVWriter;

import ca.ubc.ctlt.group.Consumer;
import ca.ubc.ctlt.group.Group;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.User;

public class CsvConsumer extends Consumer {
	public static String NAME = "CSV";
	public static String DESCRIPTION = "Exporting group to CSV file.";
	
	@Override
	public void setGroupSets(HashMap<String, GroupSet> sets) throws Exception {
		if (sets.isEmpty()) {
			log("No group to save!");
			return;
		}
		
		OutputStreamWriter steamWriter = new OutputStreamWriter(response.getOutputStream());
		CSVWriter writer = new CSVWriter(steamWriter);
		List<String[]> data = new ArrayList<String[]>();
		String[] header = {"GroupSet", "Group", "Username"};
		data.add(header);
		
		for (Entry<String, GroupSet> entryGroupSet : sets.entrySet()) {
			GroupSet set = entryGroupSet.getValue();
			String setName = set.getName().equals(GroupSet.EMPTY_NAME) ? "" : set.getName();
			
			for (Entry<String, Group> entryGroup : set.getGroups().entrySet()) {
				Group group = entryGroup.getValue();
			
				for (Entry<String, User> entryMember : group.getMemberList().entrySet()) {
					User user = entryMember.getValue();
					String[] row = {setName, group.getName(), user.getId()};
					data.add(row);
				}
			}
		}

		response.setContentType("text/plain");
		response.addHeader("Content-Disposition","attachment; filename=groups.csv" );
		//response.setContentLength();
		
		writer.writeAll(data);
		writer.close();
	}

}
