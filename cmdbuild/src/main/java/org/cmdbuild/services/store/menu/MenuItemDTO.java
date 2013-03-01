package org.cmdbuild.services.store.menu;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;

public class MenuItemDTO implements MenuItem {

	private int index;
	private Long id;
	private Integer referencedElementId = 0; // default value
	private Integer parentId;
	private MenuItemType type;
	private String description, referencedClassName, groupName;
	private final List<MenuItem> children;

	public MenuItemDTO() {
		super();
		children = new LinkedList<MenuItem>();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public MenuItemType getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Integer getParentId() {
		return parentId;
	}

	@Override
	public String getReferedClassName() {
		return referencedClassName;
	}

	@Override
	public Integer getReferencedElementId() {
		return referencedElementId;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public List<MenuItem> getChildren() {
		return children;
	}

	@Override
	public void setIndex(final int index) {
		this.index = index;
	}

	@Override
	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public void setParentId(final Integer parentId) {
		this.parentId = parentId;
	}

	@Override
	public void setReferencedElementId(final Integer referencedElementId) {
		this.referencedElementId = referencedElementId;
	}

	@Override
	public void setType(final MenuItemType type) {
		this.type = type;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public void setGroupName(final String groupName) {
		this.groupName = groupName;
	}

	@Override
	public void setReferedClassName(final String referencedClassName) {
		this.referencedClassName = referencedClassName;
	}

	@Override
	public void addChild(final MenuItem child) {
		this.children.add(child);
	}
}