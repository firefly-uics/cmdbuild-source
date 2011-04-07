package endtoend.pageobjects;

import org.springframework.stereotype.Component;

@Component
public class WebSite extends PageObject {

	static String BASE_URL = "http://localhost:8080/cmdbuild-test/";

	public void openHomePage() {
		open(BASE_URL);
	}

	public LoginPage openLoginPage() {
		return openAs(LoginPage.URL, LoginPage.class);
	}

	public String currentUrl() {
		return webDriver.getCurrentUrl();
	}
}
