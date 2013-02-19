package org.cmdbuild.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.meta.MetadataService;

public class TableImpl extends BaseSchemaImpl implements ITable {

	private static final long serialVersionUID = 1L;

	public enum TableDataDefinitionMeta {
		MODE {
			@Override
			public void setValue(final ITable table, final String value) {
				table.setMode(value);
			}

			@Override
			public String getValue(final ITable table) {
				return table.getMode().getModeString();
			}
		},
		DESCR {
			@Override
			public void setValue(final ITable table, final String value) {
				table.setDescription(value);
			}

			@Override
			public String getValue(final ITable table) {
				return table.getDescription();
			}
		},
		STATUS {
			@Override
			public void setValue(final ITable table, final String value) {
				table.setStatus(SchemaStatus.fromStatusString(value));
			}

			@Override
			public String getValue(final ITable table) {
				return table.getStatus().commentString();
			}
		},
		TYPE {
			@Override
			public void setValue(final ITable table, final String value) {
				CMTableType type;
				try {
					type = CMTableType.fromMetaValue(value);
				} catch (final Exception e) {
					type = CMTableType.CLASS;
					Log.PERSISTENCE.warn(String.format("Wrong type (%s) for table %s, using %s", value,
							table.getName(), type));
				}
				table.setTableType(type);
			}

			@Override
			public String getValue(final ITable table) {
				return table.getTableType().toMetaValue();
			}
		},
		SUPERCLASS {
			@Override
			public void setValue(final ITable table, final String value) {
				table.setSuperClass(Boolean.parseBoolean(value));
			}

			@Override
			public String getValue(final ITable table) {
				return Boolean.toString(table.isSuperClass());
			}
		},
		USERSTOPPABLE {
			@Override
			public void setValue(final ITable table, final String value) {
				table.setUserStoppable(Boolean.parseBoolean(value));
			}

			@Override
			public String getValue(final ITable table) {
				return Boolean.toString(table.isUserStoppable());
			}
		},
		MANAGER;

		public String getValue(final ITable table) {
			return null;
		}

		public void setValue(final ITable table, final String value) {
			Log.PERSISTENCE.info(String.format("Found legacy meta-attribute %s for table %s", name(), table.getName()));
		}

