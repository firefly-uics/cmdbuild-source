package endtoend.pageobjects;

import org.springframework.stereotype.Component;

import endtoend.pageobjects.AdministrationPage;
import endtoend.steps.WebSiteSteps.User;

@Component
public class WebSite extends PageObject {

	public static final String BASE_URL = "http://localhost:8080/cmdbuild-test/";
	private static final String TEST_JSONRPC_BASE_URL = BASE_URL + "services/json/test/";
	
	public void openHomePage() {
		open(BASE_URL);
	}

	public LoginPage openLoginPage() {
		return openAs(LoginPage.URL, LoginPage.class);
	}

	public String currentUrl() {
		return webDriver.getCurrentUrl();
	}

	public AdministrationPage openAdministrationPage() {
		return openAs(AdministrationPage.URL, AdministrationPage.class);
	}
	
	public void doLogin(User user) {
		open(String.format(TEST_JSONRPC_BASE_URL + "login?username=%s", user.getUserName(), user.getPassword()));
	}
	
	public void createBasicClassStructure() {
		open(TEST_JSONRPC_BASE_URL + "createbasicstructure");
	}

	public void resetDatabase() {
		open(TEST_JSONRPC_BASE_URL + "resetdatabase");
	}
}
