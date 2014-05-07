package org.cmdbuild.model.widget;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.workflow.CMActivityInstance;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Grid extends Widget {

	private static final String DEFAULT_MAP_SEPARATOR = "\n";
	private static final String DEFAULT_ENTRY_SEPARATOR = ",";
	private static final String DEFAULT_KEYVALUE_SEPARATOR = "=";
	private String className;
	private String outputName;
	private Map<String, Object> preset;
	private String mapSeparator;
	private String entrySeparator;
	private String keyValueSeparator;

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setMapSeparator(final String mapSeparator) {
		this.mapSeparator = StringUtils.defaultIfBlank(mapSeparator, DEFAULT_MAP_SEPARATOR);
	}

	public String getMapSeparator() {
		return mapSeparator;
	}

	public void setEntrySeparator(final String entrySeparator) {
		this.entrySeparator = StringUtils.defaultIfBlank(entrySeparator, DEFAULT_ENTRY_SEPARATOR);
	}

	public String getEntrySeparator() {
		return entrySeparator;
	}

	public void setKeyValueSeparator(final String keyValueSeparator) {
		this.keyValueSeparator = StringUtils.defaultIfBlank(keyValueSeparator, DEFAULT_KEYVALUE_SEPARATOR);
	}

	public String getKeyValueSeparator() {
		return keyValueSeparator;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			final Submission submission = decodeInput(input);
			output.put(outputName, outputValue(submission));
		}
	}

	private Object outputValue(final Submission submission) {
		String output = EMPTY;
		if (submission != null) {
			output = submission.getOutput();
		}
		return output;
	}

	private Submission decodeInput(final Object input) {

		String output = EMPTY;
		final StringBuilder outputBuilder = new StringBuilder();
		@SuppressWarnings("unchecked")
		final Map<String, Object> inputMap = (Map<String, Object>) input;
		final Object inputArray = inputMap.get("output");
		@SuppressWarnings("unchecked")
		final Iterable<String> inputElements = (Iterable<String>) inputArray;
		for (final String entry : inputElements) {
			HashMap<String, Object> props;
			try {
				props = new ObjectMapper().readValue(entry, HashMap.class);
				for (final Entry<String, Object> attribute : props.entrySet()) {
					final String key = attribute.getKey();
					if (attribute.getValue() == null) {
						continue;
					}
					if (key.equals("Id") || key.equals("IdClass")) {
						continue;
					}
					final String format = "%s" + keyValueSeparator + "%s";
					final String attributeString = String.format(format, key, attribute.getValue());
					outputBuilder.append(attributeString);
					outputBuilder.append(entrySeparator);
				}
			} catch (final JsonParseException e) {
				e.printStackTrace();
			} catch (final JsonMappingException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			outputBuilder.append(mapSeparator);
		}
		output = outputBuilder.toString();
		final Submission submission = new Submission();
		submission.setOutput(output);
		return submission;
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}

	public static class Submission {
		private String output;

		public String getOutput() {
			return output;
		}

		public void setOutput(final String output) {
			this.output = output;
		}
	}

}