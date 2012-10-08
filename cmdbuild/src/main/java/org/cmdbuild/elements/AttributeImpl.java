package org.cmdbuild.elements;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.dao.type.SQLQuery;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.utils.CQLFacadeCompiler;

public abstract class AttributeImpl extends BaseSchemaImpl implements IAttribute {

	private static final long serialVersionUID = 1L;

	public enum AttributeDataDefinitionMeta {
		MODE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setMode(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getMode().getModeString();
			}
		},
		DESCR {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setDescription(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getDescription();
			}
		},
		BASEDSP {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setBaseDSP(Boolean.parseBoolean(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Boolean.toString(attribute.isBaseDSP());
			}
		},
		STATUS {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setStatus(SchemaStatus.fromStatusString(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getStatus().commentString();
			}
		},
		REFERENCEDOM {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setReferenceDomain(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				if (attribute.getType() == AttributeType.REFERENCE && attribute.getReferenceDomain() != null) {
					return attribute.getReferenceDomain().getName();
				} else {
					return null;
				}
			}
		},
		FKTARGETCLASS {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setFKTargetClass(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				if (attribute.getType() == AttributeType.FOREIGNKEY && attribute.getFKTargetClass() != null) {
					return attribute.getFKTargetClass().getName();
				} else {
					return null;
				}
			}
		},
		REFERENCEDIRECT {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setIsReferenceDirect(Boolean.parseBoolean(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				if (attribute.getType() == AttributeType.REFERENCE) {
					return Boolean.toString(attribute.isReferenceDirect());
				} else {
					return null;
				}
			}
		},
		REFERENCETYPE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setReferenceType(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				if (attribute.getType() == AttributeType.REFERENCE) {
					return attribute.getReferenceType();
				} else {
					return null;
				}
			}
		},
		LOOKUP {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setLookupType(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				if (attribute.getType() == AttributeType.LOOKUP && attribute.getLookupType() != null) {
					return attribute.getLookupType().getType();
				} else {
					return null;
				}
			}
		},
		FIELDMODE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setFieldMode(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getFieldMode().getMode();
			}
		},
		CLASSORDER {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				int classOrder;
				try {
					classOrder = Integer.valueOf(value);
				} catch (NumberFormatException e) {
					classOrder = 0;
				}
				attribute.setClassOrder(classOrder);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Integer.toString(attribute.getClassOrder());
			}
		},
		FILTER {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setFilter(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getFilter();
			}
		},
		INDEX {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setIndex(Integer.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Integer.toString(attribute.getIndex());
			}
		},
		// not from comments
		LENGTH {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setLength(Integer.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Integer.toString(attribute.getLength());
			}
		},
		PRECISION {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setPrecision(Integer.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Integer.toString(attribute.getPrecision());
			}
		},
		SCALE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setScale(Integer.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Integer.toString(attribute.getScale());
			}
		},
		NOTNULL {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setNotNull(Boolean.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Boolean.toString(attribute.isNotNull());
			}
		},
		UNIQUE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setUnique(Boolean.valueOf(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Boolean.toString(attribute.isUnique());
			}
		},
		DEFAULT {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setDefaultValue(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getDefaultValue();
			}
		},
		GROUP {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setGroup(value);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getGroup();
			}
		},
		LOCAL {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
				attribute.setIsLocal(Boolean.parseBoolean(value));
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return Boolean.toString(attribute.isLocal());
			}
		},
		EDITORTYPE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String editorType) {
				attribute.setEditorType(editorType);
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getEditorType();
			}
		},
		// computed
		TYPE {
			@Override
			protected void setValueNotNull(AttributeImpl attribute, String value) {
			}
			@Override
			protected String getValue(IAttribute attribute) {
				return attribute.getType().toString();
			}
		},
		// deprecated
		COLOR, FONTCOLOR, LINEAFTER;

		protected String getValue(IAttribute attribute) {
			return null;
		}

		protected final void setValue(AttributeImpl attribute, String value) {
			if (value != null) {
				setValueNotNull(attribute, value);
			}
		}

		protected void setValueNotNull(AttributeImpl attribute, String value) {
			Log.PERSISTENCE.info(String.format("Found legacy meta-attribute %s for attribute %s.%s", name(), attribute.getSchema().getName(), attribute.getName()));
		}
	}

	private BaseSchema schema;

	private int length;
	private int precision;
	private int scale;
	private boolean isNotNull;
	private boolean isUnique;
	private String defaultValue;

	private String description;
	private int index;
	private int classOrder;
	private boolean isBaseDSP;
	private boolean isLocal;
	private String group;

	private IDomain referenceDomain;
	private String referenceType = "restrict"; //TODO 
	private boolean isReferenceDirect; 
	private String referenceFilter = null;

	private String editorType;

	private LookupType lookupType;

	private FieldMode fieldMode;

	public static IAttribute create(BaseSchema schema, String name, AttributeType type, Map<String,String> meta) {
		AttributeImpl attribute = type.createAttribute(schema, name, meta);
		return attribute;
	}

	public static IAttribute create(BaseSchema schema, String name, AttributeType type) {
		return create(schema, name, type, null);
	}

	protected AttributeImpl(BaseSchema schema, String name, Map<String,String> meta) {
		this.schema = schema;
		this.name = name;
		mode = Mode.WRITE;
		fieldMode = FieldMode.WRITE;
		status = SchemaStatus.ACTIVE;
		referenceType = "restrict"; //TODO
		isReferenceDirect = false;
		index = NEWINDEX;
		isLocal = true;
		if (meta != null) {
			readDataDefinitionMeta(meta);
		}
	}

	public void setName(String name) {
		if (isNew()) {
			super.setName(name);
		} else {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

	public boolean isNew() {
		return (index == NEWINDEX);
	}

	abstract public AttributeType getType();

	@Override
	public void readDataDefinitionMeta(final Map<String, String> dataDefinitionMeta) {
		for (Entry<String, String> entry : dataDefinitionMeta.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			AttributeDataDefinitionMeta ddm = null;
			try {
				ddm = AttributeDataDefinitionMeta.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.PERSISTENCE.warn(String.format("Meta-attribute %s not valid for attribute %s.%s", name, this.getSchema().getName(), this.getName()));
			}
			if (ddm != null) {
				ddm.setValue(this, value);
			}
		}
	}

	@Override
	public Map<String, String> genDataDefinitionMeta() {
		Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		for (AttributeDataDefinitionMeta meta : AttributeDataDefinitionMeta.values()) {
			String value = meta.getValue(this);
			if (value != null) {
				dataDefinitionMeta.put(meta.name(), value);
			}
		}
		return dataDefinitionMeta;
	}

	public void save() {
		if (isNew()) {
			setIndexToOnePastLast();
			backend.createAttribute(this);
		} else {
			backend.modifyAttribute(this);
		}
	}

	private void setIndexToOnePastLast() {
		int index = 0;
		for (IAttribute a : this.getSchema().getAttributes().values())
			index = Math.max(index, a.getIndex());
		this.setIndex(index+1);
	}

	public void delete() {
		backend.deleteAttribute(this);
		MetadataService.deleteMetadata(this);
	}

	public BaseSchema getSchema() {
		return schema;
	}

	public void setSchema(BaseSchema schema) {
		this.schema = schema;
	}

	public void setTableType(CMTableType type) {
		throw new UnsupportedOperationException();
	}

	private void setIsLocal(boolean isLocal) {
		this.isLocal = isLocal; 
	}

	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getPrecision() {
		return precision;
	}
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public boolean isNotNull() {
		return isNotNull;
	}
	public void setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}
	public boolean isUnique() {
		return isUnique;
	}
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isBaseDSP() {
		return isBaseDSP;
	}

	public void setBaseDSP(boolean isBaseDSP) {
		this.isBaseDSP = isBaseDSP;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public IDomain getReferenceDomain() {
		return referenceDomain;
	}

	public DirectedDomain getReferenceDirectedDomain() {
		return DirectedDomain.create(referenceDomain, isReferenceDirect);
	}

	public ITable getReferenceTarget() {
		if (isReferenceDirect) {
			return referenceDomain.getClass2();
		} else {
			return referenceDomain.getClass1();
		}
	}

	public void setReferenceDomain(IDomain domain) {
		this.referenceDomain = domain;
	}

	public void setReferenceDomain(String domainName) throws NotFoundException {
		if (domainName != null && domainName.length() > 0)
			this.referenceDomain = backend.getDomain(domainName);
	}

	public void setReferenceDomain(int idDomain) throws NotFoundException {
		this.referenceDomain = backend.getDomain(idDomain);
	}

	public String getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	public boolean isReferenceDirect() {
		return isReferenceDirect;
	}

	public void setIsReferenceDirect(boolean isReferenceDirect) {
		this.isReferenceDirect = isReferenceDirect;
	}

	public void setFilter(String filter) {
		if (filter == null || filter.trim().equals("") || this.getType() != AttributeType.REFERENCE) {
			this.referenceFilter = null;
		} else {
			this.referenceFilter = filter;
		}
	}

	public void setFilterSafe(String referenceQuery) {
		if (isValidReferenceFilter(referenceQuery)) {
			this.setFilter(referenceQuery);
		} else {
			throw ORMExceptionType.ORM_CQL_COMPILATION_FAILED.createException();
		}
	}

	private boolean isValidReferenceFilter(String filter) {
		boolean valid = true;
		if (filter != null && !filter.trim().equals("")) {
			try {
				QueryImpl q = CQLFacadeCompiler.compileWithTemplateParams(filter);
				valid = getReferenceTarget().treeBranch().contains(q.getFrom().mainClass().getClassName());
			} catch (Exception e) {
				valid = false;
			}
		}
		return valid;
	}

	public ITable getFKTargetClass() {
		return null;
	}

	public void setFKTargetClass(String value) {
		throw new UnsupportedOperationException();
	}
	
	public String getFilter() {
		if (!isValidReferenceFilter(referenceFilter)) {
			setFilter(null);
		}
		return referenceFilter;
	}

	public LookupType getLookupType() {
		return lookupType;
	}

	public void setLookupType(String lookupName) {
		if (lookupName != null && lookupName.length() > 0) {
			this.lookupType = backend.getLookupTypeOrDie(lookupName);
		}
	}

	public void setFieldMode(String modeName) {
		FieldMode mode = FieldMode.getValueOf(modeName);
		this.fieldMode = mode;
	}

	public FieldMode getFieldMode() {
		return fieldMode;
	}

	public void setClassOrder(int classOrder) {
		this.classOrder = classOrder;
	}

	public int getClassOrder() {
		return this.classOrder;
	}

	protected String escapeAndQuote(String value) {
		return "'" + pgEscape(value) + "'";
	}

	public static String pgEscape(String value) {
		return value.replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
	}
	
	public boolean isReserved() {
		return (this.getMode() == Mode.RESERVED);
	}
	
	public boolean isDisplayable() {
		return (this.getMode() != Mode.RESERVED
					|| ICard.CardAttributes.Notes.toString().equals(this.getDBName()))
				&& (this.getFieldMode() != FieldMode.HIDDEN);
	}

	public final Object readValue(Object maybeValue) {
		if (maybeValue == null) {
			return null;
		} else if (maybeValue instanceof SQLQuery) {
			// Skip value conversion for those LOVELY SQL inected strings
			return maybeValue;
		} else {
			return convertValue(maybeValue);
		}
	}

	protected abstract Object convertValue(Object value);

	public final String valueToString(Object value) {
		if (value == null) {
			return "";
		} else {
			return notNullValueToString(value);
		}
	}

	protected String notNullValueToString(Object value) {
		return value.toString();
	}

	public final String valueToDBFormat(Object value) {
		if (value == null) {
			return "NULL";
		} else {
			return notNullValueToDBFormat(value);
		}
	}

	protected String notNullValueToDBFormat(Object value) {
		return value.toString();
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getEditorType() {
		return this.editorType;
	}

	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}
}
