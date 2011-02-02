/**
 * @class CMDBuild.WidgetBuilders.StringAttribute
 * @extends CMDBuild.WidgetBuilders.TextualQueryAttribute
 */
CMDBuild.WidgetBuilders.StringAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.StringAttribute, CMDBuild.WidgetBuilders.TextualQueryAttribute);

CMDBuild.WidgetBuilders.StringAttribute.prototype.MAXWIDTH = 100;
/**
 * @override
 * @param attribute
 * @return object
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildGridHeader = function(attribute) {
	var innerTextWidth = attribute.len * 10;
	
	var headerObject = {
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		fixed: false,
		renderer: CMDBuild.Utils.Format.htmlEntityEncode
	};
	if (innerTextWidth > this.MAXWIDTH) {
		headerObject.width = this.MAXWIDTH;
	}
	return headerObject; 
};
/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.EntityRemoverDisplayField
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new CMDBuild.Management.EntityRemoverDisplayField ({
			fieldLabel: attribute.description,
			submitValue: false,
			name: attribute.name,
		disabled: false
	});
	return field;
},
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField or Ext.form.TextField
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildAttributeField = function(attribute) {
	var field;
	if (attribute.len > this.MAXWIDTH) {
		field = new Ext.form.TextArea({
 			fieldLabel: attribute.description,
    		name: attribute.name,
    		allowBlank: !attribute.isnotnull,
    		width: 420
		});
	} else {
		field = new Ext.form.TextField({
 			fieldLabel: attribute.description,
 			name: attribute.name,
    		maxLength: attribute.len,
    		allowBlank: !attribute.isnotnull,
    		width: (function(length) {
		    			if (length < 33) { //my chose
		    				return (length * 11)+6;
		    			} else {
		    				return 420;
		    			}
		    		})(attribute.len)
		});
	};
	if (this.customVType) {
		field.vtype = this.customVType
	};
	return field;
};