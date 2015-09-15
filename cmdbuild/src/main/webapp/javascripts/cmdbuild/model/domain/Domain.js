(function() {

	Ext.define('CMDBuild.model.domain.Domain', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.ATTRIBUTES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.Constants.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESTINATION_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.DESTINATION_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESTINATION_DISABLED_CLASSES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.Constants.DIRECT_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.INVERSE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.IS_MASTER_DETAIL, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.MASTER_DETAIL_LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ORIGIN_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.ORIGIN_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ORIGIN_DISABLED_CLASSES, type: 'auto', defaultValue: [] },
		],

		/**
		 * @param {Object} data
		 *
		 * @override
		 *
		 * TODO: waiting for server refactor (server side variables rename)
		 */
		constructor: function(data) {
			if (!Ext.isEmpty(data)) {
				data[CMDBuild.core.proxy.Constants.DESTINATION_CLASS_ID] = data['class2id'];
				data[CMDBuild.core.proxy.Constants.DESTINATION_CLASS_NAME] = data['class2'];
				data[CMDBuild.core.proxy.Constants.DESTINATION_DISABLED_CLASSES] = data['disabled2'];
				data[CMDBuild.core.proxy.Constants.DIRECT_DESCRIPTION] = data['descrdir'];
				data[CMDBuild.core.proxy.Constants.ID] = data['idDomain'];
				data[CMDBuild.core.proxy.Constants.INVERSE_DESCRIPTION] = data['descrinv'];
				data[CMDBuild.core.proxy.Constants.IS_MASTER_DETAIL] = data['md'];
				data[CMDBuild.core.proxy.Constants.MASTER_DETAIL_LABEL] = data['md_label'];
				data[CMDBuild.core.proxy.Constants.ORIGIN_CLASS_ID] = data['class1id'];
				data[CMDBuild.core.proxy.Constants.ORIGIN_CLASS_NAME] = data['class1'];
				data[CMDBuild.core.proxy.Constants.ORIGIN_DISABLED_CLASSES] = data['disabled1'];
			}

			this.callParent(arguments);
		}
	});

})();