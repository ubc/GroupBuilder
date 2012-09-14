package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.data.user.User;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.gradebook.LineitemDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.context.CourseContext;

public class SearchUtil
{
	private String debug = "";

	// User id to User object
	private HashMap<Id, User> users = new HashMap<Id, User>();
	// Course membership id to course membership
	private HashMap<Id, CourseMembership> courseMemberships = new HashMap<Id, CourseMembership>();
	// Lineitem id external string to lineitem object
	private HashMap<String, Lineitem> lineitems = new HashMap<String, Lineitem>();
	// CourseMembership id to hash of Lineitem ID to Score
	private HashMap<Id, HashMap<Id, Score>> scores = new HashMap<Id, HashMap<Id, Score>>();
	
	/**
	 * @param ctx
	 * @throws PersistenceException 
	 */
	public SearchUtil(CourseContext ctx) throws PersistenceException
	{
		super();
		
		UserDbLoader userLoader = UserDbLoader.Default.getInstance();
		CourseMembershipDbLoader courseMembershipLoader = CourseMembershipDbLoader.Default.getInstance();
		LineitemDbLoader lineitemLoader = LineitemDbLoader.Default.getInstance();
		
		// create user mappings for lookup using user id
		List<User> tmpUsers = userLoader.loadByCourseId(ctx.getCourseId());
		for (User user : tmpUsers) 
		{
			// Map username to User object
			users.put(user.getId(), user);
		}
		
		// Map coursemembership id to course membership
		List<CourseMembership> tmpMemberships = courseMembershipLoader.loadByCourseId(ctx.getCourseId());
		for (CourseMembership member : tmpMemberships)
		{
			courseMemberships.put(member.getId(), member);
		}
		
		// store scores for lookup using first coursememebershipid and then lineitemid
		List<Lineitem> tmpLineitems = lineitemLoader.loadByCourseId(ctx.getCourseId());
		for (Lineitem item : tmpLineitems)
		{
			lineitems.put(item.getId().toExternalString(), item);
			List<Score> tmpScores = item.getScores();
			for (Score score : tmpScores)
			{
				HashMap<Id, Score> lineitemScores;
				Id courseMembershipId = score.getCourseMembershipId();
				if (scores.containsKey(courseMembershipId))
				{
					lineitemScores = scores.get(courseMembershipId);
				}
				else
				{
					lineitemScores = new HashMap<Id, Score>();
					scores.put(courseMembershipId, lineitemScores);
				}
				lineitemScores.put(item.getId(), score);
			}
		}
	}
	
	/**
	 * Filter out users according to some criteria.
	 * @param fieldId - the grade center field to filter on
	 * @param op - the operator to apply for the filter
	 * @param term - the actual term to use the operator on
	 * @return
	 * @throws PersistenceException
	 * @throws ConnectionNotAvailableException
	 */
	public LinkedHashSet<UserWrapper> search(ArrayList<SearchCriteria> criterias, String combinationOp) throws PersistenceException, ConnectionNotAvailableException
	{
		if (criterias.isEmpty())
		{ // no criteria should be an error
			debug += "Search called with no criteria.\n";
			return new LinkedHashSet<UserWrapper>();
		}
		
		if (criterias.size() == 1)
		{ // only one criteria
			debug += "Search called with one criteria.\n";
			LinkedHashSet<UserWrapper> ret = searchSingleCriteria(criterias.get(0));
			createSearchFields(ret, criterias); // allow display of the search field values
			return ret;
		}
		
		debug += "Search called with multiple criteria.\n";
		debug += "Size Crit 0: " + criterias.size();
		if (combinationOp.equals("or"))
		{ // or means that we combine all the results together
			LinkedHashSet<UserWrapper> ret = new LinkedHashSet<UserWrapper>();
			for (SearchCriteria criteria : criterias)
			{
				LinkedHashSet<UserWrapper> searchRes = searchSingleCriteria(criteria);
				ret.addAll(searchRes); // this is a set union
			}
			createSearchFields(ret, criterias); // allow display of the search field values
			return ret;
		}
		else if (combinationOp.equals("and"))
		{ // and means that the resulting elements must meet all criterias
			LinkedHashSet<UserWrapper> ret = null;
			debug += "Size Crit 1: " + criterias.size();
			for (SearchCriteria criteria : criterias)
			{
				if (ret == null)
				{ // the very first search is a special case for set intersect
					ret = searchSingleCriteria(criteria);
					continue;
				}
				LinkedHashSet<UserWrapper> searchRes = searchSingleCriteria(criteria);
				ret.retainAll(searchRes); // this is a set intersection
			}
			debug += "Size Crit 2: " + criterias.size();
			createSearchFields(ret, criterias); // allow display of the search field values
			return ret;
		}
		
		debug += "Search called with invalid combination operator: " + combinationOp + "\n";
		return new LinkedHashSet<UserWrapper>();
	}
	
