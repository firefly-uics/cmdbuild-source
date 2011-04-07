package endtoend;

import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.I18n.EN.*;
import cuke4duke.spring.StepDefinitions;
import endtoend.pageobjects.WebSite;

@StepDefinitions
public class WebSiteSteps {

	@Autowired
	private WebSite webSite;

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
