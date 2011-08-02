(function() {
	var attributesMap = {
		BOOLEAN: new CMDBuild.WidgetBuilders.BooleanAttribute(),
		DECIMAL: new CMDBuild.WidgetBuilders.DecimalAttribute(),
		INTEGER: new CMDBuild.WidgetBuilders.IntegerAttribute(),
		DOUBLE: new CMDBuild.WidgetBuilders.DoubleAttribute(),
		DATE: new CMDBuild.WidgetBuilders.DateAttribute(),
		TIMESTAMP: new CMDBuild.WidgetBuilders.DateTimeAttribute(),
		TIME: new CMDBuild.WidgetBuilders.TimeAttribute(),
		LOOKUP: new CMDBuild.WidgetBuilders.LookupAttribute(),
		REFERENCE: new CMDBuild.WidgetBuilders.ReferenceAttribute(),
		FOREIGNKEY: new CMDBuild.WidgetBuilders.ForeignKeyAttribute(),
		STRING: new CMDBuild.WidgetBuilders.StringAttribute(),
		TEXT: new CMDBuild.WidgetBuilders.TextAttribute(),
		CHAR: new CMDBuild.WidgetBuilders.CharAttribute(),			
		INET: new CMDBuild.WidgetBuilders.IPAddressAttribute()
	}
	
Ext.define("CMDBuild.Management.FieldManager", {
	statics: {
		loadAttributes: function(classId, callback) {
			CMDBuild.Cache.getAttributeList(classId, callback);		
		},
		
		getFieldForAttr: function(attribute, readonly) {
			if (attribute.fieldmode == "hidden")
				return null;
			
			if (readonly || attribute.fieldmode == "read") {
				return attributesMap[attribute.type].buildReadOnlyField(attribute);
			} else {
				return attributesMap[attribute.type].buildField(attribute);			
			}
		},
	
		getHeaderForAttr: function(attribute) {
			if (attribute.fieldmode == "hidden") {
				return undefined;
			} else {
				return attributesMap[attribute.type].buildGridHeader(attribute);
			}
		},
		
		getDisplayNameForAttr: function(attribute) {
			return attributesMap[attribute.type].getDisplayNameForAttr(attribute);
		}, 
	
		getFieldSetForFilter: function(attribute) {
			return attributesMap[attribute.type].getFieldSetForFilter(attribute);
		}
	}
});

})();