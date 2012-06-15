package ca.ubc.ctlt.group.blackboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;

import ca.ubc.ctlt.group.GroupSet;

import blackboard.base.InitializationException;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;
import blackboard.platform.BbServiceException;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManager;

public class BlackboardUtil
{
	private Context ctx;
	
	public BlackboardUtil(Context ctx)
	{
		this.ctx = ctx;
	}

	public BlackboardUtil(HttpServletRequest request) throws BbServiceException, InitializationException 
	{
		ContextManager ctxMgr = null;
		try {
			// get services
			System.out.println("Initializing context manager...");
			ctxMgr = (ContextManager) BbServiceManager
					.lookupService(ContextManager.class);
			ctx = ctxMgr.setContext(request);
			System.out.println("Current context: " + ctx);
		} catch (BbServiceException e) {
			System.err.println("Lookup service failed! " + e.getMessage());
			throw e;
		} catch (InitializationException e) {
			System.err.println("Failed to initialize the context manager! "
					+ e.getFullMessageTrace());
			throw e;
		} finally {
			if (ctxMgr != null) {
				ctxMgr.releaseContext();
			}
		}
	}
	
	public Context getContext() 
	{
		return ctx;
	}
	
	public HashMap<String, GroupSet> getGroupSets() throws PersistenceException
	{
		HashMap<String, GroupSet> sets;
		
		sets = getSets(ctx.getCourseId());
		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
		GroupSet defaultSet = new GroupSet(GroupSet.EMPTY_NAME);
		
		for(Group s: bbGroups) {
			System.out.println("Processing bbGroup: " + s);
			ca.ubc.ctlt.group.Group g = new ca.ubc.ctlt.group.Group(s);
			if (!s.isInGroupSet()) {
				defaultSet.addGroup(g);
				System.out.println("Added to default set");
			} else {
				boolean added = false;
				for (Entry<String, GroupSet> entry : sets.entrySet()) {
					GroupSet set = entry.getValue();
					if (set.getId().equals(s.getSetId().toExternalString())) {
						set.addGroup(g);
						added = true;
						System.out.println("Added to "+ set.getName() +" set");
					}
				}
				
				if (!added) {
					throw new MissingResourceException("Group " + g.getName() + " could not find group set!", "GroupSet", g.getName());
				}
			}
		}
		
		if (!defaultSet.getGroups().isEmpty()) {
			sets.put(GroupSet.EMPTY_NAME, defaultSet);
		}
		System.out.println(sets);
		return sets;
	}
	
	protected HashMap<String, GroupSet> getSets (Id courseId) {
		HashMap<String, GroupSet> sets = new HashMap<String, GroupSet>();
		
		//log("Loading group sets from course " + courseId);
		List<Group> bbGroupSets = blackboard.persist.course.impl.GroupDAO.get()
				.loadGroupSetsOnly(courseId);
		
		if (bbGroupSets.isEmpty()) {
			return sets;
		}
		
		for (Group s : bbGroupSets) {
			GroupSet set = new GroupSet(s.getTitle());
			set.setId(s.getId().toExternalString());
			sets.put(s.getTitle(), set);
		}
	
		return sets;
	}
	
	protected List<Group> getAllBbGroups(Id courseId) throws PersistenceException {
		List<Group> courseGroups = null;
		GroupDbLoader groupLoader =  GroupDbLoader.Default.getInstance();
		courseGroups = groupLoader.loadByCourseId(courseId);
		return courseGroups;
	}
	
}
