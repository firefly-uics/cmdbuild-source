package unit.serializers.jackson;

import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.elements.widget.OpenReport;
import org.cmdbuild.elements.widget.Widget;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.Test;

public class WidgetSerializationTest {

	private static ObjectMapper mapper = new ObjectMapper();

	private static class EmptyWidget extends Widget {
	}

	@Test
	public void widgetsAreCreatedActiveWithEmptyLabel() {
		Widget w =  new EmptyWidget();
		assertEquals(StringUtils.EMPTY, w.getLabel());
		assertTrue(w.isActive());
	}

	@Test
	public void basicWidgetSerialization() throws JsonParseException, JsonMappingException, IOException {
		final String ID = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
		final String LABEL = "Do Something Awesome";

		Widget w =  new EmptyWidget();
		w.setId(ID);
		w.setLabel(LABEL);
		w.setActive(false);

		String jw = mapper.writeValueAsString(w);
		assertThat(jw, containsPair("type", ".WidgetSerializationTest$EmptyWidget"));
		assertThat(jw, containsPair("id", ID));
		assertThat(jw, containsPair("label", LABEL));
		assertThat(jw, containsPair("active", Boolean.FALSE));
	}

	@Test
	public void reportSerialization() throws JsonParseException, JsonMappingException, IOException {
		final String FORMAT = "CSV";
		final String CODE = "BrilliantReport";
		final Map<String,String> PRESET = new HashMap<String,String>();
		PRESET.put("K1", "V1");
		PRESET.put("K2", "V2");
		final String jw = createOpenReportJson(CODE, FORMAT, PRESET);

		Widget w = mapper.readValue(jw, Widget.class);
		assertEquals(OpenReport.class, w.getClass());
		OpenReport orw = (OpenReport) w;
		assertEquals(FORMAT, orw.getForceFormat());
		assertEquals(CODE, orw.getReportCode());
		assertEquals(PRESET, orw.getPreset());

		assertEquals(jw, mapper.writeValueAsString(orw));
	}

	private String createOpenReportJson(final String CODE, final String FORMAT, final Map<String, String> PRESET)
			throws IOException, JsonGenerationException, JsonMappingException {
		OpenReport w = new OpenReport();
		w.setForceFormat(FORMAT);
		w.setReportCode(CODE);
		w.setPreset(PRESET);
		return mapper.writeValueAsString(w);
	}

	/*
	 * Utility methods
	 */

	private static Matcher<String> containsPair(String key, Object value) {
		String valueString;
		if (value == null) {
			valueString = "null";
		} else if (value instanceof String) {
			valueString = String.format("\"%s\"", value);
		} else {
			valueString = value.toString();
		}
        return containsString(String.format("\"%s\":%s", key, valueString));
    }
}
