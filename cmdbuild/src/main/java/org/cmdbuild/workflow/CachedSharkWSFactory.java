package org.cmdbuild.workflow;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMapping;

import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BaseDeserializerFactory;
import org.apache.axis.encoding.ser.BaseSerializerFactory;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.enhydra.shark.ejb.client.ws.WAPIEJBEndpointServiceLocator;

/**
 * Extends the standard SharkWSFactory to cache the object instances, so they are created once and then
 * stored for later use.
 */
public class CachedSharkWSFactory extends SharkWSFactory {

	Map<String,Object> ports;
	String url;
	
	@SuppressWarnings("unchecked")
	public CachedSharkWSFactory(String url){
		super(url);
		this.url = url;
		ports = new HashMap();
	}
	
	@Override
	protected Object getEndpointPort(String name) throws Exception {
		if( !ports.containsKey(name) ){
			Object o = null;
			if(name.equals("WAPI")) {
				//get my object..
				System.out.println("WAPI endpoint port requested, url: " + url);
				WAPIEJBEndpointServiceLocator loc = new WAPIEJBEndpointServiceLocator();
				registerCmdbuildTypes(loc);
				o = loc.getWAPIEJBEndpointPort(new URL(url + "/WAPI"));
			} else {
				o = super.getEndpointPort(name);
			}
			ports.put(name, o);
		}
		return ports.get(name);
	}
	
	@SuppressWarnings("unchecked")
	private void registerCmdbuildTypes(WAPIEJBEndpointServiceLocator loc) {
		System.out.println("register custom cmdbuild types for shark webservices...");
		Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
		Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
		
		TypeMapping tm = loc.getTypeMappingRegistry().getTypeMapping("http://schemas.xmlsoap.org/soap/encoding/");

		QName qname = new QName("http://type.workflow.cmdbuild.org",
				"LookupType");
		SerializerFactory sf = BaseSerializerFactory.createFactory(beansf, LookupType.class, qname);
        DeserializerFactory df = BaseDeserializerFactory.createFactory(beandf, LookupType.class, qname);
        tm.register(LookupType.class, qname, sf, df);

		qname = new QName("http://type.workflow.cmdbuild.org",
				"ReferenceType");
		sf = BaseSerializerFactory.createFactory(beansf, ReferenceType.class, qname);
        df = BaseDeserializerFactory.createFactory(beandf, ReferenceType.class, qname);
        tm.register(ReferenceType.class, qname, sf, df);
        
        QName compname = new QName("http://type.workflow.cmdbuild.org","LookupType");
        sf = new ArraySerializerFactory(compname,null);
        df = new ArrayDeserializerFactory();
        qname = new QName("http://ebj.workflow.cmdbuild.org","ArrayOf_tns1_LookupType");
        tm.register(LookupType[].class, qname, sf, df);
        
        compname = new QName("http://type.workflow.cmdbuild.org","ReferenceType");
        sf = new ArraySerializerFactory(compname,null);
        df = new ArrayDeserializerFactory();
        qname = new QName("http://ebj.workflow.cmdbuild.org","ArrayOf_tns1_ReferenceType");
        tm.register(ReferenceType[].class, qname, sf, df);
        
	}
}
