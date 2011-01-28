package org.cmdbuild.elements.filters;

import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.interfaces.IAttribute;

@SuppressWarnings("serial")
public class BBoxFilter extends AbstractFilter {
	private IAttribute attribute;
	private String bbox;
	
	//the parameter for the format are: attribute, bbox and the projection
	private static final String QUERY_FORMAT = "(%s && SetSRID('BOX3D(%s)'::box3d,%d))";
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
		return String.format(QUERY_FORMAT, fullName, bbox, projection);
	}
	
	//convert the BBox from the open-layer format to the postgis format:
	//from (aa.aa,bb.bb,cc.cc,dd.dd) to (aa.aa bb.bb, cc.cc dd.dd)
	private String clearBBoxString(String bbox) {
		String[] coordinates = bbox.split(",");
		String cleanedBBox = String.format("%s %s, %s %s",(Object[]) coordinates);
		return cleanedBBox;
	}
}
