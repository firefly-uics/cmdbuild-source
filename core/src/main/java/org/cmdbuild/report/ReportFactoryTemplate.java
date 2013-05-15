package org.cmdbuild.report;

import java.awt.Color;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.FilesStore;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

public abstract class ReportFactoryTemplate extends ReportFactory {

	private static final String REPORT_DIR_NAME = "reports";
	private final Map<String, Object> jasperFillManagerParameters = new LinkedHashMap<String, Object>();

	public abstract JasperDesign getJasperDesign();

	public ReportFactoryTemplate(final DataSource dataSource, final CmdbuildConfiguration configuration) {
		super(dataSource, configuration);
	}

	@Override
	public JasperPrint fillReport() throws Exception {
		final JasperReport newjr = JasperCompileManager.compileReport(getJasperDesign());
		super.fillReport(newjr, jasperFillManagerParameters);
		return jasperPrint;
	}

	public String getReportDirectory() {
		// FIXME
		final FilesStore filesStore = applicationContext().getBean("rootFilesStore", FilesStore.class);
		return filesStore.getAbsoluteRootDirectory() + File.separator + REPORT_DIR_NAME + File.separator;
	}

	protected String getQueryString(final QueryCreator queryCreator) {
		// Add some space to the end of the query to avoid
		// the situation in which the ? is the last character
		final String query = String.format("%s     ", queryCreator.getQuery());
		final Object[] params = queryCreator.getParams();

		final String[] queryParts = query.split("\\?");
		final StringBuilder queryStringBuilder = new StringBuilder();

		for (int i = 0, l = queryParts.length - 1; i < l; ++i) {
			queryStringBuilder.append(queryParts[i]);
			queryStringBuilder.append(String.format("'%s'", params[i]));
		}

		queryStringBuilder.append(queryParts[queryParts.length - 1]);

		final String compiledQuery = queryStringBuilder.toString();
		Log.REPORT.debug(String.format("Report Query: %s", compiledQuery));
		return compiledQuery;
	}

	protected void setQuery(final String reportQuery) {
		final JRDesignQuery designQuery = new JRDesignQuery();
		designQuery.setText(reportQuery);
		getJasperDesign().setQuery(designQuery);
	}

	// add the suffix "_Description" for type Reference and Lookup
	protected String getAttributeName( //
			final String attributeName, //
			final CMAttributeType<?> cmAttributeType) {

		final String out = attributeName;
		// if (cmAttributeType instanceof ReferenceAttributeType
		// || cmAttributeType instanceof LookupAttributeType) {
		//
		// out += "_Description";
		// }

		return out;
	}

	protected JRDesignTextField createTextFieldForAttribute( //
			final String attributeName, //
			final CMAttributeType<?> attributeType) {

		final JRDesignExpression varExpr = new JRDesignExpression();
		varExpr.setValueClassName(getAttributeJavaClass(attributeType).getName());
		varExpr.setText("$F{" + getAttributeName(attributeName, attributeType) + "}");
		final JRDesignTextField field = new JRDesignTextField();
		field.setExpression(varExpr);
		field.setBlankWhenNull(true);
		field.setStretchWithOverflow(true);
		field.setForecolor(Color.BLACK);
		field.setBackcolor(Color.GRAY);
		field.setPositionType(JRElement.POSITION_TYPE_FLOAT);
		field.setX(0);
		field.setY(0);
		return field;
	}

	protected JRDesignStaticText createStaticTextForAttribute(final CMAttribute cmAttribute) {
		final JRDesignStaticText dst = new JRDesignStaticText();
		final String labelText;

		if (cmAttribute.getDescription() != null && !cmAttribute.getDescription().equals("")) {
			labelText = cmAttribute.getDescription();
		} else {
			labelText = cmAttribute.getName();
		}

		dst.setPositionType(JRElement.POSITION_TYPE_FLOAT);
		dst.setText(labelText);
		dst.setHeight(20);
		dst.setWidth(100);

		return dst;
	}

	protected JRDesignStaticText createStaticText(final String text) {
		final JRDesignStaticText dst = new JRDesignStaticText();

		dst.setText(text);
		dst.setPositionType(JRElement.POSITION_TYPE_FLOAT);
		dst.setHeight(20);
		dst.setWidth(100);

		return dst;
	}

	protected Class<?> getAttributeJavaClass(final CMAttributeType<?> cmAttributeType) {
		return new ReportAttributeTypeClassMapperVisitor().get(cmAttributeType);
	}

