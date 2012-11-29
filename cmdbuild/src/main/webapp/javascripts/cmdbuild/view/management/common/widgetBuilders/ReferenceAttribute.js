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
 * @return Ext.form.FieldSet
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.getFieldSetForFilter = function(attribute) {
	var fieldId = "_"+CMDBuild.Utils.nextId();
	var attributeCopy = Ext.apply({}, {
		fieldmode: "write", //change the fieldmode because in the filter must write on this field
		name: attribute.name+fieldId
	}, attribute);

	var field = this.buildField(attributeCopy, hideLabel = true, skipSubAttributes = true);
	var query = this.getQueryCombo(attributeCopy); 
	var fieldset = this.buildFieldsetForFilter(fieldId,field,query,attributeCopy.description);
	return fieldset;
};

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildField = function(attribute, hideLabel, skipSubAttributes) {
	var field = this.buildAttributeField(attribute, skipSubAttributes);
	field.hideLabel = hideLabel;
	return this.markAsRequired(field, attribute);
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildCellEditor = function(attribute) {
	return CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false, skipSubFields = true);
};

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildAttributeField = function(attribute, skipSubAttributes) {
	var subFields = [];
	if (!skipSubAttributes) {
		subFields = getSubFields(attribute, display = false);
	}

	return CMDBuild.Management.ReferenceField.build(attribute, subFields);
};

CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new Ext.form.DisplayField ({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.BIG_FIELD_WIDTH,
		fieldLabel: attribute.description,
		submitValue: false,
		name: attribute.name,
		disabled: false
	});

	var subFields = getSubFields(attribute, display = true);

	if (subFields.length > 0) {
		var fieldContainer = {
			xtype : 'container',
			layout : 'hbox',
			items : [
				new CMDBuild.field.CMToggleButtonToShowReferenceAttributes({
					subFields: subFields
				}),
				field
			]
		};

		field.labelWidth -= 20;

		return new Ext.container.Container({
			items: [fieldContainer].concat(subFields)
		});

	} else {
		return field;
	}
};

function getSubFields(attribute, display) {
	var d = _CMCache.getDomainById(attribute.idDomain),
		fields = [];

	if (d) {
		var attrs = d.data.attributes || [];
		for (var i=0, a=null; i<attrs.length; ++i) {
			a = attrs[i];
			if (a.isbasedsp) {
				var conf = Ext.apply({}, a);
				conf.name = "_" + attribute.name + "_" + conf.name;

				var f = CMDBuild.Management.FieldManager.getFieldForAttr(conf, display);
				f.margin = "0 0 0 5";
				if (f) {
					// Mark the sub fields with a flag "cmDomainAttribute" because
					// if a form has one of these fields can know to do a request to
					// populate it. This is needed because the values of the relations attributes
					// are not serialized in the grid data.
					// As an alternative, we can use an event to notify to the controller
					// that the form is ready (has all the fields), so the controller can
					// look for this kind of attributes and make the request if needed
					f.cmDomainAttribute = true;

					fields.push(f);
				}
			}
		}
	}

	return fields;
}