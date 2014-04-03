package org.cmdbuild.model.widget;

import java.util.Map;

public class NavigationTree extends Widget {

	private String navigationTreeName;
	private String filter;
	private Map<String, Object> preset;

	public String getNavigationTreeName() {
		return navigationTreeName;
	}
	public void setNavigationTreeName(String navigationTreeName) {
		this.navigationTreeName = navigationTreeName;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}
}