	/**
	 * Set report title (custom string)
	 */
	@SuppressWarnings("unchecked")
	protected void setTitle(final String title) {
		Object obj = null;
		JRDesignStaticText field = null;
		final JRBand titleBand = getJasperDesign().getTitle();
		final List<Object> f = titleBand.getChildren();
		final Iterator<Object> it = f.iterator();

		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof JRDesignStaticText) {
				String reportTitle = "";
				if (title != null && !title.equals("")) {
					reportTitle = title;
				}
				field = (JRDesignStaticText) obj;
				field.setText(reportTitle);
			}
		}
	}

	/**
	 * Add string parameter to design
	 * 
	 * @param parametersMap
	 * @throws JRException
	 */
	protected void addDesignParameter(final String name, final String defaultvalue) throws JRException {
		final JRDesignParameter jrParam = new JRDesignParameter();
		jrParam.setName(name);
		jrParam.setForPrompting(false);
		jrParam.setValueClass(String.class);
		final JRDesignExpression exp = new JRDesignExpression();
		exp.setText("\"" + defaultvalue + "\"");
		exp.setValueClass(String.class);
		jrParam.setDefaultValueExpression(exp);
		getJasperDesign().addParameter(jrParam);
	}

	protected void addFillParameter(final String key, final Object value) throws JRException {
		jasperFillManagerParameters.put(key, value);
	}

	/**
	 * Update images path only in title band; images are supposed to be in the
	 * same folder of master report
	 */
	@SuppressWarnings("unchecked")
	protected void updateImagesPath() {
		Object obj = null;
		final JRBand title = getJasperDesign().getTitle();
		final List<Object> f = title.getChildren();
		final Iterator<Object> it = f.iterator();

		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof JRDesignImage) {
				final JRDesignImage img = (JRDesignImage) obj;
				final JRDesignExpression varExp = (JRDesignExpression) img.getExpression();
				String path = "\"" + getReportDirectory()
						+ varExp.getText().substring(1, varExp.getText().length() - 1) + "\"";
				path = escapeWinSeparators(path);
				varExp.setText(path);
			}
		}
	}

	/**
	 * Update subreports path (in every JRBand); subreports are supposed to be
	 * in the same folder of master report
	 */
	@SuppressWarnings("unchecked")
	protected void updateSubreportsPath() {
		final List<JRBand> bands = getBands(getJasperDesign());

		for (final JRBand band : bands) {
			if (band != null) {
				final List<Object> f = band.getChildren();
				final Iterator<Object> it = f.iterator();

				Object obj = null;
				while (it.hasNext()) {
					obj = it.next();
					if (obj instanceof JRDesignSubreport) {
						final JRDesignSubreport subreport = (JRDesignSubreport) obj;
						final JRDesignExpression varExp = (JRDesignExpression) subreport.getExpression();
						String path = "\"" + getReportDirectory()
								+ varExp.getText().substring(1, varExp.getText().length() - 1) + "\"";
						path = escapeWinSeparators(path);
						varExp.setText(path);
					}
				}
			}
		}
	}

	/**
	 * Create report fields
	 */
	protected void setFields(final Iterable<? extends CMAttribute> attributes) throws JRException {
		deleteAllFields();
		for (final CMAttribute cmAttribute : attributes) {
			getJasperDesign().addField(createDesignField(cmAttribute));
		}
	}

	protected void addField( //
			final String name, //
			final String description, //
			final CMAttributeType<?> attributeType) throws JRException {

		getJasperDesign().addField(createDesignField(name, description, attributeType));
	}

	/**
	 * Remove all existing fields
	 */
	protected void deleteAllFields() {
		final JRField[] list = getJasperDesign().getFields();
		for (final JRField field : list) {
			getJasperDesign().removeField(field);
		}
	}

	/**
	 * Create report field for attribute
	 */
	private JRDesignField createDesignField(final CMAttribute cmAttribute) {
		final JRDesignField field = new JRDesignField();
		final String fieldName = fieldNameFromCMAttribute(cmAttribute);

		field.setName(fieldName);
		field.setDescription(cmAttribute.getDescription());
		// The className of the attribute
		field.setValueClassName(getAttributeJavaClass(cmAttribute.getType()).getName());
		return field;
	}

	protected String fieldNameFromCMAttribute(final CMAttribute cmAttribute) {
		final CMEntryType attributeOwner = cmAttribute.getOwner();
		final String fieldName = getAttributeName(
				attributeOwner.getIdentifier().getLocalName() + "_" + cmAttribute.getName(), cmAttribute.getType());
		return fieldName;
	}

	private JRDesignField createDesignField( //
			final String name, final String description, final CMAttributeType<?> attributeType) {

		final JRDesignField field = new JRDesignField();
		field.setName(name);
		field.setDescription(description);
		field.setValueClassName(getAttributeJavaClass(attributeType).getName());
		return field;
	}

	private String escapeWinSeparators(String path) {
		final StringBuffer newpath = new StringBuffer();
		final char sep = '\\';
		if (File.separator.toCharArray()[0] == sep) {
			final char[] ca = path.toCharArray();
			char ct;
			for (int i = 0; i < ca.length; i++) {
				ct = ca[i];
				if (ct != sep) {
					newpath.append(ct);
				} else {
					newpath.append(sep);
					newpath.append(ct);
				}
			}
			path = newpath.toString();
		}
		return path;
	}

}
