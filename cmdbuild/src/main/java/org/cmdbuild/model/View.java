package org.cmdbuild.model;

import org.cmdbuild.services.store.Store.Storable;

public class View implements Storable {
	public enum ViewType {
		SQL,
		FILTER
	}

	private Long id;
	private String name;
	private String description;
	private String sourceClassName;
	private String sourceFunction;
	private String filter;
	private ViewType type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourceClassName() {
		return sourceClassName;
	}

	public void setSourceClassName(String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}

	public String getSourceFunction() {
		return sourceFunction;
	}

	public void setSourceFunction(String sourceFunction) {
		this.sourceFunction = sourceFunction;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public ViewType getType() {
		return type;
	}

	public void setType(ViewType type) {
		this.type = type;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}
}
