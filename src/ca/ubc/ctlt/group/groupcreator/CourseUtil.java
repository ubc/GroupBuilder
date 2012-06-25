package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.sql.Connection;



import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
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
		{
			return searchSingleCriteria(criterias.get(0));
		}
		
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
			LinkedHashSet<UserWrapper> ret = searchSingleCriteria(criterias.remove(0));
			for (SearchCriteria criteria : criterias)
			{
				LinkedHashSet<UserWrapper> searchRes = searchSingleCriteria(criteria);
				ret.retainAll(searchRes); // this is a set intersection
			}
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
	private void createSearchFields(LinkedHashSet<UserWrapper> wrappers, ArrayList<SearchCriteria> criterias) throws KeyNotFoundException, PersistenceException
	{
		ScoreDbLoader scoreLoader = ScoreDbLoader.Default.getInstance();
		LineitemDbLoader lineitemLoader = LineitemDbLoader.Default.getInstance();
		
		// for each criteria, update all userwrappers with that criteria's name and value
		for (SearchCriteria criteria : criterias)
		{
			for (UserWrapper wrap : wrappers)
			{
				Lineitem item = lineitemLoader.loadById(
						Id.generateId(Lineitem.LINEITEM_DATA_TYPE, criteria.getField())
					);
				Score score = scoreLoader.loadByCourseMembershipIdAndLineitemId(
						wrap.getMember().getId(),
						item.getId()
					);
				wrap.addSearchFields(item.getName(), score.getGrade());
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
		
		GradeCenterUtil gc = new GradeCenterUtil(ctx);
		
		debug += "Checking field id: " + fieldId + "\n";
		if (gc.getUserinfoColumns().containsKey(fieldId))
		{
			debug += "Is a user info field\n";
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
			String target; // the user's value of the field being searched on
			if (fieldId.equals("firstname"))
			{
				target = user.getGivenName();
			}
			else if (fieldId.equals("lastname"))
			{
				target = user.getFamilyName();
			}
			else if (fieldId.equals("studentid"))
			{
				target = user.getStudentId();
			}
			else
			{
				debug += "Unknown user info field.\n";
				return ret;
			}
			debug += "target: " + target + " term: " + term + "\n";
			
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
		return loader.loadByCourseId(ctx.getCourseId(), db, true);
	}
}
