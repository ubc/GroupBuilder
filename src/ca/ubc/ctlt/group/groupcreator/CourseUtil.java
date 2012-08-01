package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.sql.Connection;

import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.gradebook.LineitemDbLoader;
import blackboard.persist.gradebook.ScoreDbLoader;
import blackboard.platform.context.CourseContext;
import blackboard.platform.db.JdbcServiceFactory;

public class CourseUtil
{
	private CourseContext ctx;
	private CourseMembershipDbLoader loader;
	private String debug = "";

	/**
	 * @param ctx
	 * @throws PersistenceException 
	 */
	public CourseUtil(CourseContext ctx) throws PersistenceException
	{
		super();
		this.ctx = ctx;
		this.loader = CourseMembershipDbLoader.Default.getInstance();
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
		ScoreDbLoader scoreLoader = ScoreDbLoader.Default.getInstance();
		LineitemDbLoader lineitemLoader = LineitemDbLoader.Default.getInstance();
		debug += "Size Criterias: " + criterias.size() + "\n";
		
		// for each criteria, update all userwrappers with that criteria's name and value
		for (SearchCriteria criteria : criterias)
		{
			if (criteria.isUserInfoField())
			{ // this is a user info field that is already displayed to the user by default
				debug += "Skipping criteria: " + criteria.getField();
				continue;
			}
			for (UserWrapper wrap : wrappers)
			{ // this is a field that needs to be displayed to the user
				debug += "Trying to get values for: " + criteria.getField();
				// get the field
				Lineitem item = lineitemLoader.loadById(
							Id.generateId(Lineitem.LINEITEM_DATA_TYPE, criteria.getField())
						);
				Score score;
				try
				{
					score = scoreLoader.loadByCourseMembershipIdAndLineitemId(
						wrap.getMember().getId(),
						item.getId()
					);
					// get the value of the field for this user
					debug += "... Done! " + item.getName() + " " + score.getGrade() + "\n";
					wrap.addSearchFields(item.getName(), score.getGrade());
				} catch (KeyNotFoundException e)
				{ // special case where no score can be found for this user, might be a prof?
					debug += "... Failed!\n";
					wrap.addSearchFields(item.getName(), "");
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
		Lineitem item = LineitemDbLoader.Default.getInstance().loadById(Id.generateId(Lineitem.LINEITEM_DATA_TYPE, fieldId));
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
		ArrayList<CourseMembership> list = getCourseMemberships();
		for (CourseMembership member : list)
		{
			if (cmIdMatches.containsKey(member.getId()))
			{
				ret.add(new UserWrapper(member));
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
		ArrayList<CourseMembership> list = getCourseMemberships();
		ArrayList<UserWrapper> ret = new ArrayList<UserWrapper>();
		
		for (CourseMembership member : list)
		{
			ret.add(new UserWrapper(member));
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
	
	
	/**
	 * Load the current course context's membership list.
	 * 
	 * @return A list of the current course's members.
	 * @throws ConnectionNotAvailableException
	 * @throws KeyNotFoundException
	 * @throws PersistenceException
	 */
	private ArrayList<CourseMembership> getCourseMemberships() throws ConnectionNotAvailableException, KeyNotFoundException, PersistenceException
	{
		// BB api splits loadBy* calls into a 'lightweight' and 'heavyweight' category for some reason.
		// The heavyweight call is needed to make sure the user objects information gets loaded with the proper info, 
		// and for some reason, you need to get the sql connection as a parameter for the heavyweight method call.
		Connection db = JdbcServiceFactory.getInstance().getDefaultDatabase()
				.getConnectionManager().getConnection();
		ArrayList<CourseMembership> members = loader.loadByCourseId(ctx.getCourseId(), db, true);
		ArrayList<CourseMembership> ret = new ArrayList<CourseMembership>();
		for (CourseMembership membership : members)
		{
			debug += "member: " + membership.getUser().getUserName() + " " + membership.getIsAvailable() + "\n";
			if (membership.getIsAvailable())
			{
				ret.add(membership);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a list of group names in JSON array format. 
	 * E.g.: ["group1","group2","group3"]
	 * 
	 * @return
	 * @throws PersistenceException
	 */
	public String getGroupsJSON() throws PersistenceException {
		Id courseId = ctx.getCourseId();
		String ret = "[";
		List<Group> courseGroups = null;
		GroupDbLoader groupLoader =  GroupDbLoader.Default.getInstance();
		courseGroups = groupLoader.loadByCourseId(courseId);
		for (Group group : courseGroups)
		{
			if (!group.isGroupSet())
			{
				// escape double quotes since that's the one I'm using
				String title = group.getTitle().replace("\"", "\\\"");
				ret += "\"" + title + "\",";
			}
		}
		ret = ret.substring(0, ret.length() - 1);
		ret += "]";
		return ret;
	}
}
