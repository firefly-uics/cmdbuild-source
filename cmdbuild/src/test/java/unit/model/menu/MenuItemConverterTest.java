package unit.model.menu;

import static org.cmdbuild.services.store.menu.MenuItemConverter.fromCMClass;
import static org.cmdbuild.services.store.menu.MenuItemConverter.fromCMReport;
import static org.cmdbuild.services.store.menu.MenuItemConverter.fromDashboard;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.logic.data.access.CardDTO;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.cmdbuild.services.store.menu.MenuStore.ReportExtension;
import org.junit.Test;

public class MenuItemConverterTest {

	@Test
	public void testCMClassConvertion() {
		final CMClass aClass = mockClass("FooName", "FooDescription");

		final MenuItem menuItem = fromCMClass(aClass);
		assertEquals(MenuItemType.CLASS, menuItem.getType());
		assertEquals("FooName", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals("", menuItem.getGroupName());
		assertEquals(new Integer(0), menuItem.getReferencedElementId());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testReportPDFConvertion() {
		final Long id = new Long(12);
		final CardDTO aReport = CardDTO.newInstance() //
				.withId(id) //
				.withClassName("Report") //
				.withAttribute("Description", "FooDescription") //
				.build();

		final MenuItem menuItem = fromCMReport(aReport, ReportExtension.PDF);

		assertEquals(MenuItemType.REPORT_PDF, menuItem.getType());
		assertEquals("Report", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, Long.valueOf(menuItem.getReferencedElementId()));
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testReportCSVConvertion() {
		final Long id = new Long(12);
		final CardDTO aReport = CardDTO.newInstance() //
				.withId(id) //
				.withClassName("Report") //
				.withAttribute("Description", "FooDescription") //
				.build();

		final MenuItem menuItem = fromCMReport(aReport, ReportExtension.CSV);

		assertEquals(MenuItemType.REPORT_CSV, menuItem.getType());
		assertEquals("Report", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, Long.valueOf(menuItem.getReferencedElementId()));
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testDashboardConvertion() {
		final Integer id = new Integer(12);

		final MenuItem menuItem = fromDashboard(mockDashboard("FooDescription"), id);

		assertEquals(MenuItemType.DASHBOARD, menuItem.getType());
		assertEquals("_Dashboard", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, menuItem.getReferencedElementId());
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	private CMClass mockClass(final String name, final String description) {
		final CMClass mockClass = mock(CMClass.class);
		final CMIdentifier mockIdentifier = mock(CMIdentifier.class);
		when(mockIdentifier.getLocalName()).thenReturn(name);
		when(mockClass.getIdentifier()).thenReturn(mockIdentifier);
		when(mockClass.getIdentifier().getLocalName()).thenReturn(name);
		when(mockClass.getDescription()).thenReturn(description);

		return mockClass;
	}

	private CMCard mockCard(final Integer id, final String description) {
		final CMCard mockCard = mock(CMCard.class);
		final CMClass mockReport = mockClass("Report", "Report");
		when(mockCard.getDescription()).thenReturn(description);
		when(mockCard.getType()).thenReturn(mockReport);
		when(mockCard.getId()).thenReturn(Long.valueOf(id.toString()));

		return mockCard;
	}

	private DashboardDefinition mockDashboard(final String description) {
		final DashboardDefinition mock = mock(DashboardDefinition.class);
		when(mock.getDescription()).thenReturn(description);

		return mock;
	}
}
