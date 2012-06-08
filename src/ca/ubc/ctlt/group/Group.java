package ca.ubc.ctlt.group;

import java.util.HashMap;
import java.util.List;

import blackboard.persist.BbPersistenceManager;
import blackboard.persist.PersistenceException;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.persistence.PersistenceServiceFactory;

public class Group {
	private String name;
	private String id;
	private HashMap<String, User> memberList;
	
	public Group(String name) {
		memberList = new HashMap<String, User>();
		this.name = name;
	}
	
	public Group(blackboard.data.course.Group bbGroup) throws PersistenceException {
		memberList = new HashMap<String, User>();
		this.name = bbGroup.getTitle();
		this.id = bbGroup.getId().toExternalString();
		BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance()
				.getDbPersistenceManager();
		UserDbLoader userLoader = (UserDbLoader) bbPm.getLoader(UserDbLoader.TYPE);
		List<blackboard.data.user.User> bbUserList = userLoader.loadByGroupId(bbGroup.getId());
		
		for (blackboard.data.user.User u:bbUserList) {
			User user = new User();
			user.setId(u.getUserName());
			user.setName(u.getGivenName() + " " + u.getFamilyName());
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
	public HashMap<String, User> getMemberList() {
		return memberList;
	}
	public void setMemberList(HashMap<String, User> memberList) {
		this.memberList = memberList;
	}
	
	public User getMember(String name) {
		return memberList.get(name);
	}
	
	public void addMember(User user) {
		memberList.put(user.getId(), user);
	}
}
