package endtoend.pageobjects;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class PageObject {

	static final Pattern urlPattern = Pattern.compile("/([^/]+)\\.jsp");

	static final String HELPER_BASENAME = "endtoend/helpers/helper";
	static final String HELPER_JS_CLASS = "TestHelper";
	static final String HELPER_JS_OBJECT = "TestHelperInstance";
	static final String JS_EXEC_PATTERN = "return " + HELPER_JS_OBJECT + ".%s.apply(" + HELPER_JS_OBJECT + ", arguments)";
	static final String JS_SUFFIX = ".js";
	static final String RESOURCE_CHARSET = "UTF-8";

	protected WebDriver webDriver;

	public PageObject() {
		tryCreateWebDriver();
		injectJSHelpers();
	}

	protected <T extends PageObject> T openAs(String address, Class<T> pageClass) {
		open(address);
		return pageObjectInstance(pageClass);
	}

	protected final void open(String address) {
		webDriver.navigate().to(address);
	}

	@SuppressWarnings("unchecked")
	protected <T> T callJsFunction(final String functionName, final Object... args) {
		final JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		final String script = String.format(JS_EXEC_PATTERN, functionName);
		return (T) jsExecutor.executeScript(script, args);
	}

	@SuppressWarnings("unchecked")
	protected <T> T jsExec(final String script, final Object... args) {
		final JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		return (T) jsExecutor.executeScript(script, args);
	}

	private <T extends PageObject> T pageObjectInstance(Class<T> pageClass) {
		return PageFactory.initElements(webDriver, pageClass);
	}

	private void tryCreateWebDriver() {
		try {
			webDriver = new WebDriverFacade().getWebDriver();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void injectJSHelpers() {
		if (jsIsSupportedOnTheCurrentPage()) {
			injectGlobalHelper();
			injectPageSpecificHelper();
			injectHelperInstance();
		}
	}

	private void injectGlobalHelper() {
		injectResource(HELPER_BASENAME + JS_SUFFIX);
	}

	private void injectPageSpecificHelper() {
		final String cmPage = getCMPage();
		if (cmPage != null) {
			injectResource(HELPER_BASENAME + "-" + cmPage + JS_SUFFIX);
		}
	}

	private void injectResource(final String resourceName) {
		jsExec(resourceAsString(resourceName));
	}

	private void injectHelperInstance() {
		jsExec(HELPER_JS_OBJECT + " = new " + HELPER_JS_CLASS + "()");
	}

	private String getCMPage() {
		final String url = webDriver.getCurrentUrl();
		final Matcher urlMatcher = urlPattern.matcher(url);
		if (urlMatcher.find()) {
			return urlMatcher.group(1);
		} else {
			return null;
		}
	}

	boolean jsIsSupportedOnTheCurrentPage() {
		final String url = webDriver.getCurrentUrl();
		return !"about:blank".equals(url);
	}

	private String resourceAsString(final String resourceName) {
		final InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
		return inputStreamToString(is);
	}

	private String inputStreamToString(final InputStream inputStream) {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, RESOURCE_CHARSET);
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}
}
