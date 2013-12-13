package org.cmdbuild.cmdbf.xml;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class XmlRegistry {
	private Collection<XmlNamespace> namespaces;
	
	public XmlRegistry(Collection<XmlNamespace> namespaces) {
		this.namespaces = namespaces;
		for(XmlNamespace namespace : namespaces)
			namespace.setRegistry(this);
	}
	
	private Iterable<XmlNamespace> getNamespaces() {
		return Iterables.filter(namespaces, new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input){
				return input.isEnabled();
			}
		});
	}
	
	public Iterable<String> getSystemIds() {
		return Iterables.transform(getNamespaces(), new Function<XmlNamespace, String>(){
			public String apply(XmlNamespace input) {
				return input.getSystemId();
			}
		});
	}
	
	public XmlNamespace getBySystemId(final String systemId) {
		return Iterables.find(getNamespaces(), new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input){
				return input.getSystemId().equals(systemId);
			}
		});
	}
	
	public XmlNamespace getByNamespaceURI(final String namespace) {
		return Iterables.find(getNamespaces(), new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input){
				return input.getNamespaceURI().equals(namespace);
			}
		});
	}
	
	public XmlSchema getSchema(String systemId) {
		return getBySystemId(systemId).getSchema();
	}

	public boolean updateSchema(final XmlSchema schema) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input) {
				return input.updateSchema(schema);
			}
		});
	}
	
	public Iterable<? extends Object> getTypes(final Class<?> cls) {
		return Iterables.concat(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Iterable<? extends Object>>(){
			public Iterable<? extends Object> apply(XmlNamespace input) {
				return input.getTypes(cls);
			}
		}));
	}
	
	public QName getTypeQName(final Object type) {
		return Iterables.tryFind(Iterables.transform(getNamespaces(), new Function<XmlNamespace, QName>(){
			public QName apply(XmlNamespace input) {
				return input.getTypeQName(type);
			}
		}), Predicates.notNull()).orNull();
	}
	
	public Object getType(final QName qname) {
		return Iterables.tryFind(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>(){
			public Object apply(XmlNamespace input) {
				return input.getType(qname);
			}
		}), Predicates.notNull()).orNull();
	}
	
	public boolean serialize(final Node xml, final Object object) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input) {
				return input.serialize(xml, object);
			}
		});
	}
	
	public Object deserialize(final Node xml) {
		return Iterables.find(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>(){
			public Object apply(XmlNamespace input) {
				return input.deserialize(xml);
			}
		}), Predicates.notNull());
	}
	
	public boolean serializeValue(final Node xml, final Object object) {
		return Iterables.any(getNamespaces(), new Predicate<XmlNamespace>(){
			public boolean apply(XmlNamespace input) {
				return input.serializeValue(xml, object);
			}
		});
	}
	
	public Object deserializeValue(final Node xml, final Object type) {
		return Iterables.find(Iterables.transform(getNamespaces(), new Function<XmlNamespace, Object>(){
			public Object apply(XmlNamespace input) {
				return input.deserializeValue(xml, type);
			}
		}), Predicates.notNull());
	}	
}
