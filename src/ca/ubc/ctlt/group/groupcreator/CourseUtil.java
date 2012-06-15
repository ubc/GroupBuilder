package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.sql.Connection;



import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.context.CourseContext;
import blackboard.platform.db.JdbcServiceFactory;

public class CourseUtil
{
	private CourseContext ctx;
	private CourseMembershipDbLoader loader;

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
	
	public ArrayList<UserWrapper> search(String fieldId, String op, String term) throws PersistenceException, ConnectionNotAvailableException
	{
		Hashtable<Id, Score> cmIdMatches = new Hashtable<Id, Score>(); // holds the matching CourseMembershipId
		// get the Lineitem holding the GradeCenter field, this is identified by fieldId
		GradeCenterUtil gc = new GradeCenterUtil(ctx);
		ArrayList<LineitemWrapper> fields = gc.getColumns();
		Lineitem item = null;
		for (LineitemWrapper wrap : fields)
		{
			if (wrap.getIdString().equals(fieldId))
			{
				item = wrap.getItem();
				break;
			}
		}
		// non-existent Lineitem
		if (item == null)
		{
			return new ArrayList<UserWrapper>();
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
				return new ArrayList<UserWrapper>();
			}
		}
		
		// assemble the result from the matches
		ArrayList<UserWrapper> ret = new ArrayList<UserWrapper>();
		ArrayList<CourseMembership> list = getCourseMemberships();
		for (CourseMembership member : list)
		{
			if (cmIdMatches.containsKey(member.getId()))
			{
				Score score = cmIdMatches.get(member.getId());
				ret.add(new UserWrapper(member, item.getName(), score.getGrade()));
			}
		}
		
		return ret;
	}
	
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
