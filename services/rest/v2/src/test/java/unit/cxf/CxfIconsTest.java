package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import javax.activation.DataHandler;

import org.bimserver.utils.FileDataSource;
import org.cmdbuild.logic.icon.Element;
import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfIcons;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Function;

public class CxfIconsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private IconsLogic iconsLogic;
	private Function<Icon, Element> iconToElement;
	private Function<Element, Icon> elementToIcon;
	private CxfIcons underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		iconsLogic = mock(IconsLogic.class);
		iconToElement = mock(Function.class);
		elementToIcon = mock(Function.class);
		underTest = new CxfIcons(errorHandler, iconsLogic, iconToElement, elementToIcon);
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullIconThrowsException() throws Exception {
		// given
		final DataHandler dataHandler = new DataHandler(new FileDataSource(folder.newFile()));

		// when
		underTest.create(null, dataHandler);
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullDataHandlerThrowsException() throws Exception {
		// given
		final Icon icon = newIcon().build();

		// when
		underTest.create(icon, null);
	}

	@Test
	public void create() throws Exception {
		// given
		final Icon inputIcon = newIcon().build();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(folder.newFile()));
		final Element elementFromIcon = mock(Element.class);
		doReturn(elementFromIcon) //
				.when(iconToElement).apply(any(Icon.class));
		final Element elementFromLogic = mock(Element.class);
		doReturn(elementFromLogic) //
				.when(iconsLogic).create(any(Element.class), any(DataHandler.class));
		final Icon outputIcon = newIcon().build();
		doReturn(outputIcon) //
				.when(elementToIcon).apply(any(Element.class));

		// when
		final ResponseSingle<Icon> response = underTest.create(inputIcon, dataHandler);

		// then
		verify(iconToElement).apply(eq(inputIcon));
		verify(iconsLogic).create(eq(elementFromIcon), eq(dataHandler));
		verify(elementToIcon).apply(eq(elementFromLogic));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(response,
				equalTo(newResponseSingle(Icon.class) //
						.withElement(outputIcon) //
						.build()));
	}

	@Test
	public void read() throws Exception {
		// given
		final Element first = mock(Element.class);
		final Element second = mock(Element.class);
		final Element third = mock(Element.class);
		doReturn(asList(first, second, third)) //
				.when(iconsLogic).read();
		final Icon _first = newIcon().build();
		final Icon _second = newIcon().build();
		final Icon _third = newIcon().build();
		doReturn(_first).doReturn(_second).doReturn(_third) //
				.when(elementToIcon).apply(any(Element.class));

		// when
		final ResponseMultiple<Icon> response = underTest.read();

		// then
		verify(iconsLogic).read();
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(elementToIcon, times(3)).apply(captor.capture());
		assertThat(captor.getAllValues(), contains(first, second, third));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(response,
				equalTo(newResponseMultiple(Icon.class) //
						.withElements(asList(_first, _second, _third)) //
						.withMetadata(newMetadata() //
								.withTotal(3) //
								.build())
						.build()));
	}

	@Test(expected = NullPointerException.class)
	public void readWithNullIdThrowsException() throws Exception {
		// when
		underTest.read(null);
	}

	@Test
	public void readMissingElementInvokesErrorHandler() throws Exception {
		// given
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(Element.class));

		// when
		underTest.read("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq("the_id"));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);
	}

	@Test
	public void readSingleIcon() throws Exception {
		// given
		final Element element = mock(Element.class);
		doReturn(Optional.of(element)) //
				.when(iconsLogic).read(any(Element.class));
		final Icon outputIcon = newIcon().build();
		doReturn(outputIcon) //
				.when(elementToIcon).apply(any(Element.class));

		// when
		final ResponseSingle<Icon> response = underTest.read("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(elementToIcon).apply(eq(element));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getValue().getId(), equalTo("the_id"));
		assertThat(response,
				equalTo(newResponseSingle(Icon.class) //
						.withElement(outputIcon) //
						.build()));
	}

	@Test(expected = NullPointerException.class)
	public void downloadWithNullIdThrowsException() throws Exception {
		// when
		underTest.download(null);
	}

	@Test
	public void downloadWithMissingElementInvokesErrorHandler() throws Exception {
		// given
		doReturn(Optional.empty()) //
				.when(iconsLogic).download(any(Element.class));

		// when
		underTest.download("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).download(captor.capture());
		verify(errorHandler).missingIcon(eq("the_id"));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);
	}

	@Test
	public void download() throws Exception {
		// given
		final DataHandler dataHandler = mock(DataHandler.class);
		doReturn(Optional.of(dataHandler)) //
				.when(iconsLogic).download(any(Element.class));

		// when
		final DataHandler response = underTest.download("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).download(captor.capture());
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getValue().getId(), equalTo("the_id"));
		assertThat(response, equalTo(dataHandler));
	}

	@Test(expected = NullPointerException.class)
	public void updateWithNullIdThrowsException() throws Exception {
		// given
		final DataHandler dataHandler = new DataHandler(new FileDataSource(folder.newFile()));

		// when
		underTest.update(null, dataHandler);
	}

	@Test(expected = NullPointerException.class)
	public void updateWithNullDataHandlerThrowsException() throws Exception {
		// when
		underTest.update("the_id", null);
	}

	@Test
	public void updateWithMissingElementInvokesErrorHandler() throws Exception {
		// given
		final DataHandler dataHandler = new DataHandler(new FileDataSource(folder.newFile()));
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(Element.class));

		// when
		underTest.update("the_id", dataHandler);

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq("the_id"));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getValue().getId(), equalTo("the_id"));
	}

	@Test
	public void update() throws Exception {
		// given
		final DataHandler dataHandler = new DataHandler(new FileDataSource(folder.newFile()));
		final Element element = mock(Element.class);
		doReturn(Optional.of(element)) //
				.when(iconsLogic).read(any(Element.class));

		// when
		underTest.update("the_id", dataHandler);

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(iconsLogic).update(captor.capture(), eq(dataHandler));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getAllValues().get(0).getId(), equalTo("the_id"));
	}

	@Test(expected = NullPointerException.class)
	public void deleteWithNullIdThrowsException() throws Exception {
		// when
		underTest.delete(null);
	}

	@Test
	public void deleteWithMissingElementInvokesErrorHandler() throws Exception {
		// given
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(Element.class));

		// when
		underTest.delete("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq("the_id"));
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getValue().getId(), equalTo("the_id"));
	}

	@Test
	public void delete() throws Exception {
		// given
		final Element element = mock(Element.class);
		doReturn(Optional.of(element)) //
				.when(iconsLogic).read(any(Element.class));

		// when
		underTest.delete("the_id");

		// then
		final ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
		verify(iconsLogic).read(captor.capture());
		verify(iconsLogic).delete(captor.capture());
		verifyNoMoreInteractions(errorHandler, iconsLogic, iconToElement, elementToIcon);

		assertThat(captor.getAllValues().get(0).getId(), equalTo("the_id"));
		assertThat(captor.getAllValues().get(1), equalTo(element));
	}

}
