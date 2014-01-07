package integration.logic.bim;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mchange.util.AssertException;

import utils.DatabaseDataFixture;
import utils.IntegrationTestBase;
import utils.IntegrationTestBim;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;

public class SimpleTest extends IntegrationTestBim {
	
	@ClassRule
	public static DatabaseDataFixture databaseDataFixture = DatabaseDataFixture.newInstance() //
			.dropAfter(true) //
			.hook(new Hook() {

				@Override
				public void before(final Context context) {
					try {
						final JdbcTemplate jdbcTemplate = new JdbcTemplate(context.dataSource());
						final URL url = IntegrationTestBase.class.getClassLoader().getResource("postgis.sql");
						final String sql = FileUtils.readFileToString(new File(url.toURI()));
						jdbcTemplate.execute(sql);
					} catch (Exception e) {
						e.printStackTrace();
						throw new AssertException("should never come here");
					}
				}

				@Override
				public void after(final Context context) {
					// do nothing
				}

			}) //
			.build();
	
	@Test
	public void createAndDropTestDB() throws Exception {
		super.setUp();
		System.out.println("Hello!");
	}

}
