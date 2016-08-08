(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.workflow.management.panel.tree.toolbarTop.Parent', { // TODO: waiting for refactor (rename and structure)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CAPABILITIES, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CHILDREN, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_STARTABLE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IS_SUPER_CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'int', useNull: true }
		],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = Ext.isObject(data) ? data : {};
			data[CMDBuild.core.constants.Proxy.DESCRIPTION] = data[CMDBuild.core.constants.Proxy.TEXT] || data[CMDBuild.core.constants.Proxy.DESCRIPTION];
			data[CMDBuild.core.constants.Proxy.CAPABILITIES] = Ext.decode(data['ui_card_edit_mode']);
			data[CMDBuild.core.constants.Proxy.IS_STARTABLE] = data['startable'] || data[CMDBuild.core.constants.Proxy.IS_STARTABLE];
			data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS] = Ext.isBoolean(data['superclass']) ? data['superclass'] : data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS];

			this.callParent(arguments);
		}
	});

})();
