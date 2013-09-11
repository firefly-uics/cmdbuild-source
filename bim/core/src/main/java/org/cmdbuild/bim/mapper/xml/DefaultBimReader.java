package org.cmdbuild.bim.mapper.xml;

import java.util.List;

import org.cmdbuild.bim.geometry.BimserverGeometryHelper;
import org.cmdbuild.bim.geometry.IfcGeometryHelper;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.implementation.ListAttributeDefinition;
import org.cmdbuild.bim.model.implementation.ReferenceAttributeDefinition;
import org.cmdbuild.bim.model.implementation.SimpleAttributeDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;

import com.google.common.collect.Lists;

public class DefaultBimReader implements Reader {

	private static final String GLOBAL_ID = "GlobalId";
	private final BimService service;
	private IfcGeometryHelper geometryHelper;

	public DefaultBimReader(final BimService service) {
		this.service = service;
	}
	
	private IfcGeometryHelper geometryHelper(){
		return geometryHelper;
	}

	@Override
	public List<Entity> readEntities(final String revisionId, final EntityDefinition entityDefinition)  {
		final List<Entity> entities = Lists.newArrayList();
		read(revisionId, new ReaderListener() {
			@Override
			public void retrieved(final Entity entity) {
				entities.add(entity);
			}
			
		}, entityDefinition);
		return entities;
	}

	private void read(String revisionId, ReaderListener listener, EntityDefinition entityDefinition) {

		System.out.println("reading data for revision " + revisionId + " for class " + entityDefinition.getTypeName()
				+ " corresponding to " + entityDefinition.getLabel());
		
		geometryHelper = new BimserverGeometryHelper(service, revisionId);
		
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
		Attribute globalId = entity.getAttributeByName(GLOBAL_ID);
		if (globalId.isValid()) {
			((BimEntity) retrievedEntity).addAttribute(new BimAttribute(GLOBAL_ID, ((SimpleAttribute) globalId)
					.getStringValue()));
		}
		boolean exit = false;
		//fetch and store all the other attributes
		for (AttributeDefinition attributeDefinition : attributesToRead) {
			System.out.println("attribute " + attributeDefinition.getName() + " of entity " + entity.getTypeName());
			if (!exit) {
				String attributeName = attributeDefinition.getName();
				if (attributeName.equals("_Coordinates")) {
					computeCoordinatesUsingGeometryHelper(entity, revisionId, retrievedEntity);
					System.out.println("coordinates not managed yet!");
				} else if (attributeName.equals("_Centroid")) {
					System.out.println("centroid not managed yet!");
				} else if (attributeName.equals("_Container")) {
					System.out.println("container not managed yet!");
				} else {
					Attribute attribute = entity.getAttributeByName(attributeName);
					if (attribute.isValid()) {
						if (attributeDefinition instanceof SimpleAttributeDefinition) {
							SimpleAttributeDefinition simpleAttributeDefinition = (SimpleAttributeDefinition) attributeDefinition;
							if (simpleAttributeDefinition.getValue() != "") {
								System.out.println(attributeName + " must have value " + simpleAttributeDefinition.getValue());
								System.out.println("It has value " + attribute.getValue());
								if (!simpleAttributeDefinition.getValue().equals(attribute.getValue())) {
									System.out.println("skip this entity");
									exit = true;
									return false;
								}
							}
							if (!exit) {
								SimpleAttribute simpleAttribute = (SimpleAttribute) attribute;
								System.out.println(attributeDefinition.getLabel() + ": " + simpleAttribute.getStringValue());
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
							int n = list.getValues().size();
							for (int i = 0; i < n; i++) {
								
								Attribute value = list.getValues().get(i);
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
	
	
	private void computeCoordinatesUsingGeometryHelper(Entity entity, String revisionId, Entity retrievedEntity) {
		Position3d position = geometryHelper().getAbsoluteObjectPlacement(entity);
		BimAttribute x1Attr = new BimAttribute("x1", Double.toString(position.getOrigin().x));
		BimAttribute x2Attr = new BimAttribute("x2", Double.toString(position.getOrigin().y));
		BimAttribute x3Attr = new BimAttribute("x3", Double.toString(position.getOrigin().z));
		retrievedEntity.getAttributes().add(x1Attr);
		retrievedEntity.getAttributes().add(x2Attr);
		retrievedEntity.getAttributes().add(x3Attr);
	}

}
