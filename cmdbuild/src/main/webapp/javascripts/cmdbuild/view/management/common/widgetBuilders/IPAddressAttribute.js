/**
 * @class CMDBuild.WidgetBuilders.IPAddressAttribute
 * @extends CMDBuild.WidgetBuilders.DecimalAttribute
 */
	Ext.ns("CMDBuild.WidgetBuilders"); 
	CMDBuild.WidgetBuilders.IPAddressAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.IPAddressAttribute, CMDBuild.WidgetBuilders.DecimalAttribute);
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.MAXWIDTH = CMDBuild.MEDIUM_FIELD_ONLY_WIDTH;
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.customVType = "ipv4";
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.gridRenderer = function(v) {
		return "<div>" + v + "<div>";
	};
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.getQueryOptions = function() {
		var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
		return [
			[operator.EQUAL, translation.equals],
			[operator.NET_CONTAINS, "@@contains"],
			[operator.NET_CONTAINED, "@@contained"],
			[operator.NET_CONTAINSOREQUAL, "@@containsorequal"],
			[operator.NET_CONTAINEDOREQUAL, "@@containedorequal"],
			[operator.NET_RELATION, "@@relation"]
		];
	};
/*ipv6*/
	CMDBuild.WidgetBuilders.IPAddressAttributeV6 = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.IPAddressAttributeV6, CMDBuild.WidgetBuilders.IPAddressAttribute);
	CMDBuild.WidgetBuilders.IPAddressAttributeV6.prototype.customVType = "ipv6";
	CMDBuild.WidgetBuilders.IPAddressAttributeV6.prototype.MAXWIDTH = CMDBuild.MEDIUM_FIELD_ONLY_WIDTH * 2;
