package endtoend.steps;

import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.After;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;
import endtoend.pageobjects.WebSite;

@StepDefinitions
public class WebSiteSteps {
	public enum User {
		AN_ADMINISTRATOR ("admin", "admin", "administrator");
		
		private String userName;
		private String password;
		private String role;
		
		User(String userName, String password, String role) {
			this.userName = userName;
			this.password = password;
			this.role = role;
		}

		public String getUserName() {
			return this.userName;
		}

		public String getPassword() {
			return this.password;
		}

		public String getRole() {
			return role;
		}
		
		public static User fromUserString(final String user) {
			final String userEnumItem = clearForEnum(user);
			return User.valueOf(userEnumItem);
		}
		
		private static String clearForEnum(final String user) {
			return user.replace(" ", "_").toUpperCase();
		}
	}
	
	@Autowired
	private WebSite webSite;

	@After
	public void resetDatabase() {
		webSite.resetDatabase();
	}

	@Given("^I am logged in as (.+)$")
	public void doLogin(final String user) {
		webSite.doLogin(User.fromUserString(user));
	}

	@Given("^the system is configured with a basic class structure$")
	public void theSistemIsConfiguredWithABasicClassStructure() {
		webSite.createBasicClassStructure();
	}

	@Given("^the system is configured$")
	public void theSystemIsConfigured() {
		// TODO
	}

	@Given("^I am not logged in$")
	public void iAmNotLoggedIn() {
		// TODO
	}

	@When("^I access the home page$")
	public void iAccessTheHomePage() {
		webSite.openHomePage();
	}
}
