package ca.ubc.ctlt.group.groupcreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


import blackboard.data.gradebook.Lineitem;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.gradebook.LineitemDbLoader;
import blackboard.platform.context.CourseContext;

public class GradeCenterUtil
{
	private CourseContext ctx;

	/**
	 * @param ctx
	 * @throws PersistenceException 
	 */
	public GradeCenterUtil(CourseContext ctx) throws PersistenceException
	{
		super();
		this.ctx = ctx;
	}
	
	/**
	 * The Grade Center displays columns from 3 different information sources:
	 * 1. The user object - e.g.: first name, last name
	 * 2. The calculated column - e.g.: average grade, these are calculated on the fly and not stored in the database
	 * 3. The created column - e.g.: actual grades for an assignment
	 * 
	 * The calculated and created columns are both stored as Lineitems. However, only the created columns actually
	 * store scores in the database. This method returns only the created columns, since we can't search on
	 * calculated columns as they have no accessible scores.
	 * 
	 * @return - Grade center columns stored as Lineitems that can be searched on.
	 * @throws KeyNotFoundException
	 * @throws PersistenceException
	 */
	public ArrayList<LineitemWrapper> getLineitemColumns() throws KeyNotFoundException, PersistenceException
	{
		LineitemDbLoader loader = LineitemDbLoader.Default.getInstance();
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
	
	/**
	 * The Grade Center displays columns from 3 different information sources:
	 * 1. The user object - e.g.: first name, last name
	 * 2. The calculated column - e.g.: average grade, these are calculated on the fly and not stored in the database
	 * 3. The created column - e.g.: actual grades for an assignment
	 * 
	 * This method returns a list of searchable columns that originate from the user object.
	 * 
	 * @return
	 */
	public static TreeMap<String, String> getUserinfoColumns()
	{
		TreeMap<String, String> ret = new TreeMap<String, String>();
		ret.put("lastname", "Last Name");
		ret.put("firstname", "First Name");
		ret.put("studentid", "Student ID");
		
		return ret;
	}
	
}
