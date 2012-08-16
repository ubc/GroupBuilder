package ca.ubc.ctlt.group;

import java.util.HashMap;

public class GroupSet {
	public static final String EMPTY_NAME = "##DEFAULT##";
	private String name;
	private String id;
	private HashMap<String, GroGroup> groups;
	
	public GroupSet(String name) {
		groups = new HashMap<String, GroGroup>();
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
	
	public HashMap<String, GroGroup> getGroups() {
		return groups;
	}
	
	public void setGroups(HashMap<String, GroGroup> groups) {
		this.groups = groups;
	}
	
	public GroGroup getGroup(String name) {
		return groups.get(name);
	}
	
	public void addGroup(GroGroup group) {
		groups.put(group.getName(), group);
	}
}
