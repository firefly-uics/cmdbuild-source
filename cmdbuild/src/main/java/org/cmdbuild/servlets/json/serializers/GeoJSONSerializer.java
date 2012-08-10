package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.services.gis.GeoFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.Geometry;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

public class GeoJSONSerializer {

	public JSONObject serialize(GeoFeature feature) throws JSONException {
		JSONObject jsonGeometry = serialize(feature.getGeometry());
		JSONObject properties = new JSONObject();
		properties.put("master_class", feature.getMasterCard().getSchema().getId());
		properties.put("master_card", feature.getMasterCard().getId());
		return getNewFeature(jsonGeometry, properties);
	}

	public JSONObject serialize(Geometry geom) throws JSONException {
		
		JSONObject jsonGeom = new JSONObject();
		JSONArray coordinates = new JSONArray();
		String type;
		
		switch (geom.getType()) {
			case Geometry.POINT: {
				Point p = (Point) geom;
				coordinates = getJSONPointCoordinates(p);
				jsonGeom.put("coordinates", coordinates);
			}; break;
			case Geometry.MULTIPOINT: {
				MultiPoint mp = (MultiPoint) geom;
				Point[] points = mp.getPoints();
				for (Point p: points) {
					coordinates.put(getJSONPointCoordinates(p));
				}
				jsonGeom.put("coordinates", coordinates);
			}; break;
			case Geometry.LINESTRING: {
				LineString l = (LineString) geom;
				coordinates = getJSONLineCoordinates(l);
				jsonGeom.put("coordinates", coordinates);
			}; break;
			case Geometry.MULTILINESTRING: {
				MultiLineString ml = (MultiLineString) geom;
				LineString[] lines = ml.getLines();
				for (LineString l: lines) {
					coordinates.put(getJSONLineCoordinates(l));
				}
				jsonGeom.put("coordinates", coordinates);
			}; break;
			case Geometry.POLYGON: {
				Polygon polygon = (Polygon) geom;
				jsonGeom.put("coordinates", getJSONPolygonCoorditanes(polygon));
			}; break;
			case Geometry.MULTIPOLYGON: {
				MultiPolygon multiPolygon = (MultiPolygon) geom;
				Polygon[] polygons = multiPolygon.getPolygons();
				for (Polygon polygon: polygons) {
					coordinates.put(getJSONPolygonCoorditanes(polygon));
				}
				jsonGeom.put("coordinates", coordinates);
			}; break;
			case Geometry.GEOMETRYCOLLECTION: {
				GeometryCollection collection = (GeometryCollection) geom;
				Geometry[] geometries = collection.getGeometries();
				JSONArray jsonGeometries = new JSONArray();
				for (Geometry geometry: geometries) {
					jsonGeometries.put(this.serialize(geometry));
				}
				jsonGeom.put("geometries", jsonGeometries);
			}; break;
			
			default: {
				throw new JSONException(String.format("Type %s is not supported", Geometry.getTypeString(geom.getType())));
			}
		}
				
		type = Geometry.getTypeString(geom.getType());
		jsonGeom.put("type", type);
		return jsonGeom;
	}
	
	public JSONObject getNewFeature() throws JSONException {
		return getNewFeature(new JSONObject(), new JSONObject());
	}
	
	public JSONObject getNewFeature(JSONObject geometry) throws JSONException {
		return getNewFeature(geometry, new JSONObject());
	}
	
	public JSONObject getNewFeature(JSONObject geometry, JSONObject properties) throws JSONException {
		JSONObject feature =  new JSONObject();
		feature.put("type", "Feature");
		feature.put("geometry", geometry);
		feature.put("properties", properties);
		return feature;
	}
	
	public JSONObject getNewFeatureCollection() throws JSONException {
		return getNewFeatureCollection(new JSONArray());
	}
	
	public JSONObject getNewFeatureCollection(JSONArray features) throws JSONException {
		JSONObject featureCollection =  new JSONObject();
		featureCollection.put("type", "FeatureCollection");
		featureCollection.put("features", features);
		return featureCollection;
	}
	
	public JSONObject getNewGeometryCollection() throws JSONException {
		return getNewGeometryCollection(new JSONArray());
	}
	
	public JSONObject getNewGeometryCollection(JSONArray geometries) throws JSONException {
		JSONObject geometry = new JSONObject();
		geometry.put("type", "GeometryCollection");
		geometry.put("geometries", geometries);
		return geometry;
	}
	
	private JSONArray getJSONPointCoordinates(Point p) throws JSONException {
		JSONArray coordinates = new JSONArray();
		coordinates.put(p.getX()).put(p.getY());
		return coordinates;
	}
	
	private JSONArray getJSONLineCoordinates(LineString l) throws JSONException {
		JSONArray coordinates = new JSONArray();
		Point[] points = l.getPoints();
		for (Point p: points) {
			coordinates.put(getJSONPointCoordinates(p));
		}
		return coordinates;
	}
	
	private JSONArray getJSONRingCoordinates(LinearRing l) throws JSONException {
		LineString ls = new LineString(l.getPoints());
		return getJSONLineCoordinates(ls);
	}
	
	private JSONArray getJSONPolygonCoorditanes(Polygon polygon) throws JSONException {
		JSONArray rings = new JSONArray();
		int i; LinearRing ring;
		for(i=0, ring=polygon.getRing(i); ring != null; ring=polygon.getRing(++i)) {				
			rings.put(getJSONRingCoordinates(ring));
		}
		return rings;
	}
}
