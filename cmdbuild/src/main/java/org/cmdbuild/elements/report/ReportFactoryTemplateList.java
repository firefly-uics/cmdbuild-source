package org.cmdbuild.elements.report;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.backend.postgresql.CardQueryBuilder;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
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
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataViewCardFetcher.QuerySpecsBuilderBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportFactoryTemplateList extends ReportFactoryTemplate {

	private final List<String> attributeOrder;
	private JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final CMClass table;
	private final static String REPORT_PDF = "CMDBuild_list.jrxml";
	private final static String REPORT_CSV = "CMDBuild_list_csv.jrxml";

	@Autowired
	private final CMBackend backend = CMBackend.INSTANCE;

	public ReportFactoryTemplateList( //
			final ReportExtension reportExtension, //
			final QueryOptions queryOptions, //
			final List<String> attributeOrder, //
			final String className) throws JRException { //

		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final CMDataView dataView = TemporaryObjectsBeforeSpringDI.getSystemView();
		this.reportExtension = reportExtension;
		this.attributeOrder = attributeOrder;

		table = dataAccessLogic.findClass(className);
		final QuerySpecsBuilder queryBuilder = new QuerySpecsBuilderBuilder() //
				.withDataView(dataView) //
				.withClass(table) //
				.withQueryOptions(queryOptions) //
				.build();
		final QueryCreator queryCreator = new QueryCreator(queryBuilder.build());
		final String query = getQueryString(queryCreator);

		loadDesign(reportExtension);
		initDesign(queryCreator);
	}

	private void loadDesign(final ReportExtension reportExtension) throws JRException {
		if (reportExtension == ReportExtension.PDF) {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_PDF);
		} else {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_CSV);
		}
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	private void initDesign(final QueryCreator queryCreator) throws JRException {
		setNameFromTable();
		setQuery(queryCreator);
		setAllTableFields();
		setTextFieldsInDetailBand();
		setColumnHeadersForNewFields();

		if (reportExtension == ReportExtension.PDF) {
			setTitleFromTable();
			updateImagesPath();
		}

		refreshLayout();
	}

	private void setNameFromTable() {
		jasperDesign.setName(table.getIdentifier().getLocalName());
	}

	protected void setQuery(final QueryCreator queryCreator) {
		final String queryString = getQueryString(queryCreator);
		setQuery(queryString);
	}

	@Deprecated
	protected void setQuery(final CardQuery reportQuery) {
		final CardQueryBuilder qb = new CardQueryBuilder();
		final String query = backend.cardQueryToSQL(reportQuery, qb);
		setQuery(query);
	}

	private void setTitleFromTable() {
		setTitle(table.getIdentifier().getLocalName());
	}

	private void setAllTableFields() throws JRException {
		final List<CMAttribute> fields = new LinkedList<CMAttribute>();
		for (final String attribute : attributeOrder) {
			fields.add(table.getAttribute(attribute));
		}

		setFields(fields);
	}

	@SuppressWarnings("unchecked")
	private void setTextFieldsInDetailBand() {
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand band = section.getBands()[0];
		final List<Object> graphicVector = new ArrayList<Object>();

		for (final Object obj : band.getChildren()) {
			if (!(obj instanceof JRDesignTextField)) {
				graphicVector.add(obj);
			}
		}

		final List<Object> detailVector = new ArrayList<Object>();
		for (final String attributeName : attributeOrder) {
			final CMAttribute attribute = table.getAttribute(attributeName);
			detailVector.add(createTextFieldForAttribute(
					table.getIdentifier().getLocalName() + "_" + attribute.getName(), attribute.getType()));
		}

		band.getChildren().clear();
		band.getChildren().addAll(graphicVector);
		band.getChildren().addAll(detailVector);
	}

	@SuppressWarnings("unchecked")
	private void setColumnHeadersForNewFields() {
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		final JRElement[] elements = columnHeader.getElements();
		final Vector<JRElement> designHeaders = new Vector<JRElement>();
		final Vector<JRElement> designElements = new Vector<JRElement>();

		// backup existing design elements
		for (int i = 0; i < elements.length; i++) {
			if (!(elements[i] instanceof JRDesignStaticText)) {
				designElements.add(elements[i]);
			}
		}

		// create column headers
		for (final String attribute : attributeOrder) {
			final CMAttribute cmAttribute = table.getAttribute(attribute);
			final JRDesignStaticText dst = new JRDesignStaticText();
			dst.setText(cmAttribute.getDescription());
			designHeaders.add(dst);
		}

		// save new list of items
		columnHeader.getChildren().clear();
		columnHeader.getChildren().addAll(designElements);
		columnHeader.getChildren().addAll(designHeaders);
	}

	/*
	 * Update position of report elements
	 */
	private void refreshLayout() {
		// calculate weight of all elements
		final Map<String, String> weight = new HashMap<String, String>();
		CMAttribute attribute = null;
		int virtualWidth = 0;
		int size = 0;
		final int height = 17;
		String key = "";
		for (final String attributeName : attributeOrder) {
			attribute = table.getAttribute(attributeName);
			size = getSizeFromAttribute(attribute);
			virtualWidth += size;
			key = getAttributeName(table.getIdentifier().getLocalName() + "_" + attribute.getName(),
					attribute.getType());
			weight.put(attribute.getName(), Integer.toString(size));
			weight.put(key, Integer.toString(size));
			weight.put(attribute.getDescription(), Integer.toString(size));
		}
		final int pageWidth = jasperDesign.getPageWidth();
		final double cx = (pageWidth * 0.95) / virtualWidth;
		Log.REPORT.debug("cx=" + cx + " pageWidth " + (pageWidth * 0.95) + " / virtualWidth " + virtualWidth);
		double doub = 0;
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand detail = section.getBands()[0];
		JRElement[] elements = detail.getElements();
		JRDesignTextField dtf = null;
		int x = 0;
		final int y = 2;
		Log.REPORT.debug("RF updateDesign DESIGN");
		JRDesignExpression varExpr = null;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignTextField) {
				dtf = (JRDesignTextField) elements[i];
				varExpr = (JRDesignExpression) dtf.getExpression();
				key = varExpr.getText();
				Log.REPORT.debug("text=" + key);
				key = key.substring(3, key.length() - 1);
				Log.REPORT.debug("text=" + key);
				key = weight.get(key);
				Log.REPORT.debug("kry=" + key);
				try {
					size = Integer.parseInt(key);
				} catch (final NumberFormatException e) {
					size = 0;
				}
				doub = size * cx;
				size = (int) doub;
				dtf.setX(x);
				dtf.setY(y);
				dtf.setWidth(size);
				dtf.setHeight(height);
				dtf.setBlankWhenNull(true);
				dtf.setStretchWithOverflow(true);
				Log.REPORT.debug("RF updateDesign x=" + dtf.getX() + " Width=" + dtf.getWidth());
				x += size;
			}
		}

		// sizing table headers
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		elements = columnHeader.getElements();
		JRDesignStaticText dst = null;
		x = 0;
		Log.REPORT.debug("RF updateDesign HEADER");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignStaticText) {
				dst = (JRDesignStaticText) elements[i];
				key = dst.getText();
				Log.REPORT.debug("text=" + key);
				key = weight.get(key);
				Log.REPORT.debug("key=" + key);
				size = Integer.parseInt(key);

				doub = size * cx;
				size = (int) doub;
				dst.setForecolor(Color.WHITE);
				dst.setX(x);
				dst.setHeight(height);
				dst.setWidth(size);
				Log.REPORT.debug("RF updateDesign" + dst.getText() + " x=" + dst.getX() + " Width=" + dst.getWidth());
				x += size;
			}
		}
	}

	protected int getSizeFromAttribute(final CMAttribute attribute) {
		return new CMAttributeTypeVisitor() {

			private int size = 0;

			@Override
			public void visit(final BooleanAttributeType attributeType) {
				size = 4;
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
				size = 4;
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				size = 16;
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				size = 10;
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				size = 8;
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				size = 8;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				size = 8;
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				final Integer l = attributeType.length;
				size = (l > 4 ? l : 4) > 40 ? 40 : (l > 4 ? l : 4);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				size = 50;
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
				size = 20;
			}

			@Override
			public void visit(final StringArrayAttributeType stringArrayAttributeType) {
				size = 20;
			}

			public int get() {
				return size;
			}

		}.get();
	}

}