	/**
	 * Because the multiple search is done by combining individual searches, each UserWrapper initially
	 * can only knows of at least 1 search field. We have to tell each wrapper about the other fields. 
	 * @param existing
	 * @param newer
	 * @throws PersistenceException 
	 * @throws KeyNotFoundException 
	 */
	private void createSearchFields(LinkedHashSet<UserWrapper> wrappers, ArrayList<SearchCriteria> criterias) throws PersistenceException
	{
		debug += "Size Criterias: " + criterias.size() + "\n";
		
		// for each criteria, update all userwrappers with that criteria's name and value
		for (SearchCriteria criteria : criterias)
		{
			if (criteria.isUserInfoField())
			{ // this is a user info field that is already displayed to the user by default
				debug += "Skipping criteria: " + criteria.getField() + "\n";
				continue;
			}
			for (UserWrapper wrap : wrappers)
			{ // this is a field that needs to be displayed to the user
				debug += "Trying to get values for: " + criteria.getField();
				// get the field
				Lineitem item = lineitems.get(criteria.getField());
				if (item == null)
				{
					debug += "... Can't find lineitem!\n";
					continue;
				}
				Score score = null;
				HashMap<Id, Score> userScores = scores.get(wrap.getMember().getId());
				if (userScores != null)
				{ // was able to find this user's score entries
					score = userScores.get(item.getId());
				}
				if (score != null)
				{ // there is a score entry for this user and lineitem
					wrap.addSearchFields(item.getName(), score.getGrade());
					debug += "... Done! " + item.getName() + " " + score.getGrade() + "\n";
				}
				else
				{ // no score entries found for this user and lineitem
					wrap.addSearchFields(item.getName(), "");
					debug += "... Failed!\n";
				}
			}
		}
	}
	
	/**
	 * Does the search, but only on a single criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws PersistenceException
	 * @throws ConnectionNotAvailableException
	 */
	private LinkedHashSet<UserWrapper> searchSingleCriteria(SearchCriteria criteria) 
			throws PersistenceException, ConnectionNotAvailableException
	{
		String fieldId = criteria.getField();
		String op = criteria.getOp();
		String term = criteria.getTerm();
		
		debug += "Checking field id: " + fieldId + "\n";
		if (GradeCenterUtil.getUserinfoColumns().containsKey(fieldId))
		{
			debug += "Is a user info field\n";
			criteria.setUserInfoField(true);
			return searchSingleCriteriaUserinfo(fieldId, op, term);
		}
		
		return searchSingleCriteriaLineitems(fieldId, op, term);
	}
	
