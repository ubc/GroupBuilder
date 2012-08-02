package ca.ubc.ctlt.group;

import blackboard.data.user.User;

public class GroUser {
	private User user;
	
	public GroUser(User user) {
		this.user = user;
	}
	
	@Override
	public String toString() {
		return "User [username=" + getUserName() + ", name=" + getName() + "]";
	}

	public String getUserName() {
		return user.getUserName();
	}
	
	public String getStudentID() {
		return user.getStudentId();
	}
	
	public String getName() {
		return user.getGivenName() + " " + user.getFamilyName();
	}
}
