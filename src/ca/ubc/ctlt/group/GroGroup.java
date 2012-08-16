package ca.ubc.ctlt.group;

import java.util.HashMap;
import java.util.List;

import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.PersistenceException;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.persistence.PersistenceServiceFactory;

public class GroGroup {
	private String name;
	private String id;
	private HashMap<String, GroUser> memberList;
	
	public GroGroup(String name) {
		memberList = new HashMap<String, GroUser>();
		this.name = name;
	}
	
	public GroGroup(blackboard.data.course.Group bbGroup) throws PersistenceException {
		memberList = new HashMap<String, GroUser>();
		this.name = bbGroup.getTitle();
		this.id = bbGroup.getId().toExternalString();
		BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance()
				.getDbPersistenceManager();
		UserDbLoader userLoader = (UserDbLoader) bbPm.getLoader(UserDbLoader.TYPE);
		List<User> bbUserList = userLoader.loadByGroupId(bbGroup.getId());
		
		for (User u:bbUserList) {
			GroUser user = new GroUser(u);
			addMember(user);
		}
	}
	
	@Override
	public String toString() {
		return "Group [name=" + name + ", id=" + id + ", memberList="
				+ memberList + "]";
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
	public HashMap<String, GroUser> getMemberList() {
		return memberList;
	}
	public void setMemberList(HashMap<String, GroUser> memberList) {
		this.memberList = memberList;
	}
	
	public GroUser getMember(String name) {
		return memberList.get(name);
	}
	
	public void addMember(GroUser user) {
		memberList.put(user.getUserName(), user);
	}
	
	public boolean hasMember(GroUser user) {
		return getMember(user.getUserName()) != null;
	}
}
