package ca.ubc.ctlt.group.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import blackboard.base.InitializationException;
import blackboard.data.course.Group;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;
import blackboard.platform.BbServiceException;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManager;
import blackboard.platform.persistence.PersistenceServiceFactory;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;

public class BlackboardProvider extends Provider {
	public static String NAME = "Blackboard";
	public static String DESCRIPTION = "Providing group information from Blackboard Learn.";
	private BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance()
			.getDbPersistenceManager();
	
	@Override
	public HashMap<String, GroupSet> getGroupSets() throws Exception {
		ContextManager ctxMgr = null;
		Context ctx = null;
		HashMap<String, GroupSet> sets;
		
		try {
			// get services
			log("Initializing context manager...");
			ctxMgr = (ContextManager) BbServiceManager.lookupService(ContextManager.class);
			ctx = ctxMgr.setContext(request);
			log("Current context: " + ctx);
		} catch (BbServiceException e) {
			error("Lookup service failed! " + e.getMessage());
			throw e;
		} catch (InitializationException e) {
			error("Failed to initialize the context manager! " + e.getFullMessageTrace());
			throw e;
		} finally {
			if (ctxMgr != null) {
				ctxMgr.releaseContext();
			}
		}

		
		sets = getSets(ctx.getCourseId());
		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
		GroupSet defaultSet = new GroupSet("##DEFAULT##");
		
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
					error("Group " + g.getName() + " could not find group set!");
				}
			}
		}
		
		if (!defaultSet.getGroups().isEmpty()) {
			sets.put("##DEFAULT##", defaultSet);
		}
		System.out.println(sets);
		return sets;
	}

	protected HashMap<String, GroupSet> getSets (Id courseId) {
		HashMap<String, GroupSet> sets = new HashMap<String, GroupSet>();
		
		log("Loading group sets from course " + courseId);
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
		GroupDbLoader groupLoader = (GroupDbLoader) bbPm
				.getLoader(GroupDbLoader.TYPE);
		courseGroups = groupLoader.loadByCourseId(courseId);
		return courseGroups;
	}
	
	@Override
	public String renderOptions() {
		HashMap<String, GroupSet> sets = null;
		
		try {
			sets = getGroupSets();
		} catch (Exception e) {
			System.out.println("Loading group from Blackbaord failed! " + e.getMessage());
			e.printStackTrace();
			return "Loading group from Blackbaord failed! " + e.getMessage();
		}
		if (sets.isEmpty()) {
			return "No Group info";
		}
		return sets.toString();
	}

}
