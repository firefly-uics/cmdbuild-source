(function() {
	var _FIELDS = {
		id: "id",
		type: "type",
		label: "label",
		active: "active"
	};

	Ext.define('CMDBuild.model.CMWidgetDefinitionModel', {
		statics: {
			_FIELDS: _FIELDS
		},
		extend: 'Ext.data.Model',
		fields: [
			{name: _FIELDS.id, type: 'string'},
			{name: _FIELDS.type, type: 'string'},
			{name: _FIELDS.label, type: 'string'},
			{name: _FIELDS.active, type: 'boolean', defaultValue: true}
		],
		idProperty: _FIELDS.id
	});

	//REPORT

	var _REPORT_FIELDS = {
		value: "title",
		description: "description",
		id: "id"
	};

	Ext.define('CMDBuild.model.CMReportAsComboItem', {
		statics: {
			_FIELDS: _REPORT_FIELDS
		},
		extend: 'Ext.data.Model',
		fields: [
			{name: _REPORT_FIELDS.value, type: "string"},
			{name: _REPORT_FIELDS.description, type: "string"},
			{name: _REPORT_FIELDS.id, type: "string"}
		]
	});
})();