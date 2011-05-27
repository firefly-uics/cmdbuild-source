package org.cmdbuild.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.cmdbuild.elements.AttributeImpl.AttributeDataDefinitionMeta;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SchemaCache;

public class DomainImpl extends BaseSchemaImpl implements IDomain {

	private static final long serialVersionUID = 1L;

	public enum DomainDataDefinitionMeta {
		MODE {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setMode(value);
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getMode().getModeString();
			}
		},
		LABEL { // LABEL?! Why not DESCR?!
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setDescription(value);
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getDescription();
			}
		},
		STATUS {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setStatus(SchemaStatus.fromStatusString(value));
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getStatus().commentString();
			}
		},
		TYPE {
			@Override
			public void setValue(IDomain domain, String value) {
				CMTableType type;
				try {
					type = CMTableType.fromMetaValue(value);
				} catch (Exception e) {
					type = CMTableType.DOMAIN;
					Log.PERSISTENCE.warn(String.format("Wrong type (%s) for domain %s, using %s",
							value, domain.getName(), type));
				}
				domain.setTableType(type);
			}
			@Override
			public String getValue(IDomain table) {
				return table.getTableType().toMetaValue();
			}
		},
		MASTERDETAIL {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setMasterDetail(Boolean.parseBoolean(value));
			}
			@Override
			public String getValue(IDomain domain) {
				return Boolean.toString(domain.isMasterDetail());
			}
		},
		DESCRDIR {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setDescriptionDirect(value);
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getDescriptionDirect();
			}
		},
		DESCRINV {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setDescriptionInverse(value);
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getDescriptionInverse();
			}
		},
		CARDIN {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setCardinality(value);
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getCardinality();
			}
		},
		OPENEDROWS {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setOpenedRows(Integer.parseInt(value));
			}
			@Override
			public String getValue(IDomain domain) {
				return Integer.toString(domain.getOpenedRows());
			}
		},
		CLASS1 {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setClass1(TableImpl.get(value));
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getClass1().getName();
			}
		},
		CLASS2 {
			@Override
			public void setValue(IDomain domain, String value) {
				domain.setClass2(TableImpl.get(value));
			}
			@Override
			public String getValue(IDomain domain) {
				return domain.getClass2().getName();
			}
		};

		public String getValue(IDomain domain) {
			return null;
		}
		public void setValue(IDomain domain, String value) {
			Log.PERSISTENCE.info(String.format("Found legacy meta-attribute %s for domain %s", name(), domain.getName()));
		}

		static DomainDataDefinitionMeta caseInsensitiveValueOf(String name) {
			for (DomainDataDefinitionMeta item : DomainDataDefinitionMeta.values()) {
				// it is okay to throw null pointer exception if name is null
				if (name.equalsIgnoreCase(item.name())) {
					return item;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	public static final String DomainTablePrefix = "Map_";

	private int oid;
	private ITable[] tables;

	private String cardinality;
	private int openedRows;
	private boolean isMasterDetail;

	private String description;
	private String descriptionDirect;
	private String descriptionInverse;

	public static IDomain getBase(){
		try {
			return SchemaCache.getInstance().getDomain("");
		} catch (NotFoundException e) {
			Log.PERSISTENCE.debug("Unable to find Map domain !!!", e);
			return null;
		}
	}

	DomainImpl() {
		this.setTableType(CMTableType.DOMAIN);
		this.mode = Mode.RESERVED;
		this.tables = new ITable[2];
	}

	/*
	 * Used by the backend... public because it needs refactoring
	 */
	public DomainImpl(String name, String comment, int oid) throws NotFoundException {
		this();
		this.oid = oid;
		setName(name);
		readDataDefinitionMeta(backend.parseComment(comment));
	}

    IDomain get(String domainName) throws NotFoundException {
    	return SchemaCache.getInstance().getDomain(domainName);
    }

    IDomain get(int idClass) throws NotFoundException {
    	return SchemaCache.getInstance().getDomain(idClass);
    }

	public boolean isNew() {
		return (oid <= 0);
	}

	protected Map<String, IAttribute> loadAttributes() {
		Map<String, IAttribute> a = super.loadAttributes();
		a.put("Id", AttributeImpl.create(this, "Id", AttributeType.INTEGER, notNullMeta()));
		a.put("IdDomain", AttributeImpl.create(this, "IdDomain", AttributeType.REGCLASS, notNullMeta()));
		a.put("IdClass1", AttributeImpl.create(this, "IdClass1", AttributeType.REGCLASS, notNullMeta()));
		a.put("IdObj1", AttributeImpl.create(this, "IdObj1", AttributeType.INTEGER, notNullMeta()));
		a.put("IdClass2", AttributeImpl.create(this, "IdClass2", AttributeType.REGCLASS, notNullMeta()));
		a.put("IdObj2", AttributeImpl.create(this, "IdObj2", AttributeType.INTEGER, notNullMeta()));
		a.put("Status", AttributeImpl.create(this, "Status", AttributeType.CHAR, notNullMeta()));
		a.put("User", AttributeImpl.create(this, "User", AttributeType.STRING, notNullMeta()));
		a.put("BeginDate", AttributeImpl.create(this, "BeginDate", AttributeType.TIMESTAMP, nillableMeta()));
		a.put("EndDate", AttributeImpl.create(this, "EndDate", AttributeType.TIMESTAMP, nillableMeta()));
		return a;
	}

	private Map<String,String> notNullMeta() {
		Map<String,String> notNullMeta = new HashMap<String,String>();
		notNullMeta.put(AttributeDataDefinitionMeta.NOTNULL.toString(), Boolean.TRUE.toString());
		return notNullMeta;
	}

	private Map<String,String> nillableMeta() {
		Map<String,String> notNullMeta = new HashMap<String,String>();
		notNullMeta.put(AttributeDataDefinitionMeta.NOTNULL.toString(), Boolean.FALSE.toString());
		return notNullMeta;
	}

	@Override
	public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) {
		for (Entry<String, String> entry : dataDefinitionMeta.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			DomainDataDefinitionMeta ddm = null;
			try {
				ddm = DomainDataDefinitionMeta.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.PERSISTENCE.warn(String.format("Meta-attribute %s not valid for domain %s", name, this.getName()));
			}
			if (ddm != null) {
				ddm.setValue(this, value);
			}
		}
	}

	@Override
	public Map<String, String> genDataDefinitionMeta() {
		Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		for (DomainDataDefinitionMeta meta : DomainDataDefinitionMeta.values()) {
			String value = meta.getValue(this);
			if (value != null) {
				dataDefinitionMeta.put(meta.name(), value);
			}
		}
		return dataDefinitionMeta;
	}

	public void save(){
		try {
			if(isNew())
				oid = backend.createDomain(this);
			else
				backend.modifyDomain(this);
		} catch (RuntimeException re) {
			// On errors, the cache must be refreshed
			SchemaCache.getInstance().refreshDomains();
			throw re;
		}
	}

	public void delete() {
		backend.deleteDomain(this);
	}

	public String getDBNameNotQuoted() {
		if (name == null) {
			return "Map";
		} else {
			return DomainTablePrefix + name;
		}
	}

	public void setTableType(CMTableType type) {
		if (type != CMTableType.DOMAIN) {
			type = CMTableType.DOMAIN;
			Log.PERSISTENCE.warn(String.format("Inconsistent type (%s) for table %s, using %s",
					type.toMetaValue(), getName(), type));
		}
		super.setTableType(type);
	}

	public String getDescriptionDirect() {
		return descriptionDirect;
	}

	public void setDescriptionDirect(String descriptionDirect) {
		this.descriptionDirect = descriptionDirect;
	}

	public String getDescriptionInverse() {
		return descriptionInverse;
	}

	public void setDescriptionInverse(String descriptionInverse) {
		this.descriptionInverse = descriptionInverse;
	}

	@Deprecated
	public ITable[] getTables() {
		return tables;
	}

	@Deprecated
	public void setTables(ITable[] tables) {
		this.tables = tables;
	}

	public ITable getClass1() {
		return tables[0];
	}

	public ITable getClass2() {
		return tables[1];
	}

	public void setClass1(ITable table) {
		this.tables[0] = table;
	}

	public void setClass2(ITable table) {
		this.tables[1] = table;
	}

	public int getId() {
		return oid;
	}

	public String getCardinality() {
		return cardinality;
	}

	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
	}

	public String getDescription() {
		if(this.description==null || this.description.trim().equals(""))
			return getName();
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMasterDetail() {
		return isMasterDetail;
	}

	public void setMasterDetail(boolean isMasterDetail) {
		this.isMasterDetail = isMasterDetail;
	}

	public void setOpenedRows(int openedRows) {
		this.openedRows = openedRows;
	}

	public int getOpenedRows() {
		return openedRows;
	}

	public String getType() {
		return "domain";
	}

	public boolean isLocal(ITable table) {
		return (getClass1().equals(table) || getClass2().equals(table));
	}

	public boolean getDirectionFrom(ITable sourceTable) throws ORMException {
		TableTree tree = TableImpl.tree();
		boolean directed = tree.branch(this.getClass1().getName()).contains(sourceTable.getName());
		boolean inverse = tree.branch(this.getClass2().getName()).contains(sourceTable.getName());
		if (directed && inverse)
			throw ORMExceptionType.ORM_AMBIGUOUS_DIRECTION.createException();
		return directed;
	}

	@Override
	public void reloadCache() {
		SchemaCache.getInstance().refreshDomains();
	}
}
