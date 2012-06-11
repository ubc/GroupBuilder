package ca.ubc.ctlt.group;

import java.util.HashMap;

public class GroupSet {
	public static final String EMPTY_NAME = "##DEFAULT##";
	private String name;
	private String id;
	private HashMap<String, Group> groups;
	
	public GroupSet(String name) {
		groups = new HashMap<String, Group>();
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		return "GroupSet [name=" + name + ", id=" + id + ", groups=" + groups
				+ "]";
	}


	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public HashMap<String, Group> getGroups() {
		return groups;
	}
	
	public void setGroups(HashMap<String, Group> groups) {
		this.groups = groups;
	}
	
	public Group getGroup(String name) {
		return groups.get(name);
	}
	
	public void addGroup(Group group) {
		groups.put(group.getName(), group);
	}
}
