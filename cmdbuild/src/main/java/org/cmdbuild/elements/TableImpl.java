package org.cmdbuild.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.cmdbuild.dao.attribute.ForeignKeyAttribute;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;

public class TableImpl extends BaseSchemaImpl implements ITable {

	private static final long serialVersionUID = 1L;

	public enum TableDataDefinitionMeta {
		MODE {
			@Override
			public void setValue(ITable table, String value) {
				table.setMode(value);
			}

			@Override
			public String getValue(ITable table) {
				return table.getMode().getModeString();
			}
		},
		DESCR {
			@Override
			public void setValue(ITable table, String value) {
				table.setDescription(value);
			}

			@Override
			public String getValue(ITable table) {
				return table.getDescription();
			}
		},
		STATUS {
			@Override
			public void setValue(ITable table, String value) {
				table.setStatus(SchemaStatus.fromStatusString(value));
			}

			@Override
			public String getValue(ITable table) {
				return table.getStatus().commentString();
			}
		},
		TYPE {
			@Override
			public void setValue(ITable table, String value) {
				CMTableType type;
				try {
					type = CMTableType.fromMetaValue(value);
				} catch (Exception e) {
					type = CMTableType.CLASS;
					Log.PERSISTENCE.warn(String.format("Wrong type (%s) for table %s, using %s", value,
							table.getName(), type));
				}
				table.setTableType(type);
			}

			@Override
			public String getValue(ITable table) {
				return table.getTableType().toMetaValue();
			}
		},
		SUPERCLASS {
			@Override
			public void setValue(ITable table, String value) {
				table.setSuperClass(Boolean.parseBoolean(value));
			}

			@Override
			public String getValue(ITable table) {
				return Boolean.toString(table.isSuperClass());
			}
		},
		MANAGER;

		public String getValue(ITable table) {
			return null;
		}

		public void setValue(ITable table, String value) {
			Log.PERSISTENCE.info(String.format("Found legacy meta-attribute %s for table %s", name(), table.getName()));
		}

