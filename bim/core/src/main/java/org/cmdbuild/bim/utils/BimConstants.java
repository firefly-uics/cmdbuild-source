package org.cmdbuild.bim.utils;

public class BimConstants {
	
	// IFC constants
	public static final String IFC_GLOBALID = "GlobalId";
	public static final String IFC_DIRECTION = "IfcDirection";
	public static final String IFC_PLACEMENT_REL_TO = "PlacementRelTo";
	public static final String IFC_RELATIVE_PLACEMENT = "RelativePlacement";
	public static final String IFC_OBJECT_PLACEMENT = "ObjectPlacement";
	public static final String IFC_COORDINATES = "Coordinates";
	public static final String IFC_CARTESIAN_POINT = "IfcCartesianPoint";
	public static final String IFC_AXIS = "Axis";
	public static final String IFC_REF_DIRECTION = "RefDirection";
	public static final String IFC_AXIS2_PLACEMENT3D = "IfcAxis2Placement3D";
	public static final String IFC_AXIS2_PLACEMENT2D = "IfcAxis2Placement2D";
	public static final String IFC_DIRECTION_RATIOS = "DirectionRatios";
	public static final String IFC_REL_CONTAINED = "IfcRelContainedInSpatialStructure";
	public static final String IFC_RELATING_STRUCTURE = "RelatingStructure";
	public static final String IFC_RELATED_ELEMENTS = "RelatedElements";
	
	//Attributes and tables
	public static final String GLOBALID = IFC_GLOBALID;
	public static final String FK_COLUMN_NAME = "Master";
	public static final String BIM_SCHEMA_NAME = "bim";
	public static final String GEOMETRY_ATTRIBUTE = "Geometry";
	
	public static final String X_ATTRIBUTE_NAME = "x1";
	public static final String Y_ATTRIBUTE_NAME = "x2";
	public static final String Z_ATTRIBUTE_NAME = "x3";
	
	//JDBC Queries for BIM data
	public static final String STORE_COORDINATES_QUERY_TEMPLATE = "UPDATE %s.\"%s\"" + " SET \"%s\" "
			+ "= ST_GeomFromText('%s') " + "WHERE \"%s\" = %s";
	public static final String POINT_TEMPLATE = "POINT(%s %s %s )";

	private BimConstants() {
		throw new AssertionError();
	}
}
