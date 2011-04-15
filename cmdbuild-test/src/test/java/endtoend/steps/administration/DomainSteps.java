package endtoend.steps.administration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.spring.StepDefinitions;
import endtoend.pageobjects.AdministrationPage;
import endtoend.pageobjects.WebSite;
import endtoend.pageobjects.AdministrationPage.Accordion;

@StepDefinitions
public class DomainSteps {
	
	@Autowired
	private WebSite webSite;
	private AdministrationPage administrationPage;

	@Given("^I am on the domain administration section$")
	public void iAmOnTheDomainAdministrationSection() {
		administrationPage = webSite.openAdministrationPage();
		administrationPage.openAccordion(Accordion.DOMAIN);
	}

	@Then("^I should see the list of the existing domains$")
	public void iShouldSeeTheListOfExistingDomains() {
		assertThat(administrationPage.countDomainsInAccordion(), is(equalTo(10)));
	}
}
