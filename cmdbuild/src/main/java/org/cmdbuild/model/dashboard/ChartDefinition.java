package org.cmdbuild.model.dashboard;

import java.util.ArrayList;

/*
 * A representation of the definition of a chart
 */
public class ChartDefinition {
	private String name, description, dataSourceName, type, singleSeriesField,
			labelField, categoryAxisField, categoryAxisLabel, valueAxisLabel,
			fgcolor, bgcolor, chartOrientation;

	private boolean active, autoLoad, legend;

	private int height, maximum, minimum, steps;

	private ArrayList<ChartInput> dataSourceParameters;

	private ArrayList<String> valueAxisFields;

	public ChartDefinition() {
		dataSourceParameters = new ArrayList<ChartInput>();
		valueAxisFields = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSingleSeriesField() {
		return singleSeriesField;
	}

	public void setSingleSeriesField(String singleSeriesField) {
		this.singleSeriesField = singleSeriesField;
	}

	public String getLabelField() {
		return labelField;
	}

	public void setLabelField(String labelField) {
		this.labelField = labelField;
	}

	public String getCategoryAxisField() {
		return categoryAxisField;
	}

	public void setCategoryAxisField(String categoryAxisField) {
		this.categoryAxisField = categoryAxisField;
	}

	public String getCategoryAxisLabel() {
		return categoryAxisLabel;
	}

	public void setCategoryAxisLabel(String categoryAxisLabel) {
		this.categoryAxisLabel = categoryAxisLabel;
	}

	public String getValueAxisLabel() {
		return valueAxisLabel;
	}

	public void setValueAxisLabel(String valueAxisLabel) {
		this.valueAxisLabel = valueAxisLabel;
	}

	public String getFgcolor() {
		return fgcolor;
	}

	public void setFgcolor(String fgcolor) {
		this.fgcolor = fgcolor;
	}

	public String getBgcolor() {
		return bgcolor;
	}

	public void setBgcolor(String bgcolor) {
		this.bgcolor = bgcolor;
	}

	public String getChartOrientation() {
		return chartOrientation;
	}

	public void setChartOrientation(String chartOrientation) {
		this.chartOrientation = chartOrientation;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAutoLoad() {
		return autoLoad;
	}

	public void setAutoLoad(boolean autoLoad) {
		this.autoLoad = autoLoad;
	}

	public boolean isLegend() {
		return legend;
	}

	public void setLegend(boolean legend) {
		this.legend = legend;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public int getMinimum() {
		return minimum;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	// dataSourceParameters
	public ArrayList<ChartInput> getDataSourceParameters() {
		return dataSourceParameters;
	}

	public void setDataSourceParameters(
			ArrayList<ChartInput> dataSourceParamenters) {
		this.dataSourceParameters = dataSourceParamenters;
	}

	public void addDataSourceParameter(ChartInput input) {
		this.dataSourceParameters.add(input);
	}

	public void removeDataSourceParameter(ChartInput input) {
		this.dataSourceParameters.remove(input);
	}

	// valueAxisFields
	public ArrayList<String> getValueAxisFields() {
		return valueAxisFields;
	}

	public void setValueAxisFields(ArrayList<String> valueAxisFields) {
		this.valueAxisFields = valueAxisFields;
	}

	public void addValueAxisField(String field) {
		this.valueAxisFields.add(field);
	}

	public void removeValueAxisField(String field) {
		this.valueAxisFields.remove(field);
	}

	/*
	 * The representation of how the user could
	 * insert the value for a input of the data source
	 * of the chart 
	 */
	
	public static class ChartInput {

		private String name, type, fieldType, defaultValue, lookupType,
				className, classToUseForReferenceWidget;

		private boolean required;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFieldType() {
			return fieldType;
		}

		public void setFieldType(String fieldType) {
			this.fieldType = fieldType;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getLookupType() {
			return lookupType;
		}

		public void setLookupType(String lookupType) {
			this.lookupType = lookupType;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getClassToUseForReferenceWidget() {
			return classToUseForReferenceWidget;
		}

		public void setClassToUseForReferenceWidget(String classToUseForReferenceWidget) {
			this.classToUseForReferenceWidget = classToUseForReferenceWidget;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}
	}
}