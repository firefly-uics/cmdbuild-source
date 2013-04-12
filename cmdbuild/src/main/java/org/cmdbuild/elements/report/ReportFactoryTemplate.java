package org.cmdbuild.elements.report;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.services.Settings;

public abstract class ReportFactoryTemplate extends ReportFactory {

	private static final String REPORT_DIR_NAME = "WEB-INF/reports";
	private final Map<String, Object> jasperFillManagerParameters = new LinkedHashMap<String, Object>();

	public abstract JasperDesign getJasperDesign();

	@Override
	public JasperPrint fillReport() throws Exception {
		final JasperReport newjr = JasperCompileManager.compileReport(getJasperDesign());
		super.fillReport(newjr, jasperFillManagerParameters);
		return jasperPrint;
	}

	public String getReportDirectory() {
		final Settings settings = Settings.getInstance();
		return settings.getRootPath() + REPORT_DIR_NAME + File.separator;
	}

	protected String getQueryString(final QueryCreator queryCreator) {
		final String query = queryCreator.getQuery();
		final Object[] params = queryCreator.getParams();
		// TODO put the params in the query (substitute the ?)

		return query;
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

		String out = attributeName;
		if (cmAttributeType.equals(AttributeType.REFERENCE) || cmAttributeType.equals(AttributeType.LOOKUP)) {
			out += "_Description";
		}

		return out;
	}

	protected JRDesignTextField createTextFieldForAttribute( //
			final String attributeName, //
			final CMAttributeType<?> attributeType) {

		final JRDesignExpression varExpr = new JRDesignExpression();
		varExpr.setValueClassName(getAttributeClass(attributeType).getName());
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

	protected Class<?> getAttributeClass(final CMAttributeType<?> cmAttributeType) {
		final Class<?> javaClassForAttribute = new CMAttributeTypeVisitor() {

			private Class<?> javaClassForAttribute = null;

			@Override
			public void visit(final BooleanAttributeType attributeType) {
				javaClassForAttribute = Boolean.class;
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
				javaClassForAttribute = Character.class;
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				javaClassForAttribute = Timestamp.class;
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				javaClassForAttribute = Date.class;
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				javaClassForAttribute = Double.class;
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				javaClassForAttribute = Double.class;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				javaClassForAttribute = Integer.class;
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
				javaClassForAttribute = String.class;
			}

			@Override
			public void visit(final StringArrayAttributeType stringArrayAttributeType) {
				javaClassForAttribute = String[].class;
			}

			public Class<?> get() {
				return javaClassForAttribute;
			}

		}.get();

		return javaClassForAttribute;
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
	protected void setFields(final Iterable<? extends CMAttribute> attribute) throws JRException {
		deleteAllFields();
		for (final CMAttribute cmAttribute : attribute) {
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
		field.setValueClassName(getAttributeClass(cmAttribute.getType()).getName());
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
		field.setValueClassName(getAttributeClass(attributeType).getName());
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