		static TableDataDefinitionMeta caseInsensitiveValueOf(final String name) {
			for (final TableDataDefinitionMeta item : TableDataDefinitionMeta.values()) {
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
	private boolean userStoppable = false;

	TableImpl() {
		setTableType(CMTableType.CLASS);
		mode = Mode.RESERVED;
		status = SchemaStatus.ACTIVE;
	}

	public TableImpl(final String name, final String comment, final int oid) {
		super();
		this.oid = oid;
		setName(name);
		readDataDefinitionMeta(backend.parseComment(comment));
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof ITable)
			return ((ITable) o).getName().equals(name);
		return false;
	}

	@Override
	public void readDataDefinitionMeta(final Map<String, String> dataDefinitionMeta) {
		for (final Entry<String, String> entry : dataDefinitionMeta.entrySet()) {
			final String name = entry.getKey();
			final String value = entry.getValue();
			TableDataDefinitionMeta ddm = null;
			try {
				ddm = TableDataDefinitionMeta.valueOf(name);
			} catch (final IllegalArgumentException e) {
				Log.PERSISTENCE.warn(String.format("Meta-attribute %s not valid for table %s", name, this.getName()));
			}
			if (ddm != null) {
				ddm.setValue(this, value);
			}
		}
	}

	@Override
	public Map<String, String> genDataDefinitionMeta() {
		final Map<String, String> dataDefinitionMeta = new TreeMap<String, String>();
		for (final TableDataDefinitionMeta meta : TableDataDefinitionMeta.values()) {
			final String value = meta.getValue(this);
			if (value != null) {
				dataDefinitionMeta.put(meta.name(), value);
			}
		}
		return dataDefinitionMeta;
	}

	@Override
	public void save() throws ORMException {
		if (isNew()) {
			oid = backend.createTable(this);
		} else {
			backend.modifyTable(this);
		}
	}

	@Override
	public void delete() {
		backend.deleteTable(this);
		MetadataService.of(this).deleteAllMetadata();
	}

	@Override
	public boolean isNew() {
		return (oid <= 0);
	}

	@Override
	public void setTableType(CMTableType type) {
		if (type != CMTableType.CLASS && type != CMTableType.SIMPLECLASS) {
			type = CMTableType.CLASS;
			Log.PERSISTENCE.warn(String.format("Inconsistent type (%s) for table %s, using %s", type.toMetaValue(),
					getName(), type));
		}
		super.setTableType(type);
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setSuperClass(final boolean isSuperClass) {
		this.isSuperClass = isSuperClass;
	}

	@Override
	public boolean isSuperClass() {
		return isSuperClass;
	}

	@Override
	public ITable getParent() {
		return parent;
	}

	@Override
	public void setParent(final String parent) throws NotFoundException {
		this.parent = UserOperations.from(UserContext.systemContext()).tables().get(parent);
	}

	@Override
	public void setParent(final Integer parent) throws NotFoundException {
		this.parent = UserOperations.from(UserContext.systemContext()).tables().get(parent);
	}

	@Override
	public void setParent(final ITable parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public CardFactory cards() {
		return new CardFactoryImpl(this, UserContext.systemContext());
	}

	@Override
	public TableTree treeBranch() {
		return TableImpl.tree().branch(this.getName());
	}

	@Override
	public ArrayList<ITable> getChildren() {
		final TableTree branch = treeBranch();
		final ArrayList<ITable> children = new ArrayList<ITable>();
		for (final ITable child : branch) {
			children.add(child);
		}
		return children;
	}

	@Override
	public boolean hasChild() {
		return treeBranch().getLeaves().isEmpty();
	}

	/*
	 * TODO Implement a real table tree
	 */
	public static TableTree tree() {
		return UserOperations.from(UserContext.systemContext()).tables().tree();
	}

	public class OrderEntry {
		private final String attributeName;
		private final OrderFilterType orderDirection;

		private OrderEntry(final String attributeName, final OrderFilterType orderDirection) {
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

	@Override
	public List<OrderEntry> getOrdering() {
		final List<OrderEntry> orderingList = new LinkedList<OrderEntry>();
		final List<IAttribute> sortedAttributes = getAttributesSortedByClassOrder();
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
			for (final IAttribute attr : sortedAttributes) {
				final OrderFilterType orderType = attr.getClassOrder() > 0 ? OrderFilterType.ASC : OrderFilterType.DESC;
				final OrderEntry descriptionOrder = new OrderEntry(attr.getName(), orderType);
				orderingList.add(descriptionOrder);
			}
		}
		return orderingList;
	}

	private List<IAttribute> getAttributesSortedByClassOrder() {
		final Collection<IAttribute> attributes = getAttributes().values();
		final List<IAttribute> attributesForOrdering = new LinkedList<IAttribute>();
		for (final IAttribute attr : attributes) {
			if (attr.getClassOrder() != 0)
				attributesForOrdering.add(attr);
		}
		Collections.sort(attributesForOrdering, new ClassOrderComparator());
		return attributesForOrdering;
	}

	private class ClassOrderComparator implements Comparator<IAttribute> {
		@Override
		public int compare(final IAttribute a1, final IAttribute a2) {
			final int classOrder1 = a1.getClassOrder();
			final int classOrder2 = a2.getClassOrder();

			if (classOrder1 > classOrder2)
				return 1;
			else if (classOrder1 < classOrder2)
				return -1;
			else
				return 0;
		}
	}

	@Override
	public boolean isActivity() {
		return UserOperations.from(UserContext.systemContext()).tables().fullTree().branch(ProcessType.BaseTable)
				.contains(this.getName());
	}

	@Override
	public boolean isTheTableClass() {
		return ITable.BaseTable.equals(getName());
	}

	@Override
	public boolean isTheTableActivity() {
		return ProcessType.BaseTable.equals(getName());
	}

	@Override
	public boolean isAllowedOnTrees() {
		return getMode().isCustom() || isActivity();
	}

	@Override
	public Iterable<IAttribute> fkDetails() {
		final List<IAttribute> fkDetails = new ArrayList<IAttribute>();
		for (final ITable simpleClass : UserOperations.from(UserContext.systemContext()).tables()
				.list(CMTableType.SIMPLECLASS)) {
			for (final IAttribute attribute : simpleClass.getAttributes().values()) {
				if (isTargetOfFK(attribute)) {
					fkDetails.add(attribute);
				}
			}
		}
		return fkDetails;
	}

	private boolean isTargetOfFK(final IAttribute attribute) {
		final ITable targetClass = attribute.getFKTargetClass();
		if (targetClass != null) {
			return targetClass.treeBranch().contains(this.getId());
		} else {
			return false;
		}
	}

	@Override
	public boolean isUserStoppable() {
		return userStoppable;
	}

	@Override
	public void setUserStoppable(final boolean userStoppable) {
		this.userStoppable = userStoppable;
	}

	@Override
	public String getPrivilegeId() {
		// Should match the new DAO implementation!
		return String.format("Class:%d", getId());
	}
}
