package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;
import java.util.Map;

import org.postgis.Geometry;

public class GeoCard  {
	private GeoClass type;
	private Map<String, Geometry> geometries;
	
	public GeoCard(GeoClass type) {
		this.type = type;
		geometries = new HashMap<String, Geometry>(); 
	}
	
	public GeoClass getType(){
		return type;
	}

	public Geometry get(String name) {
		return geometries.get(name);
	}

	public void set(String name, Geometry geometry) {
		geometries.put(name, geometry);
	}
}
