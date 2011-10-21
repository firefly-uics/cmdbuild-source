package endtoend.steps;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;
import endtoend.pageobjects.LoginPage;
import endtoend.pageobjects.WebSite;

@StepDefinitions
public class LoginSteps {

	@Autowired
	private WebSite webSite;
	private LoginPage loginPage;
	
	@Given("^I am on the login page")
	@When("^I access the login page$")
	public void iAccessTheLoginPage() {
		loginPage = webSite.openLoginPage();
	}
	
	@Then("^I should be redirected to the login page$")
	public void iShouldBeRedirectedToTheLoginPage() {
		assertThat(webSite.currentUrl(), is(equalTo(LoginPage.URL)));
	}

	@Then("^the version string should begin with \"([^\"]*)\"$")
	public void theTextCMDBuildShouldBeOnThePage(String text) {
		assertThat(loginPage.versionString(), startsWith(text));
	}

	@Given("^I have filled the login form with invalid data$")
	public void iHaveFilledTheLoginFormWithInvalidData() {
		loginPage.fillUsername("invaliduser");
		loginPage.fillPassword("invalidpassword");
	}

	@When("^I press the login button$")
	public void iPressTheLoginButton() {
		loginPage.pressLoginButton();
	}

	@Then("^it should fail with a popup window$")
	public void itShouldFailWithAPopupWindow() {
		waitSomeTime();
		assertThat(loginPage.errorPopupIsDisplayed(), is(true));
	}

	/*
	 * FIXME it should wait just enough time till it happens or a timeout is reached
	 */
	@Deprecated
	private void waitSomeTime() {
		try { Thread.currentThread().sleep(200); } catch (InterruptedException e) {};
	}
}
