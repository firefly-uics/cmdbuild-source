package org.cmdbuild.bim.mapper.xml;

import static org.cmdbuild.bim.utils.BimConstants.CONTAINER;
import static org.cmdbuild.bim.utils.BimConstants.COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.GEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATED_ELEMENTS;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATING_STRUCTURE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_REL_CONTAINED;
import static org.cmdbuild.bim.utils.BimConstants.POINT_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.SPACEGEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.SPACEHEIGHT;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.geometry.DefaultIfcGeometryHelper;
import org.cmdbuild.bim.geometry.IfcGeometryHelper;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.cmdbuild.bim.model.implementation.ListAttributeDefinition;
import org.cmdbuild.bim.model.implementation.ReferenceAttributeDefinition;
import org.cmdbuild.bim.model.implementation.SimpleAttributeDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BimReader implements Reader {


	private final BimService service;
	private IfcGeometryHelper geometryHelper;
	private final Map<String, String> containersMap = Maps.newHashMap();
	private String revisionId;

	public BimReader(final BimService service) {
		this.service = service;
	}

	private IfcGeometryHelper geometryHelper() {
		return geometryHelper;
	}

	@Override
	public List<Entity> readEntities(final String revisionId, final EntityDefinition entityDefinition) {
		this.revisionId = revisionId;
		final List<Entity> entities = Lists.newArrayList();
		read(new ReaderListener() {
			@Override
			public void retrieved(final Entity entity) {
				entities.add(entity);
			}

		}, entityDefinition);
		return entities;
	}

	private void read(ReaderListener listener, EntityDefinition entityDefinition) {

		System.out.println("reading data for revision " + revisionId + " for class " + entityDefinition.getTypeName()
				+ " corresponding to " + entityDefinition.getLabel());

		geometryHelper = new DefaultIfcGeometryHelper(service, revisionId);
		if (entityDefinition.isValid()) {
			List<Entity> entities = service.getEntitiesByType(revisionId, entityDefinition.getTypeName());
			if (entities.size() == 0) {
				throw new BimError("No entities of type " + entityDefinition.getTypeName() + " found in revision "
						+ revisionId);
			}
			System.out.println(entities.size() + " entities found");
			for (Entity entity : entities) {
				Entity retrievedEntity = new BimEntity(entityDefinition.getLabel());
				boolean toInsert = readEntityAttributes(entity, entityDefinition, revisionId, retrievedEntity);
				if (toInsert) {
					listener.retrieved(retrievedEntity);
				}
			}
		}
	}

	private boolean readEntityAttributes(Entity entity, EntityDefinition entityDefinition, String revisionId,
			Entity retrievedEntity) {
		Iterable<AttributeDefinition> attributesToRead = entityDefinition.getAttributes();
		// fetch and store the GlobalId
		Attribute globalId = entity.getAttributeByName(IFC_GLOBALID);
		if (globalId.isValid()) {
			((BimEntity) retrievedEntity).addAttribute(new BimAttribute(IFC_GLOBALID, ((SimpleAttribute) globalId)
					.getStringValue()));
		}
		boolean exit = false;
		// fetch and store all the other attributes
		for (AttributeDefinition attributeDefinition : attributesToRead) {
			System.out.println("attribute " + attributeDefinition.getName() + " of entity " + entity.getTypeName());
			if (!exit) {
				String attributeName = attributeDefinition.getName();
				if (attributeName.equals(COORDINATES)) {
					fetchCoordinates(entity, revisionId, retrievedEntity);
				} else if (attributeName.equals(GEOMETRY)) {
					fetchGeometry(entity.getKey(), retrievedEntity);
				} else if (attributeName.equals(CONTAINER)) {
					fetchContainerKey(entity.getKey(), retrievedEntity, attributeDefinition.getLabel());
				} else {
					Attribute attribute = entity.getAttributeByName(attributeName);
					if (attribute.isValid()) {
						if (attributeDefinition instanceof SimpleAttributeDefinition) {
							SimpleAttributeDefinition simpleAttributeDefinition = (SimpleAttributeDefinition) attributeDefinition;
							if (simpleAttributeDefinition.getValue() != "") {
								System.out.println(attributeName + " must have value "
										+ simpleAttributeDefinition.getValue());
								System.out.println("It has value " + attribute.getValue());
								if (!simpleAttributeDefinition.getValue().equals(attribute.getValue())) {
									System.out.println("skip this entity");
									exit = true;
									return false;
								}
							}
							if (!exit) {
								SimpleAttribute simpleAttribute = (SimpleAttribute) attribute;
								System.out.println(attributeDefinition.getLabel() + ": "
										+ simpleAttribute.getStringValue());
								Attribute retrievedAttribute = new BimAttribute(attributeDefinition.getLabel(),
										simpleAttribute.getStringValue());
								((BimEntity) retrievedEntity).addAttribute(retrievedAttribute);
							}
						} else if (attributeDefinition instanceof ReferenceAttributeDefinition) {
							ReferenceAttribute referenceAttribute = (ReferenceAttribute) attribute;
							Entity referencedEntity = service.getReferencedEntity(referenceAttribute, revisionId);
							EntityDefinition referencedEntityDefinition = attributeDefinition.getReference();
							if (referencedEntity.isValid() && referencedEntityDefinition.isValid()) {
								readEntityAttributes(referencedEntity, referencedEntityDefinition, revisionId,
										retrievedEntity);
							} else {
								System.out.println("referenced entity valid " + referencedEntity.isValid());
							}
						} else if (attributeDefinition instanceof ListAttributeDefinition) {
							ListAttribute list = (ListAttribute) attribute;
							int count = 1;
							for (Attribute value : list.getValues()) {
								if (value instanceof ReferenceAttribute) {
									ReferenceAttribute referenceAttribute = (ReferenceAttribute) value;
									Entity referencedEntity = service.getReferencedEntity(referenceAttribute,
											revisionId);

									for (EntityDefinition nestedEntityDefinition : ((ListAttributeDefinition) attributeDefinition)
											.getAllReferences()) {
										if (referencedEntity.isValid() && nestedEntityDefinition.isValid()) {
											readEntityAttributes(referencedEntity, nestedEntityDefinition, revisionId,
													retrievedEntity);
										} else {

										}
									}
								} else {
									SimpleAttribute simpleAttribute = (SimpleAttribute) value;
									if (list.getValues().size() > 1) {

										Attribute retrievedAttribute = new BimAttribute(attributeDefinition.getLabel()
												+ "" + count, simpleAttribute.getStringValue());
										((BimEntity) retrievedEntity).addAttribute(retrievedAttribute);
									} else {

										Attribute retrievedAttribute = new BimAttribute(attributeDefinition.getLabel(),
												simpleAttribute.getStringValue());
										((BimEntity) retrievedEntity).addAttribute(retrievedAttribute);
									}
									count++;
								}
							}
						}
					} else {

					}
				}
			}
		}
		return true;
	}

	private void fetchGeometry(String key, Entity retrievedEntity) {
		SpaceGeometry geometry = geometryHelper.fetchGeometry(key);
		if(geometry != null){
			mapGeometryIntoBimEntity(retrievedEntity, geometry);
		}
	}

	private void fetchCoordinates(Entity entity, String revisionId, Entity retrievedEntity) {
		Position3d position = geometryHelper().getAbsoluteObjectPlacement(entity);
		String pgPoint = postgisFormat(position);

		BimAttribute coordinatesAttribute = new BimAttribute(COORDINATES, pgPoint);
		retrievedEntity.getAttributes().add(coordinatesAttribute);
	}

	// If we decide to store coordinates as double[3], we will need to modify
	// only this method.
	private String postgisFormat(Position3d position) {
		String x = Double.toString(position.getOrigin().x);
		String y = Double.toString(position.getOrigin().y);
		String z = Double.toString(position.getOrigin().z);
		return String.format(POINT_TEMPLATE, x, y, z);
	}

	private void fetchContainerKey(String key, Entity retrivedEntity, String attributeName) {
		List<Entity> ifcRelations = service.getEntitiesByType(revisionId, IFC_REL_CONTAINED);
		String containerKey = "";
		boolean found = false;
		if (containersMap.containsKey(key)) {
			containerKey = containersMap.get(key);
		} else {
			for (Entity ifcRelation : ifcRelations) {
				ReferenceAttribute container = (ReferenceAttribute) ifcRelation
						.getAttributeByName(IFC_RELATING_STRUCTURE);
				ListAttribute relatedElements = (ListAttribute) ifcRelation.getAttributeByName(IFC_RELATED_ELEMENTS);
				for (Attribute relatedElementReference : relatedElements.getValues()) {
					Entity relatedElement = service.getReferencedEntity((ReferenceAttribute) relatedElementReference,
							revisionId);
					containersMap.put(relatedElement.getKey(), container.getGlobalId());
					if (relatedElement.getKey().equals(key)) {
						found = true;
						containerKey = container.getGlobalId();
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		if (!containerKey.isEmpty()) {
			BimAttribute container = new BimAttribute(attributeName, containerKey);
			retrivedEntity.getAttributes().add(container);
		}
	}

	public static void mapGeometryIntoBimEntity(Entity retrievedEntity, SpaceGeometry geometry) {
		
		StringBuilder postgisLinestringBuilder = new StringBuilder("POLYGON((");
		final String pointFormat = "%s %s %s";
		for(Vector3d point : geometry.getVertexList()){
			postgisLinestringBuilder.append(String.format(pointFormat, point.x, point.y, point.z));
			postgisLinestringBuilder.append(",");
		}
		Vector3d firstPoint =  geometry.getVertexList().get(0);
		postgisLinestringBuilder.append(String.format(pointFormat, firstPoint.x, firstPoint.y, firstPoint.z));
		postgisLinestringBuilder.append(",");
		
		int indexOfLastComma = postgisLinestringBuilder.lastIndexOf(",");
		postgisLinestringBuilder.replace(indexOfLastComma, indexOfLastComma + 1, "))");

		BimAttribute room_geometry = new BimAttribute(SPACEGEOMETRY,postgisLinestringBuilder.toString());
		retrievedEntity.getAttributes().add(room_geometry);
		
		BimAttribute room_height = new BimAttribute(SPACEHEIGHT, Double.toString(geometry.getZDim()));
		retrievedEntity.getAttributes().add(room_height);
	}
	
}
