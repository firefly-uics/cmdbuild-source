package endtoend.pageobjects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;

@Component
public class WebDriverFacade {

	private static Constructor<WebDriver> driverConstructor = getDriverConstructor();

	@SuppressWarnings("unchecked")
	private static Constructor<WebDriver> getDriverConstructor() {
		String driverName = System.getProperty("webdriver.impl", org.openqa.selenium.firefox.FirefoxDriver.class.getCanonicalName());
		try {
			return (Constructor<WebDriver>) Thread.currentThread().getContextClassLoader().loadClass(driverName).getConstructor();
		} catch (Throwable problem) {
			throw new RuntimeException("Couldn't load " + driverName, problem);
		}
	}

	private static WebDriver browser;

	public WebDriver getWebDriver() throws InvocationTargetException, IllegalAccessException, InstantiationException {
		if (browser == null) {
			browser = driverConstructor.newInstance();
		}
		return browser;
	}

	@Before
	public void deleteCookies() {
		browser.manage().deleteAllCookies();
	}

	/*
	 * FIXME This slows down the tests because the browser is closed and destroyed on every scenario
	 */
	@After
	public void closeBrowser() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (browser != null) {
			browser.close();
			//browser.quit();
			browser = null;
		}
	}
}
