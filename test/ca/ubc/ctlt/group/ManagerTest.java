package ca.ubc.ctlt.group;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class ManagerTest {

	private static final String[] providerNames = new String[] {
			"ca.ubc.ctlt.group.provider.BlackboardProvider",
			"ca.ubc.ctlt.group.provider.CsvProvider",
			"ca.ubc.ctlt.group.provider.GroupCreatorProvider" };
	
	private static final String[] consumerNames = new String[] {
		"ca.ubc.ctlt.group.consumer.BlackboardConsumer",
		"ca.ubc.ctlt.group.consumer.CsvConsumer",
		"ca.ubc.ctlt.group.consumer.GroupCreatorConsumer" };
	
	@Test
	public void testManager() {
		Manager manager = new Manager();
		assertNotNull(manager);
	}

	@Test
	public void testManagerHttpServletRequestHttpServletResponse() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getParameter("provider")).thenReturn("ca.ubc.ctlt.group.provider.CsvProvider");
		when(request.getParameter("consumer")).thenReturn("ca.ubc.ctlt.group.consumer.CsvConsumer");
		
		Manager manager = new Manager(request, response);
		assertNotNull(manager);
	}

	@Test
	public void testGetProviders() {
		Manager manager = new Manager();
		Class<Provider>[] providers = manager.getProviders();
		for (Class<Provider> c : providers) {
			assertTrue(Arrays.asList(providerNames).contains(c.getName()));
		}
	}

	@Test
	public void testGetConsumers() {
		Manager manager = new Manager();
		Class<Consumer>[] consumers = manager.getConsumers();
		for (Class<Consumer> c : consumers) {
			assertTrue(Arrays.asList(consumerNames).contains(c.getName()));
		}
	}

}
