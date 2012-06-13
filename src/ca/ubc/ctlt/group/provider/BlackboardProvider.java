package ca.ubc.ctlt.group.provider;

import java.util.HashMap;

import blackboard.base.InitializationException;
import blackboard.platform.BbServiceException;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManager;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class BlackboardProvider extends Provider {
	public static String NAME = "Blackboard";
	public static String DESCRIPTION = "Providing group information from Blackboard Learn.";
	
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
		
		BlackboardUtil util = new BlackboardUtil(ctx);
		sets = util.getGroupSets();
		
		return sets;
	}


	@Override
	public String getOptionsPage()
	{
		return "providers/blackboard/options.jsp";
	}

}
