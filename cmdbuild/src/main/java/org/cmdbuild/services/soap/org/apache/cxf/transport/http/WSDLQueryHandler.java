/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.cmdbuild.services.soap.org.apache.cxf.transport.http;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.Bus;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.transports.http.StemMatchingQueryHandler;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.ResourceManagerWSDLLocator;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@NoJSR250Annotations
public class WSDLQueryHandler implements StemMatchingQueryHandler {
	private static final Logger LOG = LogUtils.getL7dLogger(WSDLQueryHandler.class, "QueryMessages");
	private Bus bus;

	public WSDLQueryHandler() {
	}

	public WSDLQueryHandler(final Bus b) {
		bus = b;
	}

	@Override
	public String getResponseContentType(final String baseUri, final String ctx) {
		if (baseUri.toLowerCase().contains("?wsdl") || baseUri.toLowerCase().contains("?xsd=")) {
			return "text/xml";
		}
		return null;
	}

	@Override
	public boolean isRecognizedQuery(final String baseUri, final String ctx, final EndpointInfo endpointInfo,
			final boolean contextMatchExact) {
		if (baseUri != null
				&& (baseUri.contains("?") && (baseUri.toLowerCase().contains("wsdl") || baseUri.toLowerCase().contains(
						"xsd=")))) {

			final int idx = baseUri.indexOf("?");
			final Map<String, String> map = UrlUtilities.parseQueryString(baseUri.substring(idx + 1));
			if (map.containsKey("wsdl") || map.containsKey("xsd")) {
				if (contextMatchExact) {
					return endpointInfo.getAddress().contains(ctx);
				} else {
					// contextMatchStrategy will be "stem"
					return endpointInfo.getAddress().contains(UrlUtilities.getStem(baseUri.substring(0, idx)));
				}
			}
		}
		return false;
	}

