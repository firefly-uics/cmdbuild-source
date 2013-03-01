package org.cmdbuild.elements;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class DomainImpl extends BaseSchemaImpl implements IDomain {

	private static final long serialVersionUID = 1L;

	public enum DomainDataDefinitionMeta {
		MODE {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setMode(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getMode().getModeString();
			}
		},
		LABEL { // LABEL?! Why not DESCR?!
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setDescription(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getDescription();
			}
		},
		MDLABEL {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setMDLabel(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getMDLabel();
			}
		},
		STATUS {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setStatus(SchemaStatus.fromStatusString(value));
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getStatus().commentString();
			}
		},
		TYPE {
			@Override
			public void setValue(final IDomain domain, final String value) {
				CMTableType type;
				try {
					type = CMTableType.fromMetaValue(value);
				} catch (final Exception e) {
					type = CMTableType.DOMAIN;
					Log.PERSISTENCE.warn(String.format("Wrong type (%s) for domain %s, using %s", value,
							domain.getName(), type));
				}
				domain.setTableType(type);
			}

			@Override
			public String getValue(final IDomain table) {
				return table.getTableType().toMetaValue();
			}
		},
		MASTERDETAIL {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setMasterDetail(Boolean.parseBoolean(value));
			}

			@Override
			public String getValue(final IDomain domain) {
				return Boolean.toString(domain.isMasterDetail());
			}
		},
		DESCRDIR {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setDescriptionDirect(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getDescriptionDirect();
			}
		},
		DESCRINV {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setDescriptionInverse(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getDescriptionInverse();
			}
		},
		CARDIN {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setCardinality(value);
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getCardinality();
			}
		},
		OPENEDROWS {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setOpenedRows(Integer.parseInt(value));
			}

			@Override
			public String getValue(final IDomain domain) {
				return Integer.toString(domain.getOpenedRows());
			}
		},
		CLASS1 {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setClass1(UserOperations.from(UserContext.systemContext()).tables().get(value));
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getClass1().getName();
			}
		},
		CLASS2 {
			@Override
			public void setValue(final IDomain domain, final String value) {
				domain.setClass2(UserOperations.from(UserContext.systemContext()).tables().get(value));
			}

			@Override
			public String getValue(final IDomain domain) {
				return domain.getClass2().getName();
			}
		};

		public String getValue(final IDomain domain) {
			return null;
		}

		public void setValue(final IDomain domain, final String value) {
			Log.PERSISTENCE
					.info(String.format("Found legacy meta-attribute %s for domain %s", name(), domain.getName()));
		}

		static DomainDataDefinitionMeta caseInsensitiveValueOf(final String name) {
			for (final DomainDataDefinitionMeta item : DomainDataDefinitionMeta.values()) {
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
	private String mdLabel;

	private String description;
	private String descriptionDirect;
	private String descriptionInverse;

	DomainImpl() {
		this.setTableType(CMTableType.DOMAIN);
		this.mode = Mode.WRITE;
		this.tables = new ITable[2];
	}

	/*
	 * Used by the backend... public because it needs refactoring
	 */
	public DomainImpl(final String name, final String comment, final int oid) throws NotFoundException {
		this();
		this.oid = oid;
		setName(name);
		readDataDefinitionMeta(backend.parseComment(comment));
	}

	IDomain get(final String domainName) throws NotFoundException {
		return backend.getDomain(domainName);
	}

	IDomain get(final int idClass) throws NotFoundException {
		return backend.getDomain(idClass);
	}

	@Override
	public boolean isNew() {
		return (oid <= 0);
	}

	@Override
	public void readDataDefinitionMeta(final Map<String, String> dataDefinitionMeta) {
		for (final Entry<String, String> entry : dataDefinitionMeta.entrySet()) {
			final String name = entry.getKey();
			final String value = entry.getValue();
			DomainDataDefinitionMeta ddm = null;
			try {
				ddm = DomainDataDefinitionMeta.valueOf(name);
			} catch (final IllegalArgumentException e) {
				Log.PERSISTENCE.warn(String.format("Meta-attribute %s not valid for domain %s", name, this.getName()));
			}
			if (ddm != null) {
				ddm.setValue(this, value);
			}
		}
	}

	@Override
	public Map<String, String> genDataDefinitionMeta() {
		final Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		for (final DomainDataDefinitionMeta meta : DomainDataDefinitionMeta.values()) {
			final String value = meta.getValue(this);
			if (value != null) {
				dataDefinitionMeta.put(meta.name(), value);
			}
		}
		return dataDefinitionMeta;
	}

	@Override
	public void save() {
		if (isNew()) {
			oid = backend.createDomain(this);
		} else {
			backend.modifyDomain(this);
		}
	}

	@Override
	public void delete() {
		backend.deleteDomain(this);
	}

	@Override
	public String getDBNameNotQuoted() {
		if (name == null) {
			return "Map";
		} else {
			return DomainTablePrefix + name;
		}
	}

	@Override
	public void setTableType(CMTableType type) {
		if (type != CMTableType.DOMAIN) {
			type = CMTableType.DOMAIN;
			Log.PERSISTENCE.warn(String.format("Inconsistent type (%s) for table %s, using %s", type.toMetaValue(),
					getName(), type));
		}
		super.setTableType(type);
	}

	@Override
	public String getDescriptionDirect() {
		return descriptionDirect;
	}

	@Override
	public void setDescriptionDirect(final String descriptionDirect) {
		this.descriptionDirect = descriptionDirect;
	}

	@Override
	public String getDescriptionInverse() {
		return descriptionInverse;
	}

	@Override
	public void setDescriptionInverse(final String descriptionInverse) {
		this.descriptionInverse = descriptionInverse;
	}

	@Override
	@Deprecated
	public ITable[] getTables() {
		return tables;
	}

	@Deprecated
	public void setTables(final ITable[] tables) {
		this.tables = tables;
	}

	@Override
	public ITable getClass1() {
		return tables[0];
	}

	@Override
	public ITable getClass2() {
		return tables[1];
	}

	@Override
	public void setClass1(final ITable table) {
		this.tables[0] = table;
	}

	@Override
	public void setClass2(final ITable table) {
		this.tables[1] = table;
	}

	@Override
	public int getId() {
		return oid;
	}

	@Override
	public String getCardinality() {
		return cardinality;
	}

	@Override
	public void setCardinality(final String cardinality) {
		this.cardinality = cardinality;
	}

	@Override
	public String getDescription() {
		if (this.description == null || this.description.trim().equals(""))
			return getName();
		return this.description;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public final boolean isMasterDetail() {
		return isMasterDetail;
	}

	@Override
	public void setMasterDetail(final boolean isMasterDetail) {
		this.isMasterDetail = isMasterDetail;
	}

	@Override
	public String getMDLabel() {
		if (mdLabel != null) {
			return mdLabel;
		} else if (isMasterDetail()) { // For backwards compatibility
			if (CARDINALITY_N1.contains(cardinality)) {
				return getClass1().getDescription();
			} else if (CARDINALITY_1N.contains(cardinality)) {
				return getClass2().getDescription();
			}
		}
		return null;
	}

	@Override
	public void setMDLabel(String mdLabel) {
		if (mdLabel != null && mdLabel.trim().isEmpty()) {
			mdLabel = null;
		}
		this.mdLabel = mdLabel;
	}

	@Override
	public void setOpenedRows(final int openedRows) {
		this.openedRows = openedRows;
	}

	@Override
	public int getOpenedRows() {
		return openedRows;
	}

	@Override
	public String getType() {
		return "domain";
	}

	@Override
	public boolean isLocal(final ITable table) {
		return (getClass1().equals(table) || getClass2().equals(table));
	}

	@Override
	public boolean getDirectionFrom(final ITable sourceTable) throws ORMException {
		final TableTree tree = TableImpl.tree();
		final boolean directed = tree.branch(this.getClass1().getName()).contains(sourceTable.getName());
		final boolean inverse = tree.branch(this.getClass2().getName()).contains(sourceTable.getName());
		if (directed && inverse)
			throw ORMExceptionType.ORM_AMBIGUOUS_DIRECTION.createException();
		return directed;
	}

	@Override
	public String getPrivilegeId() {
		// Should match the new DAO implementation!
		return String.format("Domain:%d", getId());
	}
}
