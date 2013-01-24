(function() {
	Ext.define("CMDBuild.core.xml.XMLUtility", {
		statics: {
			xmlDOMFromString: xmlDOMFromString,
			genericExtTreeFromXMLDom: function(xmlDOM) {

				var root = null;
				if (isDocumentNode(xmlDOM)) {
					var childNodes = xmlDOM.childNodes;
					if (childNodes && childNodes.length > 0) {
						root = childNodes[0];
					}
				} else {
					root = xmlDOM;
				}

				if (root) {
					return convertXMLNode(root);
				} else {
					return {};
				}
			},
			serializeToString: serializeToString
		}
	});

	function convertXMLNode(xmlNode) {
		var childNodes = convertChildren(xmlNode.childNodes);
		var folder = childNodes.children.length > 0;
		var text = "";

		if (isTextNode(xmlNode)) {
			text = xmlNode.textContent;
		} else {
			text = xmlNode.nodeName;
		}

		if (childNodes.textContent.length > 0) {
			childNodes.textContent = ": " + childNodes.textContent;
		}

		var node = {
			text: text + childNodes.textContent,
			domNode: xmlNode,
			iconCls: "cm_no_display",
			leaf: !folder
		};

		if (folder) {
			node.children = childNodes.children;
		} else {
			node.iconCls = "cmdbuild-tree-no-icon";
		}

		return node;
	}

	function convertChildren(xmlChildNodes) {
		var children = [];
		var textContent = "";
		if (xmlChildNodes) {
			for (var i=0, l=xmlChildNodes.length; i<l; ++i) {
				var xmlChild = xmlChildNodes[i];
				if (isTextNode(xmlChild)) {
					textContent += (xmlChild.textContent + " ");
				} else {
					children.push(convertXMLNode(xmlChild));
				}
			}
		}

		return {
			children: children,
			textContent: textContent
		};
	}

	function serializeToString(xmlNode) {
		// IE
		if (xmlNode.xml) { 
			return xmlNode.xml;
		} else {
			return (new XMLSerializer()).serializeToString(xmlNode);
		}
	}

	function xmlDOMFromString(xmlString) {
		var dom = null;

		// IE
		if (window.ActiveXObject && typeof window.ActiveXObject != "undefined") {
			var parser = new window.ActiveXObject("Microsoft.XMLDOM");
			if (parser) {
				dom = new window.ActiveXObject("Microsoft.XMLDOM");
				dom.async = "false";
				dom.loadXML(xmlString);
			}
		// The others
		} else if (window.DOMParser 
				&& typeof window.DOMParser != "undefined") {

			var parser = new window.DOMParser();
			dom = parser.parseFromString(xmlString, "text/xml");

		} else {
			throw new Error("No XML parser found");
		}

		return dom;
	}

	function isDocumentNode(node) {
		return node.nodeType == node.DOCUMENT_NODE;
	}

	function isTextNode(node) {
		return node.nodeType == node.TEXT_NODE;
	}
})();