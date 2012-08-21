package ca.ubc.ctlt.group.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import blackboard.data.user.User;
import ca.ubc.ctlt.group.GroUser;
import ca.ubc.ctlt.group.GroGroup;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.UploadMultipartRequestWrapper;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class CsvProviderTest {
	private BlackboardUtil util = null;
	private UploadMultipartRequestWrapper request = null;
	private HttpServletResponse response = null;

	@Before
	public final void setUp() {
		request = mock(UploadMultipartRequestWrapper.class);
		response = mock(HttpServletResponse.class);
		when(request.getParameter("csvfile_attachmentType")).thenReturn("L");
		
		// setup BlackboardUtil fixture
		User user1 = mock(User.class);
		when(user1.getStudentId()).thenReturn("redshirt1");
		when(user1.getUserName()).thenReturn("username1");
		when(user1.getGivenName()).thenReturn("Name1");
		when(user1.getFamilyName()).thenReturn("Family1");

		User user2 = mock(User.class);
		when(user2.getStudentId()).thenReturn("redshirt2");
		when(user2.getUserName()).thenReturn("username2");
		when(user2.getGivenName()).thenReturn("Name2");
		when(user2.getFamilyName()).thenReturn("Family2");

		User user3 = mock(User.class);
		when(user3.getStudentId()).thenReturn("redshirt3");
		when(user3.getUserName()).thenReturn("username3");
		when(user3.getGivenName()).thenReturn("Name3");
		when(user3.getFamilyName()).thenReturn("Family3");

		User user4 = mock(User.class);
		when(user4.getStudentId()).thenReturn("redshirt4");
		when(user4.getUserName()).thenReturn("username4");
		when(user4.getGivenName()).thenReturn("Name4");
		when(user4.getFamilyName()).thenReturn("Family4");

		User user5 = mock(User.class);
		when(user5.getStudentId()).thenReturn("redshirt5");
		when(user5.getUserName()).thenReturn("username5");
		when(user5.getGivenName()).thenReturn("Name5");
		when(user5.getFamilyName()).thenReturn("Family5");

		util = mock(BlackboardUtil.class);
		when(util.findUserByStudentId("redshirt1")).thenReturn(user1);
		when(util.findUserByUsername("username1")).thenReturn(user1);
		when(util.findUserByStudentId("redshirt2")).thenReturn(user2);
		when(util.findUserByUsername("username2")).thenReturn(user2);
		when(util.findUserByStudentId("redshirt3")).thenReturn(user3);
		when(util.findUserByUsername("username3")).thenReturn(user3);
		when(util.findUserByStudentId("redshirt4")).thenReturn(user4);
		when(util.findUserByUsername("username4")).thenReturn(user4);
		when(util.findUserByStudentId("redshirt5")).thenReturn(user5);
		when(util.findUserByUsername("username5")).thenReturn(user5);
	}

	@Test
	public final void testGetOptionsPage() {
		CsvProvider provider = new CsvProvider();
		assertTrue("providers/csv/options.jsp"
				.equals(provider.getOptionsPage()));
	}

	@Test
	public final void testGetGroupSetsWithoutSetColumn() {
		when(request.getFileFromParameterName("csvfile_LocalFile0"))
				.thenReturn(new File("test/fixtures/groups_noset.csv"));

		CsvProvider provider = new CsvProvider();
		provider.setRequest(request);
		provider.setResponse(response);

		Map<String, GroupSet> sets = provider.getGroupSets(util);
		verifySets(sets, new String[] {GroupSet.EMPTY_NAME});
	}

	@Test
	public final void testGetGroupSetsWithSetColumn() {
		when(request.getFileFromParameterName("csvfile_LocalFile0"))
		.thenReturn(new File("test/fixtures/groups_with_sets.csv"));

		CsvProvider provider = new CsvProvider();
		provider.setRequest(request);
		provider.setResponse(response);

		Map<String, GroupSet> sets = provider.getGroupSets(util);
		verifySets(sets, new String[] { "Set1", "Set2" });
	}
	
	@Test
	public final void testGetGroupSetsWithUsernameColumn() {
		when(request.getFileFromParameterName("csvfile_LocalFile0"))
		.thenReturn(new File("test/fixtures/groups_with_usernames.csv"));

		CsvProvider provider = new CsvProvider();
		provider.setRequest(request);
		provider.setResponse(response);

		Map<String, GroupSet> sets = provider.getGroupSets(util);
		verifySets(sets, new String[] { "Set1", "Set2" });
	}

	@Test
	public final void testGetGroupSetsWithSetRandomRaw() {
		when(request.getFileFromParameterName("csvfile_LocalFile0"))
				.thenReturn(new File("test/fixtures/groups_with_sets_random.csv"));

		CsvProvider provider = new CsvProvider();
		provider.setRequest(request);
		provider.setResponse(response);

		Map<String, GroupSet> sets = provider.getGroupSets(util);
		verifySets(sets, new String[] { "Set1", "Set2" });
	}

	@Test
	public final void testGetGroupSetsWithBadFile() {
		when(request.getFileFromParameterName("csvfile_LocalFile0"))
				.thenReturn(new File("test/fixtures/groups_bad.csv"));

		CsvProvider provider = new CsvProvider();
		provider.setRequest(request);
		provider.setResponse(response);

		Map<String, GroupSet> sets = provider.getGroupSets(util);
		assertNull(sets);
	}

	@Test
	public final void testHasFileUpload() {
		CsvProvider provider = new CsvProvider();
		assertTrue(provider.hasFileUpload());
	}
	
	private final void verifySets(Map<String, GroupSet> sets, String[] setNames)
	{
		assertTrue(setNames.length == sets.size());
		
		for (String setName : setNames) {
			GroupSet set = sets.get(setName);
			assertNotNull(set);
			assertTrue(2 == set.getGroups().size());

			GroGroup group = set.getGroup("Group1");
			assertNotNull(group);
			assertTrue("Group1".equals(group.getName()));
			assertTrue(3 == group.getMemberList().size());

			GroUser user = group.getMember("username1");
			assertTrue("username1".equals(user.getUserName()));
			assertTrue("redshirt1".equals(user.getStudentID()));
			assertTrue("Name1 Family1".equals(user.getName()));

			user = group.getMember("username2");
			assertTrue("username2".equals(user.getUserName()));
			assertTrue("redshirt2".equals(user.getStudentID()));
			assertTrue("Name2 Family2".equals(user.getName()));

			user = group.getMember("username3");
			assertTrue("username3".equals(user.getUserName()));
			assertTrue("redshirt3".equals(user.getStudentID()));
			assertTrue("Name3 Family3".equals(user.getName()));

			group = set.getGroup("Group2");
			assertNotNull(group);
			assertTrue("Group2".equals(group.getName()));
			assertTrue(2 == group.getMemberList().size());

			user = group.getMember("username4");
			assertTrue("username4".equals(user.getUserName()));
			assertTrue("redshirt4".equals(user.getStudentID()));
			assertTrue("Name4 Family4".equals(user.getName()));

			user = group.getMember("username5");
			assertTrue("username5".equals(user.getUserName()));
			assertTrue("redshirt5".equals(user.getStudentID()));
			assertTrue("Name5 Family5".equals(user.getName()));
		}		
	}
}
