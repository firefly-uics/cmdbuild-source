(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.connector.MappingAttribute', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLASS_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.IS_KEY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.SOURCE_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SOURCE_NAME, type: 'string' }
		],

		/**
		 * @returns {Boolean}
		 *
		 * @override
		 */
		isValid: function () {
			return (
				!Ext.isEmpty(Ext.String.trim(this.get(CMDBuild.core.constants.Proxy.CLASS_ATTRIBUTE)))
				&& !Ext.isEmpty(Ext.String.trim(this.get(CMDBuild.core.constants.Proxy.CLASS_NAME)))
				&& !Ext.isEmpty(Ext.String.trim(this.get(CMDBuild.core.constants.Proxy.SOURCE_ATTRIBUTE)))
				&& !Ext.isEmpty(Ext.String.trim(this.get(CMDBuild.core.constants.Proxy.SOURCE_NAME)))
				&& this.callParent(arguments)
			);
		}
	});

})();
