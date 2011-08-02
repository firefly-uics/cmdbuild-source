/**
 * @class CMDBuild.WidgetBuilders.ReferenceAttribute
 * @extends CMDBuild.WidgetBuilders.ComboAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.ReferenceAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ReferenceAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildAttributeField = function(attribute) {
	var subFields = getSubFields(attribute, display = false);
	return CMDBuild.Management.ReferenceField.build(attribute, subFields);
};

CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new Ext.form.DisplayField ({
		labelAlign: "right",
		labelWidth: CMDBuild.CM_LABEL_WIDTH,
		width: CMDBuild.CM_BIG_FIELD_WIDTH,
		fieldLabel: attribute.description,
		submitValue: false,
		name: attribute.name + "_value",
		disabled: false
	});

	var subFields = getSubFields(attribute, display = true);

	if (subFields.length > 0) {
		var items = [field].concat(subFields);

		return new Ext.panel.Panel({
			frame: false,
			border: false,
			bodyCls: "x-panel-body-default-framed",
			items: items
		})
	} else {
		return field;
	}
};


function getSubFields(attribute, display) {
	var d = _CMCache.getDomainById(attribute.idDomain),
		fields = [];

	if (d) {
		Ext.Array.forEach(d.data.attributes, function(a, intex, all) {
			if (a.isbasedsp) {
				var conf = Ext.apply({}, a);
				conf.name = "_" + attribute.name + "_" + conf.name;

				var f = CMDBuild.Management.FieldManager.getFieldForAttr(conf, display);
				// Mark the sub fields with a flag "cmDomainAttribute" because
				// if a form has one of these fields can know to do a request to
				// populate it. This is needed because the values of the relations attributes
				// are not serialized in the grid data.

				// as an alternative, we can use an event to notify to the controller
				// that the form is ready (has all the fields), so the controller can
				// look for this kind of attributes and make the request if needed
				f.cmDomainAttribute = true;

				fields.push(f);
			}
		}, this);
	}

	return fields;
}