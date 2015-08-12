package org.cmdbuild.servlets.json.schema;

import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.logic.translation.converter.InstanceConverter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.translation.converter.WidgetConverter;

import com.google.common.collect.Lists;

public enum TranslatableElement {

	CLASS("class") {
		
		@Override
		public String humanReadableType() {
			return THE_CLASS;
		}
		
		@Override
		public Converter createConverter(final String field) {
			return ClassConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(ClassConverter.description());
		}
	},
	ATTRIBUTECLASS("attributeclass") {
		
		@Override
		public String humanReadableType() {
			return THE_ATTRIBUTE;
		}
		
		@Override
		public Converter createConverter(final String field) {
			return AttributeConverter.of(AttributeConverter.forClass(), field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(AttributeConverter.description(), AttributeConverter.group());
		}
	},
	DOMAIN("domain") {
		
		@Override
		public String humanReadableType() {
			return THE_DOMAIN;
		}
		
		@Override
		public Converter createConverter(final String field) {
			return DomainConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(DomainConverter.description(), DomainConverter.directDescription(),
					DomainConverter.inverseDescription(), DomainConverter.masterDetail());
		}
	},
	ATTRIBUTEDOMAIN("attributedomain") {
		
		@Override
		public String humanReadableType() {
			return THE_ATTRIBUTE;
		}
		
		@Override
		public Converter createConverter(final String field) {
			return AttributeConverter.of(AttributeConverter.forDomain(), field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(AttributeConverter.description());
		}
	},
	FILTER("filter") {
		
		@Override
		public String humanReadableType() {
			return "the filter";
		}
		
		@Override
		public Converter createConverter(final String field) {
			return FilterConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(FilterConverter.description());
		}
	},
	@Deprecated
	INSTANCE_NAME("instancename") {
		
		@Override
		public Converter createConverter(final String field) {
			return InstanceConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return null;
		}
	},
	LOOKUP_VALUE("lookupvalue") {
		
		@Override
		public String humanReadableType() {
			return "the lookup value";
		}
		
		@Override
		public Converter createConverter(final String field) {
			return LookupConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(LookupConverter.description());
		}
	},
	MENU_ITEM("menuitem") {
		
		@Override
		public String humanReadableType() {
			return "the menu entry";
		}

		@Override
		public Converter createConverter(final String field) {
			return MenuItemConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(MenuItemConverter.description());
		}

	},
	REPORT("report") {
		
		@Override
		public String humanReadableType() {
			return "the report";
		}

		@Override
		public Converter createConverter(final String field) {
			return ReportConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(ReportConverter.description());
		}

	},
	VIEW("view") {
		
		@Override
		public String humanReadableType() {
			return "the view";
		}

		@Override
		public Converter createConverter(final String field) {
			return ViewConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(ViewConverter.description());
		}

	},
	WIDGET("classwidget") {
		
		@Override
		public String humanReadableType() {
			return "the widget";
		}

		@Override
		public Converter createConverter(final String field) {
			return WidgetConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(WidgetConverter.label());
		}

	},

	UNDEFINED("undefined") {
		
		@Override
		public String humanReadableType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Converter createConverter(final String field) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<String> allowedFields() {
			throw new UnsupportedOperationException();
		}

	};

	private static final String THE_CLASS = "the class";
	private static final String THE_DOMAIN = "the domain";
	private static final String THE_ATTRIBUTE = "the attribute";
	private final String type;
	private String humanReadableType;

	private TranslatableElement(final String type) {
		this.type = type;
	};
	
	public String getType(){
		return type;
	}
	
	public String humanReadableType(){
		return humanReadableType;
	}

	public abstract Converter createConverter(String field);

	public abstract Iterable<String> allowedFields();

	public static TranslatableElement of(final String type) {
		for (final TranslatableElement element : values()) {
			if (element.type.equalsIgnoreCase(type)) {
				return element;
			}
		}
		return UNDEFINED;
	}

};