	@Override
	public void writeResponse(final String baseUri, final String ctxUri, final EndpointInfo endpointInfo,
			final OutputStream os) {
		try {
			final int idx = baseUri.toLowerCase().indexOf("?");
			final Map<String, String> params = UrlUtilities.parseQueryString(baseUri.substring(idx + 1));

			String base;

			if (endpointInfo.getProperty("publishedEndpointUrl") != null) {
				base = String.valueOf(endpointInfo.getProperty("publishedEndpointUrl"));
			} else {
				base = baseUri.substring(0, baseUri.toLowerCase().indexOf("?"));
			}

			String wsdl = params.get("wsdl");
			if (wsdl != null) {
				// Always use the URL decoded version to ensure that we have a
				// canonical representation of the import URL for lookup.
				wsdl = URLDecoder.decode(wsdl, "utf-8");
			}

			String xsd = params.get("xsd");
			if (xsd != null) {
				// Always use the URL decoded version to ensure that we have a
				// canonical representation of the import URL for lookup.
				xsd = URLDecoder.decode(xsd, "utf-8");
			}

			Map<String, Definition> mp = CastUtils.cast((Map) endpointInfo.getService().getProperty(
					WSDLQueryHandler.class.getName()));
			Map<String, SchemaReference> smp = CastUtils.cast((Map) endpointInfo.getService().getProperty(
					WSDLQueryHandler.class.getName() + ".Schemas"));

			if (mp == null) {
				endpointInfo.getService().setProperty(WSDLQueryHandler.class.getName(), new ConcurrentHashMap());
				mp = CastUtils.cast((Map) endpointInfo.getService().getProperty(WSDLQueryHandler.class.getName()));
			}
			if (smp == null) {
				endpointInfo.getService().setProperty(WSDLQueryHandler.class.getName() + ".Schemas",
						new ConcurrentHashMap());
				smp = CastUtils.cast((Map) endpointInfo.getService().getProperty(
						WSDLQueryHandler.class.getName() + ".Schemas"));
			}

			if (!mp.containsKey("")) {
				final Definition def = new ServiceWSDLBuilder(bus, endpointInfo.getService()).build();

				mp.put("", def);
				updateDefinition(def, mp, smp, base, endpointInfo);
			}

			Document doc;
			if (xsd == null) {
				Definition def = mp.get(wsdl);
				if (def == null) {
					final String wsdl2 = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus), wsdl, base);
					if (wsdl2 != null) {
						def = mp.get(wsdl2);
					}
				}
				if (def == null) {
					throw new WSDLQueryException(new Message("WSDL_NOT_FOUND", LOG, wsdl), null);
				}

				synchronized (def) {
					// writing a def is not threadsafe. Sync on it to make sure
					// we don't get any ConcurrentModificationExceptions
					if (endpointInfo.getProperty("publishedEndpointUrl") != null) {
						final String publishingUrl = String.valueOf(endpointInfo.getProperty("publishedEndpointUrl"));
						updatePublishedEndpointUrl(publishingUrl, def, endpointInfo.getName());
					}

					final WSDLWriter wsdlWriter = bus.getExtension(WSDLManager.class).getWSDLFactory().newWSDLWriter();
					def.setExtensionRegistry(bus.getExtension(WSDLManager.class).getExtensionRegistry());
					doc = wsdlWriter.getDocument(def);
				}
			} else {
				SchemaReference si = smp.get(xsd);
				if (si == null) {
					final String xsd2 = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus), xsd, base);
					if (xsd2 != null) {
						si = smp.get(xsd2);
					}
				}
				if (si == null) {
					throw new WSDLQueryException(new Message("SCHEMA_NOT_FOUND", LOG, wsdl), null);
				}

				String uri = si.getReferencedSchema().getDocumentBaseURI();
				uri = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus), uri, si.getReferencedSchema()
						.getDocumentBaseURI());
				if (uri == null) {
					uri = si.getReferencedSchema().getDocumentBaseURI();
				}
				final ResourceManagerWSDLLocator rml = new ResourceManagerWSDLLocator(uri, bus);

				final InputSource src = rml.getBaseInputSource();
				doc = XMLUtils.getParser().parse(src);
			}

			updateDoc(doc, base, mp, smp, endpointInfo);
			String enc = null;
			try {
				enc = doc.getXmlEncoding();
			} catch (final Exception ex) {
				// ignore - not dom level 3
			}
			if (enc == null) {
				enc = "utf-8";
			}

			final XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(os, enc);
			StaxUtils.writeNode(doc, writer, true);
			writer.flush();
		} catch (final WSDLQueryException wex) {
			throw wex;
		} catch (final Exception wex) {
			throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, baseUri), wex);
		}
	}

	protected void updateDoc(final Document doc, final String base, final Map<String, Definition> mp,
			final Map<String, SchemaReference> smp, final EndpointInfo ei) {
		List<Element> elementList = null;

		try {
			elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
					"http://www.w3.org/2001/XMLSchema", "import");
			for (final Element el : elementList) {
				final String sl = el.getAttribute("schemaLocation");
				if (smp.containsKey(URLDecoder.decode(sl, "utf-8"))) {
					el.setAttribute("schemaLocation", base + "?xsd=" + sl.replace(" ", "%20"));
				}
			}

			elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
					"http://www.w3.org/2001/XMLSchema", "include");
			for (final Element el : elementList) {
				final String sl = el.getAttribute("schemaLocation");
				if (smp.containsKey(URLDecoder.decode(sl, "utf-8"))) {
					el.setAttribute("schemaLocation", base + "?xsd=" + sl.replace(" ", "%20"));
				}
			}
			elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
					"http://www.w3.org/2001/XMLSchema", "redefine");
			for (final Element el : elementList) {
				final String sl = el.getAttribute("schemaLocation");
				if (smp.containsKey(URLDecoder.decode(sl, "utf-8"))) {
					el.setAttribute("schemaLocation", base + "?xsd=" + sl.replace(" ", "%20"));
				}
			}
			elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
					"http://schemas.xmlsoap.org/wsdl/", "import");
			for (final Element el : elementList) {
				final String sl = el.getAttribute("location");
				if (mp.containsKey(URLDecoder.decode(sl, "utf-8"))) {
					el.setAttribute("location", base + "?wsdl=" + sl.replace(" ", "%20"));
				}
			}
		} catch (final UnsupportedEncodingException e) {
			throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, base), e);
		}

		final Object rewriteSoapAddress = ei.getProperty("autoRewriteSoapAddress");

		if (rewriteSoapAddress != null && MessageUtils.isTrue(rewriteSoapAddress)) {
			final List<Element> serviceList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
					"http://schemas.xmlsoap.org/wsdl/", "service");
			for (final Element serviceEl : serviceList) {
				final String serviceName = serviceEl.getAttribute("name");
				if (serviceName.equals(ei.getService().getName().getLocalPart())) {
					elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
							"http://schemas.xmlsoap.org/wsdl/", "port");
					for (final Element el : elementList) {
						final String name = el.getAttribute("name");
						if (name.equals(ei.getName().getLocalPart())) {
							rewriteAddress(base, el, "http://schemas.xmlsoap.org/wsdl/soap/");
							rewriteAddress(base, el, "http://schemas.xmlsoap.org/wsdl/soap12/");
						}
					}
				}
			}
		}
		try {
			doc.setXmlStandalone(true);
		} catch (final Exception ex) {
			// likely not DOM level 3
		}
	}

	static String resolveWithCatalogs(final OASISCatalogManager catalogs, final String start, final String base) {
		if (catalogs == null) {
			return null;
		}
		String resolvedSchemaLocation = null;
		try {
			resolvedSchemaLocation = catalogs.resolveSystem(start);
			if (resolvedSchemaLocation == null) {
				resolvedSchemaLocation = catalogs.resolveURI(start);
			}
			if (resolvedSchemaLocation == null) {
				resolvedSchemaLocation = catalogs.resolvePublic(start, base);
			}
		} catch (final Exception ex) {
			// ignore
		}
		return resolvedSchemaLocation;
	}

	protected void updateDefinition(final Definition def, final Map<String, Definition> done,
			final Map<String, SchemaReference> doneSchemas, final String base, final EndpointInfo ei) {
		final OASISCatalogManager catalogs = OASISCatalogManager.getCatalogManager(bus);

		final Collection<List<?>> imports = CastUtils.cast((Collection<?>) def.getImports().values());
		for (final List<?> lst : imports) {
			final List<Import> impLst = CastUtils.cast(lst);
			for (final Import imp : impLst) {

				final String start = imp.getLocationURI();
				String decodedStart = null;
				// Always use the URL decoded version to ensure that we have a
				// canonical representation of the import URL for lookup.
				try {
					decodedStart = URLDecoder.decode(start, "utf-8");
				} catch (final UnsupportedEncodingException e) {
					throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, start), e);
				}

				final String resolvedSchemaLocation = resolveWithCatalogs(catalogs, start, base);

				if (resolvedSchemaLocation == null) {
					try {
						// check to see if it's already in a URL format. If so,
						// leave it.
						new URL(start);
					} catch (final MalformedURLException e) {
						if (done.put(decodedStart, imp.getDefinition()) == null) {
							updateDefinition(imp.getDefinition(), done, doneSchemas, base, ei);
						}
					}
				} else {
					if (done.put(decodedStart, imp.getDefinition()) == null) {
						done.put(resolvedSchemaLocation, imp.getDefinition());
						updateDefinition(imp.getDefinition(), done, doneSchemas, base, ei);
					}
				}
			}
		}

		/*
		 * This doesn't actually work. Setting setSchemaLocationURI on the
		 * import for some reason doesn't actually result in the new URI being
		 * written
		 */
		final Types types = def.getTypes();
		if (types != null) {
			for (final ExtensibilityElement el : CastUtils.cast(types.getExtensibilityElements(),
					ExtensibilityElement.class)) {
				if (el instanceof Schema) {
					final Schema see = (Schema) el;
					updateSchemaImports(see, doneSchemas, base);
				}
			}
		}
	}

	protected void updatePublishedEndpointUrl(final String publishingUrl, final Definition def, final QName name) {
		final Collection<Service> services = CastUtils.cast(def.getAllServices().values());
		for (final Service service : services) {
			final Collection<Port> ports = CastUtils.cast(service.getPorts().values());
			if (ports.isEmpty()) {
				continue;
			}

			if (name == null) {
				setSoapAddressLocationOn(ports.iterator().next(), publishingUrl);
				break; // only update the first port since we don't target any
						// specific port
			} else {
				for (final Port port : ports) {
					if (name.getLocalPart().equals(port.getName())) {
						setSoapAddressLocationOn(port, publishingUrl);
					}
				}
			}
		}
	}

	private void setSoapAddressLocationOn(final Port port, final String url) {
		final List<?> extensions = port.getExtensibilityElements();
		for (final Object extension : extensions) {
			if (extension instanceof SOAP12Address) {
				((SOAP12Address) extension).setLocationURI(url);
			} else if (extension instanceof SOAPAddress) {
				((SOAPAddress) extension).setLocationURI(url);
			}
		}
	}

	protected void updateSchemaImports(final Schema schema, final Map<String, SchemaReference> doneSchemas,
			final String base) {
		final OASISCatalogManager catalogs = OASISCatalogManager.getCatalogManager(bus);
		final Collection<List<?>> imports = CastUtils.cast((Collection<?>) schema.getImports().values());
		for (final List<?> lst : imports) {
			final List<SchemaImport> impLst = CastUtils.cast(lst);
			for (final SchemaImport imp : impLst) {
				final String start = imp.getSchemaLocationURI();

				if (start != null) {
					String decodedStart = null;
					// Always use the URL decoded version to ensure that we have
					// a
					// canonical representation of the import URL for lookup.
					try {
						decodedStart = URLDecoder.decode(start, "utf-8");
					} catch (final UnsupportedEncodingException e) {
						throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, start), e);
					}

					if (!doneSchemas.containsKey(decodedStart)) {
						final String resolvedSchemaLocation = resolveWithCatalogs(catalogs, start, base);
						if (resolvedSchemaLocation == null) {
							try {
								// check to see if it's already in a URL format.
								// If so, leave it.
								new URL(start);
							} catch (final MalformedURLException e) {
								if (doneSchemas.put(decodedStart, imp) == null) {
									updateSchemaImports(imp.getReferencedSchema(), doneSchemas, base);
								}
							}
						} else {
							if (doneSchemas.put(decodedStart, imp) == null) {
								doneSchemas.put(resolvedSchemaLocation, imp);
								updateSchemaImports(imp.getReferencedSchema(), doneSchemas, base);
							}
						}
					}
				}
			}
		}

		final List<SchemaReference> includes = CastUtils.cast(schema.getIncludes());
		for (final SchemaReference included : includes) {
			final String start = included.getSchemaLocationURI();

			if (start != null) {
				String decodedStart = null;
				// Always use the URL decoded version to ensure that we have a
				// canonical representation of the import URL for lookup.
				try {
					decodedStart = URLDecoder.decode(start, "utf-8");
				} catch (final UnsupportedEncodingException e) {
					throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, start), e);
				}

				final String resolvedSchemaLocation = resolveWithCatalogs(catalogs, start, base);
				if (resolvedSchemaLocation == null) {
					if (!doneSchemas.containsKey(decodedStart)) {
						try {
							// check to see if it's aleady in a URL format. If
							// so, leave it.
							new URL(start);
						} catch (final MalformedURLException e) {
							if (doneSchemas.put(decodedStart, included) == null) {
								updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
							}
						}
					}
				} else if (!doneSchemas.containsKey(decodedStart) || !doneSchemas.containsKey(resolvedSchemaLocation)) {
					doneSchemas.put(decodedStart, included);
					doneSchemas.put(resolvedSchemaLocation, included);
					updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
				}
			}
		}
		final List<SchemaReference> redefines = CastUtils.cast(schema.getRedefines());
		for (final SchemaReference included : redefines) {
			final String start = included.getSchemaLocationURI();

			if (start != null) {
				String decodedStart = null;
				// Always use the URL decoded version to ensure that we have a
				// canonical representation of the import URL for lookup.
				try {
					decodedStart = URLDecoder.decode(start, "utf-8");
				} catch (final UnsupportedEncodingException e) {
					throw new WSDLQueryException(new Message("COULD_NOT_PROVIDE_WSDL", LOG, start), e);
				}

				final String resolvedSchemaLocation = resolveWithCatalogs(catalogs, start, base);
				if (resolvedSchemaLocation == null) {
					if (!doneSchemas.containsKey(decodedStart)) {
						try {
							// check to see if it's aleady in a URL format. If
							// so, leave it.
							new URL(start);
						} catch (final MalformedURLException e) {
							if (doneSchemas.put(decodedStart, included) == null) {
								updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
							}
						}
					}
				} else if (!doneSchemas.containsKey(decodedStart) || !doneSchemas.containsKey(resolvedSchemaLocation)) {
					doneSchemas.put(decodedStart, included);
					doneSchemas.put(resolvedSchemaLocation, included);
					updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
				}
			}
		}
	}

	@Override
	public boolean isRecognizedQuery(final String baseUri, final String ctx, final EndpointInfo endpointInfo) {
		return isRecognizedQuery(baseUri, ctx, endpointInfo, false);
	}

	public void setBus(final Bus bus) {
		this.bus = bus;
	}

	private void rewriteAddress(final String base, final Element el, final String soapNS) {
		final List<Element> sadEls = DOMUtils.findAllElementsByTagNameNS(el, soapNS, "address");
		for (final Element soapAddress : sadEls) {
			soapAddress.setAttribute("location", base);
		}
	}

}
