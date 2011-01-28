CMDBuild.Management.FieldManagerClass = Ext.extend(Ext.Component, {
	initComponent : function() {
		this.attributesMap = {
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
		};
		CMDBuild.Management.FieldManagerClass.superclass.initComponent.apply(this, arguments);
	},
	
	loadAttributes: function(classId, callback) {
		CMDBuild.Cache.getAttributeList(classId, callback);		
	},
	
	getFieldForAttr: function(attribute, readonly) {
		if (attribute.fieldmode == "hidden")
			return null;
		
		if (readonly || attribute.fieldmode == "read") {
			return CMDBuild.Management.FieldManager.attributesMap[attribute.type].buildReadOnlyField(attribute);
		} else {
			return CMDBuild.Management.FieldManager.attributesMap[attribute.type].buildField(attribute);			
		}
	},

	getHeaderForAttr: function(attribute) {
		if (attribute.fieldmode == "hidden") {
			return undefined;
		} else {
			return CMDBuild.Management.FieldManager.attributesMap[attribute.type].buildGridHeader(attribute);
		}
	},
	
	getDisplayNameForAttr: function(attribute) {
		return CMDBuild.Management.FieldManager.attributesMap[attribute.type].getDisplayNameForAttr(attribute);
	}, 

	getFieldSetForFilter: function(attribute) {
		return CMDBuild.Management.FieldManager.attributesMap[attribute.type].getFieldSetForFilter(attribute);
	}
});
CMDBuild.Management.FieldManager = Ext.apply(new CMDBuild.Management.FieldManagerClass, {});