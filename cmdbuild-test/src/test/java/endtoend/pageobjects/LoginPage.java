package endtoend.pageobjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;


public class LoginPage extends PageObject {

	public static String URL = WebSite.BASE_URL + "index.jsp";

	@FindBy(how = How.ID, using = "release_box")
	private WebElement versionStringBox;

	/*
	 * Actions
	 */

	public void fillUsername(String username) {
		callJsFunction("fillUsername", username);
	}

	public void fillPassword(String password) {
		callJsFunction("fillPassword", password);
	}

	public void pressLoginButton() {
		callJsFunction("pressLoginButton");
	}

	/*
	 * Data retrieval
	 */

	public String versionString() {
		return versionStringBox.getText();
	}

	public Boolean errorPopupIsDisplayed() {
		return callJsFunction("errorPopupIsDisplayed");
	}
}
