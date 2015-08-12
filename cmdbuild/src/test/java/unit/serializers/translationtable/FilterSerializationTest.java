package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.servlets.json.serializers.translations.table.FilterTranslationSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class FilterSerializationTest {

	FilterStore.Filter filter1 = mock(FilterStore.Filter.class);
	FilterStore.Filter filter2 = mock(FilterStore.Filter.class);
	FilterStore.GetFiltersResponse filters = new DataViewFilterStore.DataViewGetFiltersResponse(Lists.newArrayList(
			filter1, filter2), 2);

	@Before
	public void setup() {
		doReturn("a").when(filter1).getName();
		doReturn("b").when(filter2).getName();
		doReturn("B").when(filter1).getDescription();
		doReturn("A").when(filter2).getDescription();
	}

	@Test
	public void typeFilterCreatesFilterSerializer() throws Exception {

		// given
		final FilterStore filterStore = mock(FilterStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withType("filter") //
				.withOutput(Output.TABLE) //
				.build();

		// when
		final TranslationSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof FilterTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final FilterStore filterStore = mock(FilterStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(filters).when(filterStore).fetchAllGroupsFilters();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withFilterStore(filterStore) //
				.withTranslationLogic(translationLogic) //
				.withOutput(Output.TABLE) //
				.withType("filter") //
				.build();
		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

	@Test
	public void serializationHasOnlyDescriptionField() throws Exception {

		// given
		final FilterStore filterStore = mock(FilterStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(filters).when(filterStore).fetchAllGroupsFilters();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withFilterStore(filterStore) //
				.withTranslationLogic(translationLogic) //
				.withOutput(Output.TABLE) //
				.withType("filter") //
				.build();
		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		final TableEntry firstFilter = elements.get(0);
		final List<EntryField> fields = Lists.newArrayList(firstFilter.getFields());
		assertTrue(fields.size() == 1);
		assertTrue(fields.get(0).getName().equalsIgnoreCase("description"));
	}

	@Test
	public void orderFiltersByNameIsNotYetSupported() throws Exception {
		// given
		final FilterStore filterStore = mock(FilterStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(filters).when(filterStore).fetchAllGroupsFilters();

		final JSONArray sorters = new JSONArray();
		final JSONObject classSorter = new JSONObject();
		classSorter.put("element", "filter");
		classSorter.put("field", "name");
		classSorter.put("direction", "asc");
		sorters.put(classSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withFilterStore(filterStore) //
				.withTranslationLogic(translationLogic) //
				.withType("filter") //
				.withOutput(Output.TABLE) //
				.build();
		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

}
