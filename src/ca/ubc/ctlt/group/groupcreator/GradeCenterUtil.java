package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.Collections;


import blackboard.data.gradebook.Lineitem;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.gradebook.LineitemDbLoader;
import blackboard.platform.context.CourseContext;

public class GradeCenterUtil
{
	private CourseContext ctx;
	private LineitemDbLoader loader;

	/**
	 * @param ctx
	 * @throws PersistenceException 
	 */
	public GradeCenterUtil(CourseContext ctx) throws PersistenceException
	{
		super();
		this.ctx = ctx;
		this.loader = LineitemDbLoader.Default.getInstance();
	}
	
	public ArrayList<LineitemWrapper> getColumns() throws KeyNotFoundException, PersistenceException
	{
		ArrayList<Lineitem> list = loader.loadByCourseId(ctx.getCourseId());
		ArrayList<LineitemWrapper> ret = new ArrayList<LineitemWrapper>();
		for (Lineitem item : list)
		{
			// exclude calculated columns such as "Total" and "Weighted Average",
			// since they don't have Score entries, we can't sort on them
			if (!item.getScores().isEmpty())
			{
				ret.add(new LineitemWrapper(item));
			}
		}
		
		Collections.sort(ret);
		
		return ret;
	}
	
}
