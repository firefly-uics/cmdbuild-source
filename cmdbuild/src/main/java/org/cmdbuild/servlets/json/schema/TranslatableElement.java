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
		public Converter createConverter(final String field) {
			return FilterConverter.of(field);
		}

		@Override
		public Iterable<String> allowedFields() {
			return Lists.newArrayList(FilterConverter.description());
		}
	},
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
		public Converter createConverter(final String field) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<String> allowedFields() {
			throw new UnsupportedOperationException();
		}

	};

	private final String type;

	private TranslatableElement(final String type) {
		this.type = type;
	};
	
	public String getType(){
		return type;
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