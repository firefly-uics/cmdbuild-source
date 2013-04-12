package org.cmdbuild.elements.interfaces;

import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.attribute.*;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;

public interface IAttribute extends BaseSchema {

	public enum AttributeType {
		
		BOOLEAN("bool", Constants.Webservices.BOOLEAN_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new BooleanAttribute(schema, name, meta);
			}
		},
		INTEGER("int4", Constants.Webservices.INTEGER_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new IntegerAttribute(schema, name, meta);
			}
		},
		DECIMAL("numeric", Constants.Webservices.DECIMAL_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new DecimalAttribute(schema, name, meta);
			}
		},
		DOUBLE("float8", Constants.Webservices.DOUBLE_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new DoubleAttribute(schema, name, meta);
			}
		},
		DATE("date", Constants.Webservices.DATE_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new DateAttribute(schema, name, meta);
			}
		},
		TIMESTAMP("timestamp", Constants.Webservices.TIMESTAMP_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new DateTimeAttribute(schema, name, meta);
			}
		},
		CHAR("bpchar", Constants.Webservices.CHAR_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new CharAttribute(schema, name, meta);
			}
		},
		STRING("varchar", Constants.Webservices.STRING_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new StringAttribute(schema, name, meta);
			}
		},
		TEXT("text", Constants.Webservices.TEXT_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new TextAttribute(schema, name, meta);
			}
		},
		REFERENCE("int4", Constants.Webservices.REFERENCE_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new ReferenceAttribute(schema, name, meta);
			}
		},
		FOREIGNKEY("int4", Constants.Webservices.FOREIGNKEY_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new ForeignKeyAttribute(schema, name, meta);
			}
		},
		LOOKUP("int4", Constants.Webservices.LOOKUP_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new LookupAttribute(schema, name, meta);
			}
		},
		INET("inet", Constants.Webservices.INET_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new IPAddressAttribute(schema, name, meta);
			}
		},
		TIME("time", Constants.Webservices.TIME_TYPE_NAME) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new TimeAttribute(schema, name, meta);
			}
		},
		REGCLASS("regclass") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new RegclassAttribute(schema, name, meta);
			}
		},
		POINT("POINT") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new GeometryAttribute(schema, name, meta, this);
			}
		},
		BINARY("bytea") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new BinaryAttribute(schema, name, meta);
			}
		},
		INTARRAY("_int4") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new IntArrayAttribute(schema, name, meta);
			}
		},
		STRINGARRAY("_varchar") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta) {
				return new StringArrayAttribute(schema, name, meta);
			}
		};

		private final String dbStr;
		private final String wsName;

		AttributeType(String dbStr) {
			this(dbStr, null);
		}

		AttributeType(String dbStr, String wsName) {
			this.dbStr = dbStr;
			this.wsName = wsName;
		}

		public String toDBString() {
			return dbStr;
		}

		public boolean isReserved() {
			return (wsName == null);
		}

		public abstract AttributeImpl createAttribute(BaseSchema schema, String name, Map<String, String> meta);

		public static AttributeType fromDBString(String typeName) {
			for (AttributeType type : AttributeType.values()) {
				if (typeName.equals(type.toDBString())) {
					return type;
				}
			}
			return null;
		}

		public String wsName() {
			return (wsName == null) ? Constants.Webservices.UNKNOWN_TYPE_NAME: wsName;
		}
		
	}

	public enum FieldMode {
		WRITE("write"), READ("read"), HIDDEN("hidden");

		private final String fieldMode;

		FieldMode(String mode) {
			this.fieldMode = mode;
		}

		public String getMode() {
			return fieldMode;
		}

		public static FieldMode getValueOf(String mode) {
			FieldMode[] fmodes = FieldMode.values();
			try {
				for (FieldMode m : fmodes) {
					if (m.getMode().equals(mode)) {
						return FieldMode.valueOf(m.toString());
					}
				}
				return FieldMode.WRITE;
			} catch (IllegalArgumentException e) {
				return FieldMode.WRITE;
			}
		}
	}

	/*
	 * Index meaning: -2 : new attribute: you should never see this on DB -1 :
	 * reserved index: index for reserved attributes 0 : index not assigned >0 :
	 * index assigned
	 */
	public static final int NEWINDEX = -2;
	public static final int RESERVEDINDEX = -1;

	public AttributeType getType();

	public void save() throws ORMException;

	public BaseSchema getSchema();

	public void setSchema(BaseSchema schema);

	public int getLength();

	public void setLength(int length);

	public int getPrecision();

	public void setPrecision(int precision);

	public int getScale();

	public void setScale(int scale);

	public boolean isNotNull();

	public void setNotNull(boolean isNotNull);

	public boolean isUnique();

	public void setUnique(boolean isUnique);

	public String getDefaultValue();

	public void setDefaultValue(String defaultValue);

	public String getDescription();

	public void setDescription(String description);

	public int getIndex();

	public void setIndex(int index);

	public boolean isBaseDSP();

	public void setBaseDSP(boolean isBaseDSP);

	public boolean isLocal();

	public IDomain getReferenceDomain();

	public DirectedDomain getReferenceDirectedDomain();

	public ITable getReferenceTarget();

	public void setReferenceDomain(IDomain domain);

	public void setReferenceDomain(String domainName) throws NotFoundException;

	public void setReferenceDomain(int idDomain) throws NotFoundException;

	public String getReferenceType();

	public void setReferenceType(String referenceType);

	public boolean isReferenceDirect();

	public void setIsReferenceDirect(boolean isReferenceDirect);

	public void setFilter(String referenceQuery);

	public void setFilterSafe(String referenceQuery);

	public String getFilter();

	public void setFKTargetClass(String value);

	public ITable getFKTargetClass();

	public LookupType getLookupType();

	public void setLookupType(String lookupName);

	public void setFieldMode(String modeName);

	public FieldMode getFieldMode();

	public boolean isDisplayable();

	public boolean isReserved();

	public void delete();

	public void setClassOrder(int classOrder);

	public int getClassOrder();

	public String valueToString(Object value);

	public String valueToDBFormat(Object value);

	public Object readValue(Object value);

	public String getGroup();

	public void setGroup(String value);

	public String getEditorType();

	public void setEditorType(String editorType);
}