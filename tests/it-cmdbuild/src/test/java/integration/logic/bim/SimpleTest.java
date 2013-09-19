package integration.logic.bim;

import org.junit.Test;

import utils.IntegrationTestBim;

public class SimpleTest extends IntegrationTestBim {

	@Test
	public void createAndDropTestDB() throws Exception {
		super.setUp();
		System.out.println("Hello!");
	}

}
