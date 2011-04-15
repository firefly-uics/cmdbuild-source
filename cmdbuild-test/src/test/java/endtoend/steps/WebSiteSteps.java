package endtoend.steps;

import org.springframework.beans.factory.annotation.Autowired;

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
	}
	
	@Autowired
	private WebSite webSite;

	@Given("^I am logged in as (.+)$")
	public void doLogin(final String user) {
		String userEnumItem = clearForEnum(user);
		webSite.doLogin(User.valueOf(userEnumItem));
	}

	private String clearForEnum(final String user) {
		return user.replace(" ", "_").toUpperCase();
	}

	@Given("^the system is configured with a basic class structure$")
	public void theSistemIsConfiguredWithABasicClassStructure() {
		// TODO
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
