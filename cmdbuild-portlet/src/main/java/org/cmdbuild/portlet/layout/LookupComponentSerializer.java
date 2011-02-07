package org.cmdbuild.portlet.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.cmdbuild.portlet.operation.LookupOperation;
import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Lookup;

public class LookupComponentSerializer implements HTMLSerializer {

	private static final String FLOWSTATUS_LABEL = "Stato processo";
	private static final String FLOWSTATUS_TYPE = "FlowStatus";
	private static final String PROCESS_TERMINATED = "closed.terminated";
	private static final String PROCESS_ABORTED = "closed.aborted";
	private static final String LOOKUP_IMAGE_PREAMBLE = "image:";
	private List<Integer> genealogy;
	private final ComponentLayout layout;
	private String javascript;

	public LookupComponentSerializer(final ComponentLayout layout) {
		this.layout = layout;
	}

	public String serializeHtml(final String javascript) {
		this.javascript = javascript;
		return serializeHtml();
	}

	public String serializeHtml() {
		final LookupOperation operation = new LookupOperation(layout.getClient());
		final List<Lookup> lookups = operation.getLookupList(layout.getSchema().getLookupType());
		String lookup = "";
		if (lookups != null) {
			final LinkedList<HashMap<Integer, Lookup>> lookuplevels = buildLookupLevels(lookups);
			addLookupToList(lookups, lookuplevels);
			lookup = printLookupLevels(layout, lookuplevels);
		}
		return lookup;
	}

	public String addFlowStatusLookup(final String defaultdisplay, final String classname) {
		final LookupOperation operation = new LookupOperation(layout.getClient());
		final FieldUtils utils = new FieldUtils();
		final List<Lookup> lookups = generateProcessLookup(operation.getLookupList(FLOWSTATUS_TYPE));
		final StringBuilder result = new StringBuilder();
		result.append("<li id=\"CMDBuildFlowstatus\" style=\"border: none ; float: right; background: #ffffff;\">\n"
				+ "<div id=\"CMDBuildProcessstatus\">\n");
		result.append(FLOWSTATUS_LABEL).append(": <select name=\"").append(FLOWSTATUS_TYPE).append(
				"\" onchange=\"CMDBuildSetFlowstatus(\'").append(utils.checkString(classname)).append(
				"\',this.value)\" >\n");
		if (lookups != null) {
			for (final Lookup lookup : lookups) {
				if (!defaultdisplay.equals(lookup.getCode())) {
					result.append("<option value=\"").append(lookup.getCode()).append("\">").append(
							utils.checkString(lookup.getDescription())).append("</option>\n");
				} else {
					result.append("<option value=\"").append(lookup.getCode()).append("\" selected=\"selected\">")
							.append(utils.checkString(lookup.getDescription())).append("</option>\n");
				}
			}
		} else {
			result.append("<option value=''>  </option>");
		}
		result.append("</select>");
		result.append("</div>\n</li>\n");
		return result.toString();
	}

	private List<Lookup> generateProcessLookup(final List<Lookup> lookupList) {
		final List<Lookup> processLookupList = new LinkedList<Lookup>();
		for (final Lookup lookup : lookupList) {
			if (PROCESS_TERMINATED.equals(lookup.getCode()) || PROCESS_ABORTED.equals(lookup.getCode())) {
				continue;
			} else {
				processLookupList.add(lookup);
			}
		}
		final Lookup allProcesses = new Lookup();
		allProcesses.setDescription("Tutti");
		allProcesses.setType(lookupList.get(0).getType());
		allProcesses.setId(-1);
		allProcesses.setCode("all");
		processLookupList.add(allProcesses);
		return processLookupList;
	}

	private LinkedList<HashMap<Integer, Lookup>> buildLookupLevels(final List<Lookup> lookups) {
		final int levels = countLookupLevels(lookups.get(0));
		final LinkedList<HashMap<Integer, Lookup>> lookuplevels = new LinkedList<HashMap<Integer, Lookup>>();
		for (int i = 0; i < levels; i++) {
			lookuplevels.add(new HashMap<Integer, Lookup>());
		}
		return lookuplevels;
	}

