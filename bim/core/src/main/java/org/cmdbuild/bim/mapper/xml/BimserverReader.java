package org.cmdbuild.bim.mapper.xml;

import java.util.List;

import org.cmdbuild.bim.geometry.BimserverGeometryHelper;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.Reader.ReaderListener;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.CatalogFactory;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
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

public class BimserverReader implements Reader {

	private final BimService service;
	private final Catalog catalog;


	public BimserverReader(final BimService service, final CatalogFactory catalogFactory) {
		this.service = service;
		catalog = catalogFactory.create();
		System.out.println("Catalog created");
	}
	
	public BimserverReader(final CatalogFactory catalogFactory) {
		catalog = catalogFactory.create();
		service = null; // I think that it will not be used anymore - ServiceFacade knows which is the service. 
	}

	@Override
	public void read(String revisionId, ReaderListener listener) {
	
	}

	@Override
	public void read(String revisionId, ReaderListener listener, int i) {
		EntityDefinition entityDefinition = catalog.getEntityDefinition(i);
		System.out.println("reading data for revision " + revisionId + " for class " + entityDefinition.getTypeName()
				+ " corresponding to " + entityDefinition.getLabel());
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

	@Override
	public Iterable<Entity> readEntities(final String revisionId, int i) {
		final List<Entity> entities = Lists.newArrayList();
		read(revisionId, new ReaderListener() {

			@Override
			public void retrieved(final Entity entity) {
				entities.add(entity);
			}

		}, i);
		return entities;
	}

	@Override
	public Iterable<Entity> read(final String revisionId) {
		return null;
	}

	@Override
	public String getCmdbClass(int i) {
		EntityDefinition entityDefinition = catalog.getEntityDefinition(i);
		return entityDefinition.getLabel();
	}

	@Override
	public int getNumberOfEntititesDefinitions() {
		return catalog.getSize();
	}

	@Override
	public String getIfcType(int i) {
		EntityDefinition entityDefinition = catalog.getEntityDefinition(i);
		return entityDefinition.getTypeName();
	}


	private boolean readEntityAttributes(Entity entity, EntityDefinition entityDefinition, String revisionId,
			Entity retrievedEntity) {
		Iterable<AttributeDefinition> attributesToRead = entityDefinition.getAttributes();
		boolean exit = false;
		for (AttributeDefinition attributeDefinition : attributesToRead) {
			System.out.println("attribute " + attributeDefinition.getName() + " of entity " + entity.getTypeName());
			if (!exit) {
				String attributeName = attributeDefinition.getName();
				if (attributeName.equals("_Coordinates")) {
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


}