	private LinkedHashSet<UserWrapper> searchSingleCriteriaUserinfo(String fieldId, String op, String term) 
			throws KeyNotFoundException, PersistenceException, ConnectionNotAvailableException
	{
		ArrayList<UserWrapper> users = getUsers();
		LinkedHashSet<UserWrapper> ret = new LinkedHashSet<UserWrapper>();
		for (UserWrapper user : users)
		{
			String target = getValueFromUserInfoField(user, fieldId); // the user's value of the field being searched on
			if (target.isEmpty())
			{
				debug += "Unknown user info field.\n";
				return ret;
			}
			
			if (op.equals("contains"))
			{
				if (target.contains(term) || term.isEmpty())
				{
					ret.add(user);
				}
			}
			else if (op.equals("exactly"))
			{
				if (target.equals(term))
				{
					ret.add(user);
				}
			}
			else if (op.equals("exclude"))
			{
				if (!target.contains(term))
				{
					ret.add(user);
				}
			}
			else if (op.equals("greater"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(target);
					if (left > right)
					{
						ret.add(user);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			
			}
			else if (op.equals("equal"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(target);
					if (left == right)
					{
						ret.add(user);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			}
			else if (op.equals("less"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(target);
					if (left < right)
					{
						ret.add(user);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			}
			else
			{ // invalid operator
				debug += "Invalid operator.\n";
				return new LinkedHashSet<UserWrapper>();
			}
		}
		
		return ret;
	}
	
	private String getValueFromUserInfoField(UserWrapper user, String fieldId)
	{
		if (fieldId.equals("firstname"))
		{
			return user.getGivenName();
		}
		else if (fieldId.equals("lastname"))
		{
			return user.getFamilyName();
		}
		else if (fieldId.equals("studentid"))
		{
			return user.getStudentId();
		}
		else
		{
			return "";
		}
	}

	private LinkedHashSet<UserWrapper> searchSingleCriteriaLineitems(String fieldId, String op, String term) 
			throws KeyNotFoundException, PersistenceException, ConnectionNotAvailableException
	{
		// We need to first find the Lineitem that we're searching on, e.g.: The "Section" Lineitem.
		// Once the Lineitem is found, we need the Scores stored for that item.
		// The Scores is where we apply the search operator and term, it'll hold the actual
		// data such as "101" or "201" for the "Section" Lineitem.
		// If we find a matching Score, we get the CourseMembershipId from the Score (which we'll later use to get
		// user information) and use it as key to store the Score. We still need the Score to get the actual
		// value in the field since we need to redraw the table with values from the searched on column.
		Hashtable<Id, Score> cmIdMatches = new Hashtable<Id, Score>(); // holds the matching CourseMembershipId
		// get the Lineitem holding the GradeCenter field, this is identified by fieldId
		Lineitem item = lineitems.get(fieldId);
		// non-existent Lineitem
		if (item == null)
		{
			debug += "Can't find lineitem\n";
			return new LinkedHashSet<UserWrapper>();
		}
		
		
		// get the Scores associated with the Lineitem, this is the search term
		// we're looking for
		ArrayList<Score> scores = item.getScores();
		for (Score score : scores)
		{
			if (op.equals("contains"))
			{
				if (score.getGrade().contains(term) || term.isEmpty())
				{ // found a match for the search term, add this guy to the matches
					cmIdMatches.put(score.getCourseMembershipId(), score);
				}
			}
			else if (op.equals("exactly"))
			{
				if (score.getGrade().equals(term))
				{ // found a match for the search term, add this guy to the matches
					cmIdMatches.put(score.getCourseMembershipId(), score);
				}
			}
			else if (op.equals("exclude"))
			{
				if (!score.getGrade().contains(term))
				{ // found a match for the search term, add this guy to the matches
					cmIdMatches.put(score.getCourseMembershipId(), score);
				}
			}
			else if (op.equals("greater"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(score.getGrade());
					if (left > right)
					{
						cmIdMatches.put(score.getCourseMembershipId(), score);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			
			}
			else if (op.equals("equal"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(score.getGrade());
					if (left == right)
					{
						cmIdMatches.put(score.getCourseMembershipId(), score);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			}
			else if (op.equals("less"))
			{
				try 
				{
					double right = Double.parseDouble(term);
					double left = Double.parseDouble(score.getGrade());
					if (left < right)
					{
						cmIdMatches.put(score.getCourseMembershipId(), score);
					}
				}
				catch (NumberFormatException e)
				{ // given fields are not numbers
				}
			}
			else
			{ // invalid operator
				debug += "Invalid operator\n";
				return new LinkedHashSet<UserWrapper>();
			}
		}
		
		// assemble the result from the matches
		LinkedHashSet<UserWrapper> ret = new LinkedHashSet<UserWrapper>();
		for (Map.Entry<Id, Score> entry : cmIdMatches.entrySet())
		{
			CourseMembership member = courseMemberships.get(entry.getKey());
			if (member.getIsAvailable())
			{
				User user = users.get(member.getUserId());
				ret.add(new UserWrapper(member, user));
			}
		}
		
		return ret;
	}
	
	/**
	 * Using the course membership list, wrap each member in the UserWrapper object.
	 * 
	 * @return A list of UserWrapper objects.
	 * @throws KeyNotFoundException
	 * @throws PersistenceException
	 * @throws ConnectionNotAvailableException
	 */
	public ArrayList<UserWrapper> getUsers() throws KeyNotFoundException, PersistenceException, ConnectionNotAvailableException
	{
		ArrayList<UserWrapper> ret = new ArrayList<UserWrapper>();
		
		for (Map.Entry<Id, CourseMembership> entry : courseMemberships.entrySet())
		{
			CourseMembership member = entry.getValue();
			User user = users.get(member.getUserId());
			if (member.getIsAvailable() && member.getRole().equals(CourseMembership.Role.STUDENT))
			{
				ret.add(new UserWrapper(member, user));
			}
		}
		
		return ret;
	}
	
	/**
	 * Primitive logging system, just keep concatenating log messages
	 * into a single string and print it out. This returns the log
	 * messages string. 
	 * 
	 * @return log messages
	 */
	public String getDebug()
	{
		return debug;
	}
}