		static TableDataDefinitionMeta caseInsensitiveValueOf(String name) {
			for (TableDataDefinitionMeta item : TableDataDefinitionMeta.values()) {
				// it is okay to throw null pointer exception if name is null
				if (name.equalsIgnoreCase(item.name())) {
					return item;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	private ITable parent = null;

	private String description = "";
	private boolean isSuperClass = false;

	TableImpl() {
		setTableType(CMTableType.CLASS);
		mode = Mode.RESERVED;
		status = SchemaStatus.ACTIVE;
	}

	public TableImpl(String name, String comment, int oid) {
		super();
		this.oid = oid;
		setName(name);
		readDataDefinitionMeta(backend.parseComment(comment));
	}

	public boolean equals(Object o) {
		if (o instanceof ITable)
			return ((ITable) o).getName().equals(name);
		return false;
	}

	@Override
	public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) {
		for (Entry<String, String> entry : dataDefinitionMeta.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			TableDataDefinitionMeta ddm = null;
			try {
				ddm = TableDataDefinitionMeta.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.PERSISTENCE.warn(String.format("Meta-attribute %s not valid for table %s", name, this.getName()));
			}
			if (ddm != null) {
				ddm.setValue(this, value);
			}
		}
	}

	@Override
	public Map<String, String> genDataDefinitionMeta() {
		Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		for (TableDataDefinitionMeta meta : TableDataDefinitionMeta.values()) {
			String value = meta.getValue(this);
			if (value != null) {
				dataDefinitionMeta.put(meta.name(), value);
			}
		}
		return dataDefinitionMeta;
	}

	public void save() throws ORMException {
		try {
			if (isNew()) {
				oid = backend.createTable(this);
			} else {
				backend.modifyTable(this);
			}
			// TODO Change with something better AFTER there is a real table
			// tree
			SchemaCache.getInstance().refreshTables();
		} catch (RuntimeException re) {
			// On errors, the cache must be refreshed
			SchemaCache.getInstance().refreshTables();
			throw re;
		}
	}

	public void delete() {
		backend.deleteTable(this);
		MetadataService.deleteMetadata(this);
	}

	public boolean isNew() {
		return (oid <= 0);
	}

	public void setTableType(CMTableType type) {
		if (type != CMTableType.CLASS && type != CMTableType.SIMPLECLASS) {
			type = CMTableType.CLASS;
			Log.PERSISTENCE.warn(String.format("Inconsistent type (%s) for table %s, using %s", type.toMetaValue(),
					getName(), type));
		}
		super.setTableType(type);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setSuperClass(boolean isSuperClass) {
		this.isSuperClass = isSuperClass;
	}

	public boolean isSuperClass() {
		return isSuperClass;
	}

	public ITable getParent() {
		return parent;
	}

	public void setParent(String parent) throws NotFoundException {
		this.parent = TableImpl.get(parent);
	}

	public void setParent(Integer parent) throws NotFoundException {
		this.parent = TableImpl.get(parent);
	}

	public void setParent(ITable parent) {
		this.parent = parent;
	}

	public String toString() {
		return getDescription();
	}

	public CardFactory cards() {
		return new CardFactoryImpl(this, UserContext.systemContext());
	}

	static ITable get(String className) throws NotFoundException {
		return SchemaCache.getInstance().getTable(className);
	}

	static ITable get(int idClass) throws NotFoundException {
		return SchemaCache.getInstance().getTable(idClass);
	}

	public static Iterable<ITable> list() {
		return SchemaCache.getInstance().getTableList();
	}

	public static Iterable<ITable> list(CMTableType type) {
		List<ITable> list = new ArrayList<ITable>();
		for (ITable table : list()) {
			if (type == null || type.equals(table.getTableType())) {
				list.add(table);
			}
		}
		return list;
	}

	public TableTree treeBranch() {
		return TableImpl.tree().branch(this.getName());
	}

	public ArrayList<ITable> getChildren() {
		TableTree branch = treeBranch();
		ArrayList<ITable> children = new ArrayList<ITable>();
		for (ITable child : branch) {
			children.add(child);
		}
		return children;
	}

	public boolean hasChild() {
		return treeBranch().getLeaves().isEmpty();
	}

	@SuppressWarnings("deprecation")
	public static TableTree tree() {
		return SchemaCache.getInstance().getTableTree();
	}

	public class OrderEntry {
		private String attributeName;
		private OrderFilterType orderDirection;

		private OrderEntry(String attributeName, OrderFilterType orderDirection) {
			this.attributeName = attributeName;
			this.orderDirection = orderDirection;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public OrderFilterType getOrderDirection() {
			return orderDirection;
		}
	}

	public List<OrderEntry> getOrdering() {
		List<OrderEntry> orderingList = new LinkedList<OrderEntry>();
		List<IAttribute> sortedAttributes = getAttributesSortedByClassOrder();
		OrderEntry orderEntry = null;

		if (sortedAttributes.isEmpty()) {
			if (getAttributes().containsKey(ICard.CardAttributes.Description.toString())) {
				orderEntry = new OrderEntry(ICard.CardAttributes.Description.toString(), OrderFilterType.ASC);
			} else if (getAttributes().containsKey(ICard.CardAttributes.BeginDate.toString())) {
				orderEntry = new OrderEntry(ICard.CardAttributes.BeginDate.toString(), OrderFilterType.DESC);
			}
			if (orderEntry != null) {
				orderingList.add(orderEntry);
			}
		} else {
			for (IAttribute attr : sortedAttributes) {
				OrderFilterType orderType = attr.getClassOrder() > 0 ? OrderFilterType.ASC : OrderFilterType.DESC;
				OrderEntry descriptionOrder = new OrderEntry(attr.getName(), orderType);
				orderingList.add(descriptionOrder);
			}
		}
		return orderingList;
	}

	private List<IAttribute> getAttributesSortedByClassOrder() {
		Collection<IAttribute> attributes = getAttributes().values();
		List<IAttribute> attributesForOrdering = new LinkedList<IAttribute>();
		for (IAttribute attr : attributes) {
			if (attr.getClassOrder() != 0)
				attributesForOrdering.add(attr);
		}
		Collections.sort(attributesForOrdering, new ClassOrderComparator());
		return attributesForOrdering;
	}

	private class ClassOrderComparator implements Comparator<IAttribute> {
		public int compare(IAttribute a1, IAttribute a2) {
			int classOrder1 = a1.getClassOrder();
			int classOrder2 = a2.getClassOrder();

			if (classOrder1 > classOrder2)
				return 1;
			else if (classOrder1 < classOrder2)
				return -1;
			else
				return 0;
		}
	}

	@Override
	public void reloadCache() {
		SchemaCache.getInstance().refreshTables();
	}

	public boolean isActivity() {
		return UserContext.systemContext().tables().fullTree().branch(ProcessType.BaseTable).contains(this.getName());
	}

	public boolean isTheTableClass() {
		return ITable.BaseTable.equals(getName());
	}

	public boolean isTheTableActivity() {
		return ProcessType.BaseTable.equals(getName());
	}

	public boolean isAllowedOnTrees() {
		return getMode().isCustom() || isActivity();
	}

	@Override
	public Iterable<IAttribute> fkDetails() {
		List<IAttribute> fkDetails = new ArrayList<IAttribute>();
		for (ITable simpleClass : list(CMTableType.SIMPLECLASS)) {
			for (IAttribute attribute : simpleClass.getAttributes().values()) {
				if (isTargetOfFK(attribute)) {
					fkDetails.add(attribute);
				}
			}
		}
		return fkDetails;
	}

	private boolean isTargetOfFK(IAttribute attribute) {
		if (attribute instanceof ForeignKeyAttribute) {
			return ((ForeignKeyAttribute) attribute).getFKTargetClass().treeBranch().contains(this.getId());
		} else {
			return false;
		}
	}
}
