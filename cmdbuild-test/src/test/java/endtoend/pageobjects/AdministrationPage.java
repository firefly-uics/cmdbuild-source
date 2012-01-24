package endtoend.pageobjects;

public class AdministrationPage extends PageObject {
	public static String URL = WebSite.BASE_URL + "administration.jsp";
	
	public enum Accordion {
		DOMAIN ("domain");
		
		private String name;
		Accordion(String accordionName) {
			this.name = accordionName;
		}
		
		public String getName() {
			return this.name;
		}
	}

	
	/*
	 * Actions
	 */
	

	public void openAccordion(Accordion accordion) {
		callJsFunction("openAccordion", accordion.getName());
	}

	/*
	 * Data retrieval
	 */

	public int countDomainsInAccordion() {
		return ((Long) callJsFunction("countDomainsInAccordion")).intValue();
	}
	
}
