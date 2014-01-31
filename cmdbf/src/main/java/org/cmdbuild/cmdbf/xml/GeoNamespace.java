package org.cmdbuild.cmdbf.xml;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.store.DBLayerMetadataStore;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GeoNamespace extends AbstractNamespace {
	private GISLogic gisLogic;
	private DBLayerMetadataStore layerMetadataStore;
	public static final String GEO_NAME = "name";
	public static final String GEO_DESCRIPTION = "description";
	public static final String GEO_MAPSTYLE = "mapStyle";
	public static final String GEO_TYPE = "type";
	public static final String GEO_INDEX = "index";
	public static final String GEO_MINIMUMZOOM = "minimumZoom";
	public static final String GEO_MAXIMUMZOOM = "maximumZoom";
	public static final String GEO_VISIBILITY = "visibility";

	public GeoNamespace(String name, CMDataView dataView, GISLogic gisLogic, CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
		this.gisLogic = gisLogic;
		this.layerMetadataStore = new DBLayerMetadataStore(dataView);
	}
	
	@Override	
	public boolean isEnabled() {
		return gisLogic.isGisEnabled();
	}

	@Override
	public XmlSchema getSchema() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);	
			Document document = documentBuilderFactory.newDocumentBuilder().newDocument();		
			XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
			XmlSchema schema = null;
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			//schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
			schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
			
			for(GeoClass geoClass : getTypes(GeoClass.class)) {
				XmlSchemaType type = getXsd(geoClass, document, schema);
				XmlSchemaElement element = new XmlSchemaElement(/*schema, true*/);
				schema.getItems().add(element);
				element.setSchemaTypeName(type.getQName());
				element.setName(type.getName());
			}
			return schema;
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean updateSchema(XmlSchema schema) {
		try {
			boolean updated = false;
			if(getNamespaceURI().equals(schema.getTargetNamespace())) {
				//for(XmlSchemaElement element : schema.getElements().values())
				Iterator<?> iterator = schema.getElements().getValues();
				while(iterator.hasNext()) {
					XmlSchemaElement element = (XmlSchemaElement)iterator.next();
					GeoClassFromXsd(element, schema);
				}
				updated = true;
			}
			return updated;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Override
	public Iterable<GeoClass> getTypes(Class<?> cls) {
		if(GeoClass.class.isAssignableFrom(cls)) {
			Map<String, GeoClass> types = new HashMap<String, GeoClass>(); 
			try {
				for(LayerMetadata layer : gisLogic.list()) {
					if(layer.getFullName() != null) {
						String[] parts = layer.getFullName().split("_");
						if(parts.length > 2) {
							String typeName = parts[1];
							GeoClass geoClass = types.get(typeName);
							if(geoClass == null) {
								geoClass = new GeoClass(typeName);
								types.put(typeName, geoClass);
							}
							geoClass.put(layer.getName(), layer);
						}
					}					
				}
			} catch (Exception e) {
				throw new Error(e);
			}
			return types.values();
		}
		else
			return Collections.emptyList();
	}

	@Override
	public QName getTypeQName(Object type) {
		if (type instanceof GeoClass)
			return new QName(getNamespaceURI(), ((GeoClass) type).getName(), getNamespacePrefix());
		else
			return null;
	}

	@Override
	public GeoClass getType(final QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI())) { 
			try {
				GeoClass geoClass = new GeoClass(qname.getLocalPart());
				for(LayerMetadata layer : layerMetadataStore.list(geoClass.getName()))
					geoClass.put(layer.getName(), layer);
				return geoClass.isEmpty() ? null : geoClass;
			} catch (Exception e) {
				throw new Error(e);
			}
		}
		else
			return null;
	}

	@Override
	public boolean serialize(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof GeoCard) {
			GeoCard geoCard = (GeoCard)entry;
			GeoClass type = geoCard.getType();
			QName qName = getTypeQName(type);
			Element xmlElement = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(), getNamespacePrefix() + ":" + qName.getLocalPart());
			for(LayerMetadata layer : type.getLayers()) {
				Geometry value = geoCard.get(layer.getName());
				Element property = xml.getOwnerDocument().createElementNS(getNamespaceURI(), getNamespacePrefix() + ":" + layer.getName());				
				property.setTextContent(value!=null ? value.toString(): null);
				xmlElement.appendChild(property);
			}			
			xml.appendChild(xmlElement);
			serialized = true;
		}	
		return serialized;
	}
	
	@Override	
	public Object deserialize(Node xml) {
		GeoCard value = null;
		GeoClass type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if(type != null) {
			Map<String, String> properties = new HashMap<String, String>();
			for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
				Node item = xml.getChildNodes().item(i);
				if (item instanceof Element) {
					Element child = (Element) item;
					String name = child.getLocalName();
					if (name == null)
						name = child.getTagName();
					properties.put(name, child.getTextContent());
				}
			}
			value = new GeoCard(type);
			for(LayerMetadata layer : type.getLayers()) {
				String geometry = properties.get(layer.getName());
				if(geometry!=null && !geometry.isEmpty()) {
					try {
						value.set(layer.getName(), PGgeometry.geomFromString(geometry));
					} catch (SQLException e) {
						throw new Error(e);
					}
				}
			}
		}
		return value;
	}
	
	private XmlSchemaType getXsd(GeoClass geoClass, Document document, XmlSchema schema) {
		XmlSchemaComplexType type = new XmlSchemaComplexType(schema/*, true*/);
		schema.getItems().add(type);
		type.setName(getTypeQName(geoClass).getLocalPart());
		XmlSchemaSequence sequence = new XmlSchemaSequence();
		for(LayerMetadata layer : geoClass.getLayers()) {
			XmlSchemaElement element = new XmlSchemaElement(/*schema, false*/);
			element.setName(layer.getName());
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);		
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(GEO_DESCRIPTION, layer.getDescription());
			properties.put(GEO_MAPSTYLE, layer.getMapStyle());
			properties.put(GEO_TYPE, layer.getType());
			properties.put(GEO_INDEX, Integer.toString(layer.getIndex()));
			properties.put(GEO_MINIMUMZOOM, Integer.toString(layer.getMinimumZoom()));
			properties.put(GEO_MAXIMUMZOOM, Integer.toString(layer.getMaximumzoom()));
			properties.put(GEO_VISIBILITY, layer.getVisibilityAsString());
			setAnnotations(element, properties, document);
			sequence.getItems().add(element);			
		}
		type.setParticle(sequence);
		return type;
	}
		
	private GeoClass GeoClassFromXsd(XmlSchemaObject schemaObject, XmlSchema schema) throws Exception {
		XmlSchemaType type = null;
		if(schemaObject instanceof XmlSchemaType)
			type = (XmlSchemaType)schemaObject;
		else if(schemaObject instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement)schemaObject;
			type = element.getSchemaType();
			if(type == null) {
				QName typeName = element.getSchemaTypeName();
				type = schema.getTypeByName(typeName);
			}
		}
		GeoClass geoClass = getType(type.getQName());
		if(type != null) {
			if(type instanceof XmlSchemaComplexType) {
				XmlSchemaParticle particle = ((XmlSchemaComplexType)type).getParticle();
				if(particle!=null && particle instanceof XmlSchemaSequence) {
					if(geoClass == null)
						geoClass = new GeoClass(type.getName());
					XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
					//for(XmlSchemaSequenceMember schemaItem : sequence.getItems()) {
					for(int i=0; i<sequence.getItems().getCount(); i++) {
						XmlSchemaObject schemaItem = sequence.getItems().getItem(i);
						if(schemaItem instanceof XmlSchemaElement) {
							XmlSchemaElement element = (XmlSchemaElement)schemaItem;
							Map<String, String> properties = getAnnotations(element);
							LayerMetadata layer = geoClass.get(element.getName());
							if(layer == null) {
								layer = new LayerMetadata();
								layer.setName(element.getName());
								layer.setDescription(properties.get(GEO_DESCRIPTION));
								layer.setMapStyle(properties.get(GEO_MAPSTYLE));
								layer.setType(properties.get(GEO_TYPE));
								if(properties.get(GEO_INDEX) != null)
									layer.setIndex(Integer.parseInt(properties.get(GEO_INDEX)));
								if(properties.get(GEO_MINIMUMZOOM) != null)
									layer.setMinimumZoom(Integer.parseInt(properties.get(GEO_MINIMUMZOOM)));
								if(properties.get(GEO_MAXIMUMZOOM) != null)
									layer.setMaximumzoom(Integer.parseInt(properties.get(GEO_MAXIMUMZOOM)));
								layer.setVisibilityFromString(properties.get(GEO_VISIBILITY));
								layer.setCardBindingFromString(null);
								layer = gisLogic.createGeoAttribute(type.getName(), layer);
							}
							else {
								layer = gisLogic.modifyGeoAttribute(type.getName(), layer.getName(), properties.get(GEO_DESCRIPTION),
										(properties.get(GEO_MINIMUMZOOM) != null) ? Integer.parseInt(properties.get(GEO_MINIMUMZOOM)) : layer.getMinimumZoom(),
										(properties.get(GEO_MAXIMUMZOOM) != null) ? Integer.parseInt(properties.get(GEO_MAXIMUMZOOM)) : layer.getMaximumzoom(),
										properties.get(GEO_MAPSTYLE));
							}
							geoClass.put(layer.getName(), layer);
						}
					}
				}
			}
		}
		return geoClass;
	}
}
