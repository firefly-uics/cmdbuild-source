package unit.serializers;

import junit.framework.TestCase;

import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.postgis.Geometry;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

public class TestGeoJSONSerializer extends TestCase {
	public GeoJSONSerializer serializer;
	
	@Before
	public void setUp() {
		serializer = new GeoJSONSerializer();
	}
	
	@Test
	public void testPointSerialization() throws JSONException {
		Geometry p = new Point(0.456 ,0.654);
		JSONObject json = serializer.serialize(p);
		JSONObject test = new JSONObject("{type:POINT,coordinates:[0.456,0.654]}");
		assertEquals("The point isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testMultiPointSerialization() throws JSONException {
		Point[] points = {new Point(0.1 ,0.1), new Point(0.2 ,0.2)};		
		MultiPoint mp = new MultiPoint(points);
		
		JSONObject json = serializer.serialize(mp);
		JSONObject test = new JSONObject("{type:MULTIPOINT,coordinates:[[0.1,0.1],[0.2,0.2]]}");
		
		assertEquals("The multiline isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testLineStringSerialization() throws JSONException {
		Point[] points = {new Point(0.1 ,0.1), new Point(0.2 ,0.2)};		
		LineString l = new LineString(points);
		
		JSONObject json = serializer.serialize(l);
		JSONObject test = new JSONObject("{type:LINESTRING,coordinates:[[0.1,0.1],[0.2,0.2]]}");
		
		assertEquals("The line isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testMultiLineStringSerialization() throws JSONException {
		Point[] points = {new Point(0.1 ,0.1), new Point(0.2 ,0.2)};		
		LineString[] lines = {new LineString(points), new LineString(points)};
		MultiLineString ml = new MultiLineString(lines);
		
		JSONObject json = serializer.serialize(ml);
		JSONObject test = new JSONObject("{type:MULTILINESTRING,coordinates:[[[0.1,0.1],[0.2,0.2]],[[0.1,0.1],[0.2,0.2]]]}");
		
		assertEquals("The multiline isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testPolygonSerialization() throws JSONException {
		Point[] points = {new Point(0.1 ,0.1), new Point(0.2 ,0.2), new Point(0.3 ,0.3), new Point(0.1 ,0.1)};
		LinearRing[] lr = {new LinearRing(points)};
		Polygon pl = new Polygon(lr);
		
		JSONObject json = serializer.serialize(pl);
		JSONObject test = new JSONObject("{type:POLYGON,coordinates:[[[0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1]]]}");
		
		assertEquals("The polygon isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testMultyPolygonSerialization() throws JSONException {
		Point[] points = {new Point(0.1 ,0.1), new Point(0.2 ,0.2), new Point(0.3 ,0.3), new Point(0.1 ,0.1)};
		LinearRing[] lr = {new LinearRing(points)};
		Polygon[] pl = {new Polygon(lr), new Polygon(lr)};
		MultiPolygon multyPolygon = new MultiPolygon(pl);
		
		JSONObject json = serializer.serialize(multyPolygon);
		JSONObject test = new JSONObject("{type:MULTIPOLYGON,"+
							"coordinates: ["+
								"[" +
									"[ [0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1] ]" +
								"]," +
								"[" +
									"[ [0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1] ]" +
								"]" +
							"]}");
		
		assertEquals("The multipolygon isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testGeometryCollectionSerialization() throws JSONException {
		Point point = new Point(100,0);
		Point[] points = {new Point(101 ,0), new Point(102 ,1)};
		LineString lineString = new LineString(points);
		Geometry[] geometries = {point,lineString};
		GeometryCollection collection = new GeometryCollection(geometries);
		
		JSONObject json = serializer.serialize(collection);
		JSONObject test = new JSONObject("{"+
				" type: GEOMETRYCOLLECTION,"+
				" geometries: [{" +
				"   type: POINT," +
				"   coordinates: [100.0, 0.0]" +
       			" },{"+
       			"   type: LINESTRING," +
       			"   coordinates: [ [101.0, 0.0], [102.0, 1.0] ]" +
       			" }]" +
				"}");
		
		assertEquals("The GeometryCollection isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testGetNewFeature() throws JSONException {
		JSONObject json = serializer.getNewFeature();
		JSONObject test = new JSONObject("{type:Feature,geometry:{},properties:{}}");
		assertEquals("The new feature isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testGetNewFeatureCollection() throws JSONException {
		JSONObject json = serializer.getNewFeatureCollection();
		JSONObject test = new JSONObject("{type:FeatureCollection,features:[]}");
		assertEquals("The new featureCollections isn't well serialized: ",test.toString(), json.toString());
	}
	
	@Test
	public void testGetNewGeometryCollection() throws JSONException {
		JSONObject json = serializer.getNewGeometryCollection();
		JSONObject test = new JSONObject("{type:GeometryCollection,geometries:[]}");
		assertEquals("The new featureCollections isn't well serialized: ",test.toString(), json.toString());
	}
}