	private int countLookupLevels(Lookup lookup) {
		int levels = 1;
		while (true) {
			if (lookup.getParent() == null) {
				return levels;
			}
			lookup = lookup.getParent();
			++levels;
		}
	}

	private void addLookupToList(final List<Lookup> lookups, final LinkedList<HashMap<Integer, Lookup>> lookuplevels) {
		for (int i = 0; i < lookups.size(); i++) {
			final Iterator<HashMap<Integer, Lookup>> iterator = lookuplevels.iterator();
			for (Lookup current = lookups.get(i); current != null; current = current.getParent()) {
				iterator.next().put(current.getId(), current);
			}
		}
	}

	private String printLookupLevels(final ComponentLayout component,
			final LinkedList<HashMap<Integer, Lookup>> lookuplevels) {
		final StringBuilder lookupLevelsPrint = new StringBuilder();
		final AttributeSchema schema = component.getSchema();
		String label = schema.getDescription();
		String id = "cmdbcombo-" + schema.getIdClass();
		String name;

		Lookup selectedLookup = null;
		if (!"".equals(component.getValue())) {
			final LookupOperation operation = new LookupOperation(layout.getClient());
			selectedLookup = operation.getLookup(Integer.valueOf(component.getId()));
		}
		genealogy = new LinkedList<Integer>();
		getLookupGenealogy(selectedLookup);
		for (int i = lookuplevels.size(); --i >= 0;) {
			if (i == 0) {
				name = schema.getName();
			} else {
				name = null;
			}

			final Collection<Lookup> lookups = lookuplevels.get(i).values();
			lookupLevelsPrint.append(printLookup(component, label, name, id, lookups, selectedLookup, lookuplevels
					.size() - 1 == i));
			label = "";
			id += "_";
		}
		return lookupLevelsPrint.toString();
	}

	private String printLookup(final ComponentLayout component, final String label, final String name, final String id,
			final Collection<Lookup> lookups, final Lookup selectedLookup, final boolean isFirst) {

		final AttributeSchema schema = component.getSchema();
		final String fieldmode = schema.getFieldmode();
		final FieldUtils utils = new FieldUtils();
		final String htmlLabel = label.length() == 0 ? "&nbsp;" : utils.checkString(utils.setMandatoryField(schema));
		final boolean isLookupDisabled = PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible();
		final List<Lookup> lookupList = new ArrayList<Lookup>(lookups);

		final StringBuilder result = new StringBuilder();
		result.append("<div class=\"CMDBuildRow\"><label class=\"CMDBuildCol1\">").append(htmlLabel).append(
				"</label>\n").append("<span class=\"CMDBuilcol2\">");
		if (isFirst) {
			final String code = lookups.iterator().next().getCode();
			if (code != null && code.startsWith(LOOKUP_IMAGE_PREAMBLE)) {
				Collections.sort(lookupList, new Comparator<Lookup>() {

					public int compare(final Lookup l1, final Lookup l2) {
						return l1.getPosition() - l2.getPosition();
					}
				});
				final String lookupType = lookups.iterator().next().getType();
				result.append(String.format("<input type=\"hidden\" class=\"CMDBuild%s\" id=\"%s\" name=\"%s\"/>",
						lookupType, lookupType, lookupType));
				for (final Lookup lookup : lookupList) {
					result.append(printLookupWithImage(lookup, javascript));
				}
			} else {
				final String fullSelect = genSelectElement(schema, name, id, isLookupDisabled,
						genOptionElements(lookups));
				result.append(fullSelect);
			}
		} else {
			final String fullSelect = genSelectElement(null, name, "full" + id, isLookupDisabled,
					genOptionElements(lookups));
			String emptySelect = "";
			if (!("".equals(component.getValue()))) {
				final Collection<Lookup> filteredLookup = generateFilteredLookups(lookupList, selectedLookup
						.getParentId());
				emptySelect = genSelectElement(schema, name, id, isLookupDisabled, genOptionElements(filteredLookup));
			} else {
				emptySelect = genSelectElement(schema, name, id, isLookupDisabled, genOptionElements(null));
			}
			result.append("<div id=\"CMDBuildHiddenCombo_").append(utils.checkString(name)).append(
					"\" class=\"CMDBuildHiddenCombo\" style=\"display: none\">").append(fullSelect).append("</div>")
					.append(emptySelect);
		}
		result.append("</span>").append("</div>");
		return result.toString();
	}

