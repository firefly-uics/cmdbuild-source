(function() {

	/**
	 * Build model from class attributes
	 */
	Ext.define('CMDBuild.model.common.attributes.DynamicModelFromAttributes', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [],

		/**
		 * @param {Array} fieldsDefinitions
		 */
		constructor: function(fieldsDefinitions) {
			var fieldsForModel = [];

			if (Ext.isArray(fieldsDefinitions)) {
				Ext.Array.forEach(fieldsDefinitions, function(field, i, allFields) {
					switch (field.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
						case 'BOOLEAN': {
							fieldsForModel.push({ name: field.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'boolean' });
						} break;

						case 'DATE': {
							fieldsForModel.push({ name: field.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'date' });
						} break;

						case 'DECIMAL':
						case 'DOUBLE': {
							fieldsForModel.push({ name: field.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'float', useNull: true });
						} break;

						case 'INTEGER': {
							fieldsForModel.push({ name: field.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'int', useNull: true });
						} break;

						default: {
							fieldsForModel.push({ name: field.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'string' });
						}
					}
				}, this);

				this.self.setFields(fieldsForModel);
			}

			this.callParent();
		}
	});

})();