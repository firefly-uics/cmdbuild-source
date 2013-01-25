package org.cmdbuild.elements.filters;

import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.services.DBService;

@SuppressWarnings("serial")
public class BBoxFilter extends AbstractFilter {
	private IAttribute attribute;
	private String bbox;

	//the parameter for the format are: attribute, bbox and the projection
	private static final int projection = 900913;

	public BBoxFilter(IAttribute attribute, String bbox) {
		this.attribute = attribute;
		this.bbox = clearBBoxString(bbox);
	}

	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		if (queryMapping != null && queryMapping.containsKey(attribute.getName())) {
			return toString(queryMapping.get(attribute.getName()).getValueName() );
		} else {
			return toString("\"" + attribute.getSchema().getDBName() + "\".\"" +  attribute.getName() + "\"");
		}
	}

	private String toString(String fullName) {
		String format = getQueryFormat();
		if (format != null) {
			return String.format(format, fullName, bbox, projection);
		} else {
			return "FALSE";
		}
	}

	//convert the BBox from the open-layer format to the postgis format:
	//from (aa.aa,bb.bb,cc.cc,dd.dd) to (aa.aa bb.bb, cc.cc dd.dd)
	private String clearBBoxString(String bbox) {
		String[] coordinates = bbox.split(",");
		String cleanedBBox = String.format("%s %s, %s %s",(Object[]) coordinates);
		return cleanedBBox;
	}

	/**
	 * Check the version of postGIS because from 2.0.0 the
	 * function to set a SRID changed the signature
	 * 
	 * @return the string format for the bbox filter
	 */
	private String getQueryFormat() {
		DBService.getInstance();
		String postGISVersion = DBService.getPostGISVersion();
		if (postGISVersion != null) {
			if ("2.0.0".compareTo(postGISVersion) >= 0) {
				return "(%s && SetSRID('BOX3D(%s)'::box3d,%d))";
			} else {
				return "(%s && ST_SetSRID('BOX3D(%s)'::box3d,%d))";
			}
		}
		return null;
	}
}
