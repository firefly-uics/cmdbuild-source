package unit.bim.mapper.xml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import static org.cmdbuild.bim.utils.BimConstants.SPACEGEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.SPACEHEIGHT;

import com.google.common.collect.Lists;

public class PostgisFormatTest {
	
	@Test
	public void format3dLinestring() throws Exception {
		//given 
		SpaceGeometry geometry = mock(SpaceGeometry.class);
		List<Vector3d> vertexList = Lists.newArrayList();
		Vector3d v0 = new Vector3d(0, 0, 0);
		Vector3d v1 = new Vector3d(1, 0, 0);
		Vector3d v2 = new Vector3d(1, 1, 0);
		Vector3d v3 = new Vector3d(0, 1, 0);
		vertexList.add(v0);
		vertexList.add(v1);
		vertexList.add(v2);
		vertexList.add(v3);
		when(geometry.getVertexList()).thenReturn(vertexList);
		when(geometry.getZDim()).thenReturn(4000.);
		Entity retrievedEntity = new DefaultEntity("Pippo", null);
		
		//when
		BimReader.mapGeometryIntoBimEntity(retrievedEntity , geometry);
		
		//then
		Attribute geometryAttribute = retrievedEntity.getAttributeByName(SPACEGEOMETRY);
		String postgisString = geometryAttribute.getValue();
		assertThat(postgisString, equalTo("POLYGON((0.0 0.0 0.0,1.0 0.0 0.0,1.0 1.0 0.0,0.0 1.0 0.0,0.0 0.0 0.0))"));
		
		Attribute heightAttribute = retrievedEntity.getAttributeByName(SPACEHEIGHT);
		assertThat(heightAttribute.getValue(), equalTo(Double.toString(4000.)));
	}
	
	
	@Test
	public void formatPostgis3DSurface() throws Exception {

		// given
		SpaceGeometry geometry = mock(SpaceGeometry.class);

		List<Vector3d> vertexList = Lists.newArrayList();
		Vector3d v0 = new Vector3d(0, 0, 0);
		Vector3d v1 = new Vector3d(1, 0, 0);
		Vector3d v2 = new Vector3d(1, 1, 0);
		Vector3d v3 = new Vector3d(0, 1, 0);
		vertexList.add(v0);
		vertexList.add(v1);
		vertexList.add(v2);
		vertexList.add(v3);
		when(geometry.getVertexList()).thenReturn(vertexList);
		when(geometry.getZDim()).thenReturn(4000.);

		// when
		int numberOfVertices = geometry.getVertexList().size();
		String[] faces = new String[numberOfVertices + 2];

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("((");
		for (int i = 0; i < numberOfVertices; i++) {
			Vector3d currentVertex = vertexList.get(i);
			String pgVertexString = toPGString(currentVertex);
			stringBuilder.append(pgVertexString + ",");
			if (i == numberOfVertices - 1) {
				stringBuilder.append(toPGString(vertexList.get(0)) + "))");
			}
		}
		faces[0] = stringBuilder.toString();

		double h = geometry.getZDim();

		String formatStandardFace = "((%s,%s,%s,%s,%s))";
		for (int i = 0; i < numberOfVertices - 1; i++) {
			Vector3d w1 = vertexList.get(i);
			Vector3d w2 = vertexList.get(i + 1);
			Vector3d w3 = new Vector3d(w2.x, w2.y, w2.z + h);
			Vector3d w4 = new Vector3d(w1.x, w1.y, w1.z + h);

			faces[i + 1] = String.format(formatStandardFace, toPGString(w1),
					toPGString(w2), toPGString(w3), toPGString(w4),
					toPGString(w1));
		}

		Vector3d w1 = vertexList.get(numberOfVertices - 1);
		Vector3d w2 = vertexList.get(0);
		Vector3d w3 = new Vector3d(w2.x, w2.y, w2.z + h);
		Vector3d w4 = new Vector3d(w1.x, w1.y, w1.z + h);
		faces[numberOfVertices] = String.format(formatStandardFace,
				toPGString(w1), toPGString(w2), toPGString(w3), toPGString(w4),
				toPGString(w1));

		stringBuilder = new StringBuilder("((");
		for (int i = 0; i < numberOfVertices; i++) {
			Vector3d wi = vertexList.get(i);
			Vector3d wih = new Vector3d(wi.x, wi.y, wi.z + h);
			stringBuilder.append( toPGString(wih) + ",");
			if (i == numberOfVertices - 1) {
				Vector3d w0 = vertexList.get(0);
				Vector3d w0h = new Vector3d(w0.x, w0.y, w0.z + h);
				stringBuilder.append(toPGString(w0h) + "))");
			}
		}
		faces[numberOfVertices + 1] = stringBuilder.toString();

		stringBuilder = new StringBuilder("(");
		for (String face : faces) {
			stringBuilder.append(face + ",");
		}
		int start = stringBuilder.length() - 1;
		stringBuilder.replace(start, start + 1, ")");

		String theString = stringBuilder.toString();
		final String postgisGeometryString = "POLYHEDRALSURFACE" + theString;

		// then
		System.out.println(postgisGeometryString);
	}

	private String toPGString(Vector3d vertex) {
		String pgVertexFormat = "%s %s %s";
		String pgVertex = String.format(pgVertexFormat, vertex.x, vertex.y,
				vertex.z);
		return pgVertex;
	}

}
