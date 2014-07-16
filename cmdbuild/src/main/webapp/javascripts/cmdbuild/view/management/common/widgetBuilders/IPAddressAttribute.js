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
		[operator.NET_CONTAINS, "translation.net_contains"],
		[operator.NET_CONTAINED, "translation.net_contained"],
		[operator.NET_CONTAINS, "translation.net_containsorequal"],
		[operator.NET_CONTAINED, "translation.net_containedorequal"],
		[operator.NET_RELATION, "translation.net_relation"]
	];
};
