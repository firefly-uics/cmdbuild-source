(function() {

	/**
	 * Build grid model from class attributes
	 */
	Ext.define('CMDBuild.model.widget.Grid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [],

		/**
		 * @param {Array} fieldsDefinitions
		 */
		constructor: function(fieldsDefinitions) {
			var fieldsForModel = [];

			if (Ext.isArray(fieldsDefinitions)) {
				Ext.Array.forEach(fieldsDefinitions, function(field, i, allFields) {
					switch (field[CMDBuild.core.proxy.Constants.TYPE]) {
						case 'BOOLEAN': {
							fieldsForModel.push({ name: field[CMDBuild.core.proxy.Constants.NAME], type: 'boolean' });
						} break;

						case 'DATE': {
							fieldsForModel.push({ name: field[CMDBuild.core.proxy.Constants.NAME], type: 'date' });
						} break;

						case 'DECIMAL':
						case 'DOUBLE': {
							fieldsForModel.push({ name: field[CMDBuild.core.proxy.Constants.NAME], type: 'float', useNull: true });
						} break;

						case 'INTEGER': {
							fieldsForModel.push({ name: field[CMDBuild.core.proxy.Constants.NAME], type: 'int', useNull: true });
						} break;

						default: {
							fieldsForModel.push({ name: field[CMDBuild.core.proxy.Constants.NAME], type: 'string' });
						}
					}
				}, this);

				CMDBuild.model.widget.Grid.setFields(fieldsForModel);
			}

			this.callParent();
		}
	});

})();