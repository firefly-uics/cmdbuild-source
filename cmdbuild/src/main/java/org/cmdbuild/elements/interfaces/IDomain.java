package org.cmdbuild.elements.interfaces;

import org.cmdbuild.exception.ORMException;


public interface IDomain extends BaseSchema {

	public static final String BaseTable = "";
	public static final String DomainTablePrefix = "Map_";

	public static String CARDINALITY_11 = "1:1";
	public static String CARDINALITY_N1 = "N:1";
	public static String CARDINALITY_1N = "1:N";
	public static String CARDINALITY_NN = "N:N";

	public void save();
	public void delete();

	public String getDBName();
	public String getDescriptionDirect();
	public void setDescriptionDirect(String descriptionDirect);
	public String getDescriptionInverse();
	public void setDescriptionInverse(String descriptionInverse);
	public ITable[] getTables();
	public ITable getClass1();
	public void setClass1(ITable table);
	public ITable getClass2();
	public void setClass2(ITable table);
	public int getId();
	public String getCardinality();
	public void setCardinality(String cardinality);
	public String getDescription();
	public void setDescription(String description);
	public boolean isMasterDetail();
	public void setMasterDetail(boolean isMasterDetail);
	public String getMDLabel();
	public void setMDLabel(String mdLabel);
	public void setOpenedRows(int openedRows);
	public int getOpenedRows();
	public String getType();
	public boolean isLocal(ITable table);
	public boolean isNew();
	public boolean getDirectionFrom(ITable sourceTable)  throws ORMException;
}
