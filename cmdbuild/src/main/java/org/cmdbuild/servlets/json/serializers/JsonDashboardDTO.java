package org.cmdbuild.servlets.json.serializers;

import java.util.LinkedList;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.model.dashboard.DashboardDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public interface JsonDashboardDTO {

	public class JsonDashboardListResponse {

		static Function<CMFunction, JsonDataSource> dataSourceConverter = new Function<CMFunction, JsonDataSource>() {
			@Override
			public JsonDataSource apply(final CMFunction input) {
				return new JsonDataSource(input);
			}
		};

		private final Map<Long, DashboardDefinition> dashboards;
		private final Iterable<JsonDataSource> dataSources;

		public JsonDashboardListResponse(final Map<Long, DashboardDefinition> dashboards) {
			this.dashboards = dashboards;
			this.dataSources = new LinkedList<JsonDataSource>();
		}

		public JsonDashboardListResponse(final Map<Long, DashboardDefinition> dashboards,
				final Iterable<? extends CMFunction> dataSources) {
			this.dashboards = dashboards;
			this.dataSources = Lists.newArrayList(Iterables.transform(dataSources, dataSourceConverter));
		}

		public Map<Long, DashboardDefinition> getDashboards() {
			return dashboards;
		}

		public Iterable<JsonDataSource> getDataSources() {
			return dataSources;
		}
	}

	public class JsonDataSource {

		static Function<CMFunctionParameter, JsonDataSourceParameter> parameterConverter = new Function<CMFunctionParameter, JsonDataSourceParameter>() {
			@Override
			public JsonDataSourceParameter apply(final CMFunctionParameter input) {
				return new JsonDataSourceParameter(input);
			}
		};

		private final CMFunction inner;

		private JsonDataSource(final CMFunction inner) {
			this.inner = inner;
		}

		public String getName() {
			return inner.getIdentifier().getLocalName();
		}

		public Iterable<JsonDataSourceParameter> getInput() {
			return Lists.newArrayList(Iterables.transform(inner.getInputParameters(), parameterConverter));
		}

		public Iterable<JsonDataSourceParameter> getOutput() {
			return Lists.newArrayList(Iterables.transform(inner.getOutputParameters(), parameterConverter));
		}
	}

	public class JsonDataSourceParameter {

		private final String name;
		private final String type;

		private JsonDataSourceParameter(final CMFunctionParameter parameter) {
			this.name = parameter.getName();
			this.type = new TypeConverter(parameter.getType()).getTypeName();
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		// TODO move away
		public static class TypeConverter implements CMAttributeTypeVisitor {
			private String typeName;

			public TypeConverter(CMAttributeType<?> type) {
				type.accept(this);
			}

			public String getTypeName() {
				return typeName;
			}

			@Override
			public void visit(BooleanAttributeType attributeType) {
				typeName = AttributeType.BOOLEAN.toString();
			}

			@Override
			public void visit(DateTimeAttributeType attributeType) {
				typeName = AttributeType.TIMESTAMP.toString();
			}

			@Override
			public void visit(DateAttributeType attributeType) {
				typeName = AttributeType.DATE.toString();
			}

			@Override
			public void visit(DecimalAttributeType attributeType) {
				typeName = AttributeType.DECIMAL.toString();
			}

			@Override
			public void visit(DoubleAttributeType attributeType) {
				typeName = AttributeType.DOUBLE.toString();
			}

			@Override
			public void visit(EntryTypeAttributeType attributeType) {
				typeName = AttributeType.REGCLASS.toString();
			}

			@Override
			public void visit(ForeignKeyAttributeType attributeType) {
				typeName = AttributeType.FOREIGNKEY.toString();
			}

			@Override
			public void visit(GeometryAttributeType attributeType) {
				typeName = "unsupported";
			}

			@Override
			public void visit(IntegerAttributeType attributeType) {
				typeName = AttributeType.INTEGER.toString();
			}

			@Override
			public void visit(IpAddressAttributeType attributeType) {
				typeName = AttributeType.INET.toString();
			}

			@Override
			public void visit(LookupAttributeType attributeType) {
				typeName = AttributeType.LOOKUP.toString();
			}

			@Override
			public void visit(ReferenceAttributeType attributeType) {
				typeName = AttributeType.REFERENCE.toString();
			}

			@Override
			public void visit(StringAttributeType attributeType) {
				typeName = AttributeType.STRING.toString();
			}

			@Override
			public void visit(TextAttributeType attributeType) {
				typeName = AttributeType.TEXT.toString();
			}

			@Override
			public void visit(TimeAttributeType attributeType) {
				typeName = AttributeType.TIME.toString();
			}

			@Override
			public void visit(StringArrayAttributeType stringArrayAttributeType) {
				typeName = AttributeType.STRINGARRAY.toString();
			}
		};
	}
}
