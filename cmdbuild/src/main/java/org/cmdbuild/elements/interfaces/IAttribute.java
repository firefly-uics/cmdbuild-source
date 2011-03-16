package org.cmdbuild.elements.interfaces;

import org.cmdbuild.dao.attribute.*;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;

public interface IAttribute extends BaseSchema {

	public enum AttributeType {
		BOOLEAN("bool") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new BooleanAttribute(schema, name);
			}
		}, INTEGER("int4") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new IntegerAttribute(schema, name);
			}
		}, DECIMAL("numeric") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new DecimalAttribute(schema, name);
			}
		}, DOUBLE("float8") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new DoubleAttribute(schema, name);
			}
		}, DATE("date") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new DateAttribute(schema, name);
			}
		}, TIMESTAMP("timestamp") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new DateTimeAttribute(schema, name);
			}
		}, CHAR("bpchar") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new CharAttribute(schema, name);
			}
		}, STRING("varchar") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new StringAttribute(schema, name);
			}
		}, TEXT("text") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new TextAttribute(schema, name);
			}
		}, REFERENCE("int4") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new ReferenceAttribute(schema, name);
			}
		}, FOREIGNKEY("int4") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new ForeignKeyAttribute(schema, name);
			}
		}, LOOKUP("int4") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new LookupAttribute(schema, name);
			}
		}, INET("inet") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new IPAddressAttribute(schema, name);
			}
		}, TIME("time") {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new TimeAttribute(schema, name);
			}
		}, REGCLASS("regclass", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new RegclassAttribute(schema, name);
			}
		}, POINT("POINT", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new GeometryAttribute(schema, name, this);
			}
		}, LINESTRING("LINESTRING", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new GeometryAttribute(schema, name, this);
			}
		}, POLYGON("POLYGON", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new GeometryAttribute(schema, name, this);
			}
		}, BINARY("bytea", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new BinaryAttribute(schema, name);
			}
		}, INTARRAY("_int4", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new IntArrayAttribute(schema, name);
			}
		}, STRINGARRAY("_varchar", true) {
			@Override
			public AttributeImpl createAttribute(BaseSchema schema, String name) {
				return new StringArrayAttribute(schema, name);
			}
		};

		private final String dbStr;
		private final boolean reserved;
		AttributeType(String dbStr) {
			this.dbStr = dbStr;
			this.reserved = false;
		}
		AttributeType(String dbStr, boolean reserved) {
			this.dbStr = dbStr;
			this.reserved = reserved;
		}
		public String toDBString() { return dbStr; }
		public boolean isReserved() { return reserved; }
		public abstract AttributeImpl createAttribute(BaseSchema schema, String name);
		public static AttributeType fromDBString(String typeName) {
			for (AttributeType type : AttributeType.values()){
				if(typeName.equals(type.toDBString())) {
					return type;
				}
			}
			return null;
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

		public static FieldMode getValueOf (String mode){
			FieldMode[] fmodes = FieldMode.values();
			try{
				for(FieldMode m: fmodes){
					if(m.getMode().equals(mode)){
						return FieldMode.valueOf(m.toString());
					}
				}
				return FieldMode.WRITE;
			} catch(IllegalArgumentException e){
				return FieldMode.WRITE;
			}
		}
	}

	/*
	 * Index meaning:
	 * -2 : new attribute: you should never see this on DB
	 * -1 : reserved index: index for reserved attributes
	 *  0 : index not assigned
	 * >0 : index assigned 
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
}