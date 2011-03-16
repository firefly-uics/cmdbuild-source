package org.cmdbuild.portlet.layout;

import java.util.List;

import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.MenuSchema;

public class MenuLayout {

	private String treemenu = "";

	private static final String FOLDER = "folder";
	private static final String SUPERCLASS = "superclass";
	private static final String SUPERPROCESSCLASS = "superprocessclass";

	enum ReportType {

		REPORTPDF("pdf"), REPORTCSV("csv"), REPORTODT("odt"), REPORTXML("xml");

		private final String extension;

		ReportType(final String extension) {
			this.extension = extension;
		}

		String getExtension() {
			return extension;
		}
	}

	public String printTreeMenu(final MenuSchema menuschema) {
		treemenu = "";
		treemenu = printMenu(menuschema);
		return treemenu;
	}

	private String printMenu(final MenuSchema menuschema) {
		final List<MenuSchema> children = menuschema.getChildren();
		if (FOLDER.equals(menuschema.getMenuType()) || SUPERCLASS.equals(menuschema.getMenuType())
				|| SUPERPROCESSCLASS.equals(menuschema.getMenuType())) {
			printFolder(menuschema, children);
		} else {
			if (menuschema.getId() > 0) {
				printNode(menuschema);
			}
			printChildren(children);
		}
		return treemenu;

	}

	private void printNode(final MenuSchema menuschema) {
		final ReportType reportType = getReportType(menuschema);
		if (reportType != null) {
			printReportNode(menuschema, reportType);
		} else {
			printClassNode(menuschema);
		}
	}

	private ReportType getReportType(final MenuSchema menuschema) {
		final String reportType = menuschema.getMenuType().toUpperCase();
		try {
			return ReportType.valueOf(reportType);
		} catch (final Exception e) {
			return null;
		}
	}

	private void printFolder(final MenuSchema menuschema, final List<MenuSchema> children) {
		final FieldUtils utils = new FieldUtils();
		final String isdefault = addIsDefaultClass(menuschema.isDefaultToDisplay());
		treemenu = treemenu
				+ String.format("<li><span class=\"folder %s\">%s</span>", isdefault, utils.checkString(menuschema
						.getDescription()));
		if (children != null) {
			treemenu = treemenu + "<ul>";
			printChildren(children);
			treemenu = treemenu + "</ul>";
		}
		treemenu = treemenu + "</li>";
	}

	private void printChildren(final List<MenuSchema> children) {
		if (children != null) {
			for (final MenuSchema child : children) {
				final MenuSchema item = child;
				if (item != null) {
					printMenu(item);
				}
			}
		}
	}

	private String addIsDefaultClass(final Boolean isDefault) {
		String defaultClass = "";
		if (isDefault != null && isDefault) {
			defaultClass = " isDefaultToDisplay";
		}
		return defaultClass;
	}

	private void printReportNode(final MenuSchema menuschema, final ReportType reportType) {
		final FieldUtils utils = new FieldUtils();
		final String isdefault = addIsDefaultClass(menuschema.isDefaultToDisplay());
		treemenu = treemenu
				+ String
						.format(
								"<li><span class=\"%s %s\"><a onclick=\"return CMDBuildGetReportParameters(\'%s\', \'%s\', \'%s\')\">%s</a></span></li>",
								reportType.getExtension(), isdefault, menuschema.getId(), menuschema.getDescription(),
								reportType.getExtension(), utils.checkString(menuschema.getDescription()));
	}

	private void printClassNode(final MenuSchema menuschema) {
		final FieldUtils utils = new FieldUtils();
		final String isdefault = addIsDefaultClass(menuschema.isDefaultToDisplay());
		treemenu = treemenu
				+ String
						.format(
								"<li><span class=\"file %s\"><a onclick=\"return CMDBuildSetSessionVariables(\'%s\', \'%s\', \'%s\', \'%s\')\">%s</a></span></li>",
								isdefault, utils.checkString(menuschema.getClassname()), menuschema.getMenuType(),
								utils.checkString(menuschema.getDescription()), menuschema.getPrivilege(), utils
										.checkString(menuschema.getDescription()));
	}

}
