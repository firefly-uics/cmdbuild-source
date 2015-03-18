package org.cmdbuild.model;

import org.cmdbuild.services.localization.LocalizableStorableVisitor;

public class View implements _View {


	private Long id;
	private String name;
	private String description;
	private String sourceClassName;
	private String sourceFunction;
	private String filter;
	private ViewType type;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getSourceClassName() {
		return sourceClassName;
	}

	@Override
	public void setSourceClassName(final String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}

	@Override
	public String getSourceFunction() {
		return sourceFunction;
	}

	@Override
	public void setSourceFunction(final String sourceFunction) {
		this.sourceFunction = sourceFunction;
	}

	@Override
	public String getFilter() {
		return filter;
	}

	@Override
	public void setFilter(final String filter) {
		this.filter = filter;
	}

	@Override
	public ViewType getType() {
		return type;
	}

	@Override
	public void setType(final ViewType type) {
		this.type = type;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	@Override
	public String getPrivilegeId() {
		return String.format("View:%d", getId());
	}

	@Override
	public void accept(LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}
}
