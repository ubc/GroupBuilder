package ca.ubc.ctlt.group;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

import blackboard.base.InitializationException;
import blackboard.data.user.User;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.BbServiceException;
import blackboard.platform.context.Context;
import blackboard.platform.db.JdbcServiceFactory;

public class GroUser {
	User user;
	
	public GroUser(User user) {
		this.user = user;
	}
	
	/**
	 * Given a student id, load the user object by searching the class list for
	 * a user with the matching student id.
	 * 
	 * @param studentId
	 * @param request
	 * @throws InitializationException
	 * @throws BbServiceException
	 * @throws KeyNotFoundException
	 * @throws PersistenceException
	 * @throws ConnectionNotAvailableException
	 */
	public GroUser(String studentId, HttpServletRequest request) throws InitializationException, BbServiceException, ConnectionNotAvailableException, KeyNotFoundException, PersistenceException {
		BlackboardUtil bbutil = new BlackboardUtil(request);
		Context ctx = bbutil.getContext();
		Connection db = JdbcServiceFactory.getInstance().getDefaultDatabase()
				.getConnectionManager().getConnection();
		ArrayList<User> users = UserDbLoader.Default.getInstance().loadByCourseId(ctx.getCourseId(), db, true);
		
		for (User u : users)
		{
			if (u.getStudentId().equals(studentId))
			{
				this.user = u;
				break;
			}
		}
	}
	
	public GroUser(String username) throws ConnectionNotAvailableException, KeyNotFoundException, PersistenceException {
		Connection db = JdbcServiceFactory.getInstance().getDefaultDatabase()
				.getConnectionManager().getConnection();
		this.user = UserDbLoader.Default.getInstance().loadByUserName(username, db, true);
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
