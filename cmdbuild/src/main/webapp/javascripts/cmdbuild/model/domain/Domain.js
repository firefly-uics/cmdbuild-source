(function() {

	Ext.define('CMDBuild.model.domain.Domain', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_DISABLED_CLASSES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.IS_MASTER_DETAIL, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_DISABLED_CLASSES, type: 'auto', defaultValue: [] },
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
				data[CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID] = data['class2id'];
				data[CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME] = data['class2'];
				data[CMDBuild.core.constants.Proxy.DESTINATION_DISABLED_CLASSES] = data['disabled2'];
				data[CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION] = data['descrdir'];
				data[CMDBuild.core.constants.Proxy.ID] = data['idDomain'];
				data[CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION] = data['descrinv'];
				data[CMDBuild.core.constants.Proxy.IS_MASTER_DETAIL] = data['md'];
				data[CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL] = data['md_label'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID] = data['class1id'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME] = data['class1'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_DISABLED_CLASSES] = data['disabled1'];
			}

			this.callParent(arguments);
		}
	});

})();