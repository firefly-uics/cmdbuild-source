package org.cmdbuild.bim.mapper.xml;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.geometry.BimserverGeometryHelper;
import org.cmdbuild.bim.geometry.IfcGeometryHelper;
import org.cmdbuild.bim.mapper.SpaceGeometryReader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.cmdbuild.bim.model.implementation.IfcPosition3d;
import org.cmdbuild.bim.model.implementation.SpaceGeometryImpl;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class BimserverSpaceGeometryReader implements SpaceGeometryReader{

	private static final Logger logger = LoggerSupport.geom_logger;
	
	private final String revisionId;
	private final BimService service;
	private final IfcGeometryHelper geomHelper;
	private String key;

	public BimserverSpaceGeometryReader(BimService service, String revisionId) {
		this.service = service;
		this.revisionId = revisionId;
		geomHelper = new BimserverGeometryHelper(service, revisionId);
	}

	@Override
	public SpaceGeometry computeCentroid(String spaceIdentifier) {
		key = spaceIdentifier;
		Entity space = service.getEntityByGuid(revisionId, key);
		SpaceGeometry geometry = new SpaceGeometryImpl();
		logger.info("");
		logger.info("");
		logger.info("Space: " + key + " Name: " + space.getAttributeByName("Name").getValue());
		if (!space.isValid()) {
			throw new BimError("No space found with given identifier");
		}

		IfcGeometryHelper geometryHelper = new BimserverGeometryHelper(service, revisionId);
		Position3d spacePosition = geometryHelper.getAbsoluteObjectPlacement(space);
		logger.info(spacePosition.toString());

		Attribute representationAttribute = space.getAttributeByName("Representation");
		if (representationAttribute.isValid()) {
			Entity shape = service.getReferencedEntity((ReferenceAttribute) representationAttribute, revisionId);
			if (!shape.isValid()) {
				throw new BimError("No shape found for given space");
			}
			Attribute representationList = shape.getAttributeByName("Representations");
			if (!representationList.isValid()) {
				throw new BimError("No valid representation found in shape");
			}
			int indexOfBB = getIndexOfBB(representationList);
			boolean isThereABB = indexOfBB != -1;
			logger.info("Is there a Bounding Box? " + isThereABB);
			if (isThereABB) {
				logger.info("Index of BoundingBox : " + indexOfBB);

				Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(indexOfBB),
						revisionId);

				geometry = getGeometryFromBoundingBox(representation);
				logger.info("Relative coordinates of centroid: " + geometry.getCentroid());

				Position3d absolutePlacement = geomHelper.getAbsoluteObjectPlacement(space);
				logger.info("Absolute placement of space: " + absolutePlacement);

				convertCoordinates(geometry.getCentroid(), absolutePlacement);
				logger.info("Absolute coordinates of centroid: " + geometry.getCentroid());
			} else {
				Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(0), revisionId);
				Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
				if (typeAttribute.isValid()) {
					SimpleAttribute type = (SimpleAttribute) typeAttribute;
					if (type.getValue().equals("SweptSolid")) {
						geometry = getGeometryFromSweptSolid(representation);
					}
				}
				logger.info("Relative coordinates of centroid: " + geometry.getCentroid());

				Position3d absolutePlacement = geomHelper.getAbsoluteObjectPlacement(space);
				logger.info("Absolute placement of space: " + absolutePlacement);

				convertCoordinates(geometry.getCentroid(), absolutePlacement);
				logger.info("Absolute coordinates of centroid: " + geometry.getCentroid());
			}
		}
		return geometry;
	}
	
	
	private SpaceGeometry getGeometryFromBoundingBox(Entity representation) {
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		Entity boundingBox = service.getReferencedEntity(item, revisionId);
		if (!boundingBox.getTypeName().equals("IfcBoundingBox")) {
			throw new BimError(
					"This is not an IfcBoundingBox. This is an unexpected problem, I do not know what to do.");
		}
		Attribute xDim = boundingBox.getAttributeByName("XDim");
		Attribute yDim = boundingBox.getAttributeByName("YDim");
		Attribute zDim = boundingBox.getAttributeByName("ZDim");
		Attribute cornerAttribute = boundingBox.getAttributeByName("Corner");
		if (!xDim.isValid() || !yDim.isValid() || !zDim.isValid() || !cornerAttribute.isValid()) {
			throw new BimError("Some attribute of the Bounding Box is not filled. I do not know what to do.");
		}
		Double dx = Double.parseDouble(xDim.getValue());
		Double dy = Double.parseDouble(yDim.getValue());
		Double dz = Double.parseDouble(zDim.getValue());

		Entity cornerPoint = service.getReferencedEntity((ReferenceAttribute) cornerAttribute, revisionId);
		Vector3d corner = geomHelper.getCoordinatesOfIfcCartesianPoint(cornerPoint);

		double[] centroidAsArray = { corner.x + dx / 2, corner.y + dy / 2, corner.z + dz / 2 };

		Vector3d centroid = new Vector3d(centroidAsArray);
		return new SpaceGeometryImpl(centroid, dx, dy, dz);
	}
	
	
	
	private SpaceGeometry getGeometryFromSweptSolid(Entity representation) {
		logger.info("Get geometry from Swept Solid...");
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		Entity sweptSolid = service.getReferencedEntity(item, revisionId);
		if (!sweptSolid.getTypeName().equals("IfcExtrudedAreaSolid")) {
			throw new BimError(
					"This is not an IfcExtrudedAreaSolid. This is an unexpected problem, I do not know what to do.");
		}
		Attribute positionAttribute = sweptSolid.getAttributeByName("Position");
		if (!positionAttribute.isValid()) {
			throw new BimError("Position attribute not found");
		}
		Entity position = service.getReferencedEntity((ReferenceAttribute) positionAttribute, revisionId);

		// SWEPT SOLID POSITION
		Position3d sweptSolidPosition = geomHelper.getPositionFromIfcPlacement(position);
		logger.info("IfcExtrudedAreaSolid.Position " + sweptSolidPosition);

		// DEPTH
		Attribute depth = sweptSolid.getAttributeByName("Depth");
		Double dz = new Double(0);
		if (depth.isValid()) {
			dz = Double.parseDouble(depth.getValue());
		}

		Attribute sweptAreaAttribute = sweptSolid.getAttributeByName("SweptArea");
		if (!sweptAreaAttribute.isValid()) {
			throw new BimError("Solid attribute not found");
		}
		Entity sweptArea = service.getReferencedEntity((ReferenceAttribute) sweptAreaAttribute, revisionId);

		Double dx = new Double(0);
		Double dy = new Double(0);

		Vector3d centroid = new Vector3d(0, 0, 0);

		List<Position3d> polylineVertices = Lists.newArrayList();
		if (sweptArea.getTypeName().equals("IfcRectangleProfileDef")) {
			Entity rectangleProfile = sweptArea;
			Attribute rectangleProfilePositionAttribute = rectangleProfile.getAttributeByName("Position");
			if (!rectangleProfilePositionAttribute.isValid()) {
				throw new BimError("Position attribute not found");
			}
			Entity rectangleProfilePositionEntity = service.getReferencedEntity(
					(ReferenceAttribute) rectangleProfilePositionAttribute, revisionId);

			// POSITION OF RECTANGLE PROFILE (centroid)
			Position3d rectangleProfilePosition = geomHelper.getPositionFromIfcPlacement(rectangleProfilePositionEntity);
			centroid = rectangleProfilePosition.getOrigin();

			Attribute xDimAttribute = rectangleProfile.getAttributeByName("XDim");
			Attribute yDimAttribute = rectangleProfile.getAttributeByName("YDim");
			if (!xDimAttribute.isValid() || !yDimAttribute.isValid()) {
				throw new BimError("Dimension attribute not found");
			}
			dx = Double.parseDouble(xDimAttribute.getValue());
			dy = Double.parseDouble(yDimAttribute.getValue());

		} else if (sweptArea.getTypeName().equals("IfcArbitraryClosedProfileDef") || sweptArea.getTypeName().equals("IfcArbitraryProfileDefWithVoids")) {
			Attribute outerCurveAttribute = sweptArea.getAttributeByName("OuterCurve");
			if (!outerCurveAttribute.isValid()) {
				throw new BimError("Outer Curve attribute not found");
			}
			Entity outerCurve = service.getReferencedEntity((ReferenceAttribute) outerCurveAttribute, revisionId);
			if (!outerCurve.getTypeName().equals("IfcPolyline")) {
				throw new BimError("Curve of type " + outerCurve.getTypeName() + " not handled");
			}
			Attribute pointsAttribute = outerCurve.getAttributeByName("Points");
			if (!pointsAttribute.isValid()) {
				throw new BimError("Points attribute not found");
			}
			ListAttribute edgesOfPolylineList = (ListAttribute) pointsAttribute;
			for (Attribute pointAttribute : edgesOfPolylineList.getValues()) {
				Entity edge = service.getReferencedEntity((ReferenceAttribute) pointAttribute, revisionId);
				Vector3d polylinePointCoordinates = geomHelper.getCoordinatesOfIfcCartesianPoint(edge);
				polylineVertices.add(new IfcPosition3d(polylinePointCoordinates));
			}
			
			// Centroid and dimensions of Arbitrary Profile
			centroid = geomHelper.computeCentroidFromPolyline(polylineVertices);
			dx = geomHelper.computeWidthFromPolyline(polylineVertices);
			dy = geomHelper.computeHeightFromPolyline(polylineVertices);
		} else {
			logger.info("IfcProfileDef of type " + sweptArea.getTypeName() + " not handled");
			return new SpaceGeometryImpl();
		}
		
		convertCoordinates(centroid, sweptSolidPosition);

		return new SpaceGeometryImpl(centroid, dx, dy, dz);
	}
	
	
	
	private void convertCoordinates(Vector3d P, Position3d referenceSystem) {
		referenceSystem.getVersorsMatrix().transform(P);
		P.add(referenceSystem.getOrigin());
	}

	private int getIndexOfBB(Attribute representationList) {
		int index = -1;
		for (Attribute value : ((ListAttribute) representationList).getValues()) {
			index++;
			Entity representation = service.getReferencedEntity((ReferenceAttribute) value, revisionId);
			Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
			String representationType = typeAttribute.getValue();
			if (representationType.equals("BoundingBox")) {
				return index;
			}
		}
		return -1;
	}
	
	
}
