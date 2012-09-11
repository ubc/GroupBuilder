package ca.ubc.ctlt.group.groupcreator;

import java.util.List;

import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;
import blackboard.platform.context.CourseContext;

public class CourseUtil
{
	private CourseContext ctx;

	/**
	 * @param ctx
	 * @throws PersistenceException 
	 */
	public CourseUtil(CourseContext ctx) throws PersistenceException
	{
		super();
		this.ctx = ctx;
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
		if (ret.length() > 1) {
			ret = ret.substring(0, ret.length() - 1);
		}
		ret += "]";
		return ret;
	}
}