	public String printLookupWithImage(final Lookup lookup, final String javascript) {
		final StringBuilder html = new StringBuilder();
		final String imageName = lookup.getCode().split("image:")[1];
		html.append(String.format("<img class=\"CMDBuildImageLookup\" title=\"%s\" alt=\"%s\" "
				+ "src=\"%s/upload/images/%s\" onclick=\"CMDBuildSelectImage(this, '%s', '%s')\" />", lookup
				.getDescription(), lookup.getDescription(), layout.getContextPath(), imageName, lookup.getType(),
				lookup.getId()));
		return html.toString();
	}

	private String genOptionElements(final Collection<Lookup> lookups) {
		final FieldUtils utils = new FieldUtils();
		final StringBuilder options = new StringBuilder("<option value=\"\">  </option>");
		if (lookups != null) {
			final Collection<Lookup> sortedLookups = buildSortedLookups(lookups);
			for (final Lookup l : sortedLookups) {
				final boolean isCurrentOptionSelected = genealogy.contains(l.getId());
				final String option = String.format("<option value=\"%d\" class=\"%d\" %s>%s</option>\n", l.getId(), l
						.getParentId(), isCurrentOptionSelected ? " selected=\"selected\"" : "", utils.checkString(l
						.getDescription()));
				options.append(option);
			}
		}

		return options.toString();
	}

	private void getLookupGenealogy(final Lookup selectedLookup) {
		if (selectedLookup != null && selectedLookup.getParentId() >= 0) {
			genealogy.add(selectedLookup.getId());
			getLookupGenealogy(selectedLookup.getParent());
		}
	}

	private String genSelectElement(final AttributeSchema schema, final String name, final String id,
			final boolean disabled, final String options) {
		final FieldUtils utils = new FieldUtils();
		String required = "";
		if (utils.isRequired(schema)) {
			required = " required ";
		}

		final StringBuilder select = new StringBuilder("<select id=\"").append(id).append("\"");
		if (name != null) {
			select.append("name=\"").append(utils.checkString(name)).append("\"");
		} else {
			select.append("onchange=\"CMDBuildComboFilter(this.id, this.value)\"");
		}
		if (disabled) {
			select.append(" disabled=\"disabled\"");
		}
		String schemaname = "";
		if (schema != null) {
			schemaname = schema.getName();
		}
		select.append(String.format(" class=\"CMDBuildMultilevel %s %s %s \" >%s</select>", required, schemaname, id,
				options));
		final String resetFieldScript = String.format("onclick=\"CMDBuildResetField('%s')\"", id);
		select.append("<button class=\"CMDBuildResetCombo\" type=\"button\" ").append(resetFieldScript);
		if (disabled) {
			select.append(" disabled=\"disabled\"");
		}
		select.append(">x</button>");
		return select.toString();
	}

	private Collection<Lookup> buildSortedLookups(final Collection<Lookup> lookups) {
		final TreeMap<Integer, Lookup> lookupTreeMap = new TreeMap<Integer, Lookup>();
		for (final Lookup l : lookups) {
			lookupTreeMap.put(l.getPosition(), l);
		}
		return lookupTreeMap.values();
	}

	private Collection<Lookup> generateFilteredLookups(final List<Lookup> lookups, final int parentId) {
		final Collection<Lookup> filteredLookups = new LinkedList<Lookup>();
		for (final Lookup lookup : lookups) {
			if (lookup.getParentId() == parentId) {
				filteredLookups.add(lookup);
			} else {
				continue;
			}
		}
		return filteredLookups;
	}
}
