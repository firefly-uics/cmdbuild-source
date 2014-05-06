package integration.services.bimserver.cli;

import static org.cmdbuild.bim.utils.BimConstants.IFC_AXIS2_PLACEMENT3D;
import static org.cmdbuild.bim.utils.BimConstants.IFC_BUILDING_ELEMENT_PROXY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_CARTESIAN_POINT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCAL_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCATION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_PRODUCT_DEFINITION_SHAPE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATIVE_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.bimserver.BimserverClient;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.DefaultBimserverClient;
import org.cmdbuild.bim.service.bimserver.SmartBimserverClient;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AddObjects {

	private BimService service;
	private BimserverClient client;
	private final String url = "http://localhost:11080";
	private final String username = "admin@tecnoteca.com";
	private final String password = "admin";

	@Before
	public void setUp() {
		final BimserverConfiguration configuration = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public void disable() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public String getUsername() {
				return username;
			}

			@Override
			public String getUrl() {
				return url;
			}

			@Override
			public String getPassword() {
				return password;
			}

			@Override
			public void addListener(final ChangeListener listener) {
			}
		};
		client = new SmartBimserverClient(new DefaultBimserverClient(configuration));
		service = new BimserverService(client);
		System.out.println("Connection established\n");
	}

	@Test
	public void createAndChangeOneAttribute() throws Exception {

		final String name = "PG-" + RandomStringUtils.randomAlphanumeric(5);
		final BimProject createProject = service.createProject(name);
		final String projectId = createProject.getIdentifier();
		System.out.println(projectId + " " + name);

		String transactionId = client.openTransaction(projectId);

		final String objectId = client.createObject(transactionId, "IfcFurnishingElement");
		final String guid = RandomStringUtils.randomAlphanumeric(22);
		client.setStringAttribute(transactionId, objectId, "GlobalId", guid);
		client.setStringAttribute(transactionId, objectId, "Description", "C1");

		String revisionId = client.commitTransaction(transactionId);
		refreshWithMerge(projectId);

		Iterable<Entity> entities = client.getEntitiesByType("IfcFurnishingElement", revisionId);
		assertTrue(Iterables.size(entities) == 1);

		transactionId = client.openTransaction(projectId);
		client.setStringAttribute(transactionId, objectId, "Description", "C2");

		revisionId = client.commitTransaction(transactionId);
		refreshWithMerge(projectId);

		entities = client.getEntitiesByType("IfcFurnishingElement", revisionId);
		assertTrue(Iterables.size(entities) == 1);

	}

	@Test
	public void createAndMoveObjectOnEmptyFile() throws Exception {

		final String name = "PG-" + RandomStringUtils.randomAlphanumeric(5);
		final BimProject createProject = service.createProject(name);
		final String projectId = createProject.getIdentifier();
		final URL url = ClassLoader.getSystemResource("cuboShape.ifc");
		final File file = new File(url.toURI());
		client.checkin(projectId, file, false);
		System.out.println(projectId + " " + name);

		String revisionId = client.getLastRevisionOfProject(projectId);
		final String shapeOid = findShapeWithName("Cubo", revisionId);

		String transactionId = client.openTransaction(projectId);

		final String objectId = client.createObject(transactionId, "IfcFurnishingElement");
		final String guid = RandomStringUtils.randomAlphanumeric(22);
		client.setStringAttribute(transactionId, objectId, "GlobalId", guid);
		client.setStringAttribute(transactionId, objectId, "Description", "C1");
		final String placementOid = service.createObject(transactionId, IFC_LOCAL_PLACEMENT);
		service.setReference(transactionId, objectId, IFC_OBJECT_PLACEMENT, placementOid);

		final double x1d = 1;
		final double x2d = 1;
		final double x3d = 1;

		final String relativePlacementId = service.createObject(transactionId, IFC_AXIS2_PLACEMENT3D);
		service.setReference(transactionId, placementOid, IFC_RELATIVE_PLACEMENT, relativePlacementId);
		final String cartesianPointId = service.createObject(transactionId, IFC_CARTESIAN_POINT);
		System.out.println("Set coordinates " + x1d + " " + x2d + " " + x3d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x1d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x2d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x3d);
		service.setReference(transactionId, relativePlacementId, IFC_LOCATION, cartesianPointId);

		service.setReference(transactionId, objectId, "Representation", shapeOid);
		revisionId = client.commitTransaction(transactionId);
		refreshWithMerge(projectId);

		Iterable<Entity> entities = client.getEntitiesByType("IfcFurnishingElement", revisionId);
		assertTrue(Iterables.size(entities) == 1);
		final double x1d1 = 2;
		final double x2d1 = 2;
		final double x3d1 = 2;

		transactionId = client.openTransaction(projectId);
		client.setDoubleAttributes(transactionId, cartesianPointId, IFC_COORDINATES,
				Lists.newArrayList(x1d1, x2d1, x3d1));
		revisionId = client.commitTransaction(transactionId);

		entities = client.getEntitiesByType("IfcFurnishingElement", revisionId);
		assertTrue(Iterables.size(entities) == 1);

		final double x1d2 = 3;
		final double x2d2 = 3;
		final double x3d2 = 3;

		transactionId = client.openTransaction(projectId);
		client.setDoubleAttributes(transactionId, cartesianPointId, IFC_COORDINATES,
				Lists.newArrayList(x1d2, x2d2, x3d2));
		revisionId = client.commitTransaction(transactionId);
		refreshWithMerge(projectId);

		entities = client.getEntitiesByType("IfcFurnishingElement", revisionId);
		assertTrue(Iterables.size(entities) == 1);
	}

	@Test
	public void createAndMoveObject() throws Exception {

		final String name = "PG-" + RandomStringUtils.randomAlphanumeric(5);
		final BimProject createProject = service.createProject(name);
		final String projectId = createProject.getIdentifier();
		final URL url = ClassLoader.getSystemResource("conUnOggetto.ifc");
		final File file = new File(url.toURI());
		client.checkin(projectId, file, false);
		System.out.println(projectId + " " + name);

		String revisionId = client.getLastRevisionOfProject(projectId);

		Iterable<Entity> entities = client.getEntitiesByType(IFC_BUILDING_ELEMENT_PROXY, revisionId);
		System.out.println("There are " + Iterables.size(entities) + " elements");

		String transaction = client.openTransaction(projectId);
		System.out.println("first transaction " + transaction);
		revisionId = client.getLastRevisionOfProject(projectId);
		BimserverEntity object = (BimserverEntity) client.getEntityByGuid(revisionId, "giSfIP0JmaXLNQOHiUAT92",
				Lists.newArrayList(IFC_BUILDING_ELEMENT_PROXY));
		BimserverEntity objectPlacement = (BimserverEntity) getReferencedEntity(revisionId, object,
				IFC_OBJECT_PLACEMENT);
		BimserverEntity relativePlacement = (BimserverEntity) getReferencedEntity(revisionId, objectPlacement,
				IFC_RELATIVE_PLACEMENT);
		BimserverEntity cartesianPoint = (BimserverEntity) getReferencedEntity(revisionId, relativePlacement,
				IFC_LOCATION);
		String cartesianPointId = String.valueOf(cartesianPoint.getOid());

		List<Double> coordinates = Lists.newArrayList((double) 1, (double) 1, (double) 1);
		service.setDoubleAttributes(transaction, cartesianPointId, IFC_COORDINATES, coordinates);

		client.commitTransaction(transaction);
		refreshWithMerge(projectId);

		transaction = client.openTransaction(projectId);
		System.out.println("second transaction " + transaction);
		revisionId = client.getLastRevisionOfProject(projectId);
		object = (BimserverEntity) client.getEntityByGuid(revisionId, "giSfIP0JmaXLNQOHiUAT92",
				Lists.newArrayList(IFC_BUILDING_ELEMENT_PROXY));
		objectPlacement = (BimserverEntity) getReferencedEntity(revisionId, object, IFC_OBJECT_PLACEMENT);
		relativePlacement = (BimserverEntity) getReferencedEntity(revisionId, objectPlacement, IFC_RELATIVE_PLACEMENT);
		cartesianPoint = (BimserverEntity) getReferencedEntity(revisionId, relativePlacement, IFC_LOCATION);
		cartesianPointId = String.valueOf(cartesianPoint.getOid());

		coordinates = Lists.newArrayList((double) 2, (double) 2, (double) 2);
		service.setDoubleAttributes(transaction, cartesianPointId, IFC_COORDINATES, coordinates);

		client.commitTransaction(transaction);
		refreshWithMerge(projectId);

		entities = client.getEntitiesByType("IfcBuildingElementProxy", client.getLastRevisionOfProject(projectId));
		System.out.println("There are " + Iterables.size(entities) + " elements");

	}

	private void refreshWithMerge(final String projectId) {
		try {
			final DataHandler exportedData = client.downloadIfc(client.getLastRevisionOfProject(projectId));
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			exportedData.writeTo(outputStream);
			client.checkin(projectId, file, true);
			System.out.println("Project refreshed");
		} catch (final Throwable t) {
			throw new BimError("Problems while refreshing project", t);
		}
	}

	private Entity getReferencedEntity(final String revisionId, final Entity object, final String attributeName) {
		final long objectPlacementOid = ReferenceAttribute.class.cast(object.getAttributeByName(attributeName))
				.getOid();
		final Entity objectPlacement = service.getEntityByOid(revisionId, Long.toString(objectPlacementOid));
		return objectPlacement;
	}

	private String findShapeWithName(final String shapeName, final String revisionId) {
		final Iterable<Entity> shapeList = service.getEntitiesByType(IFC_PRODUCT_DEFINITION_SHAPE, revisionId);
		for (final Entity shape : shapeList) {
			final Attribute shapeNameAttribute = shape.getAttributeByName("Name");
			if (shapeNameAttribute.getValue() != null && shapeNameAttribute.getValue().equals(shapeName)) {
				System.out.println("Shape found with id " + shape.getKey());
				return shape.getKey();
			}
		}
		return INVALID_ID;
	}

}
