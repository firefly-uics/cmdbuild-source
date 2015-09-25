(function() {

	Ext.define('CMDBuild.model.group.privileges.GridRecord', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: 'none_privilege', type: 'boolean' },
			{ name: 'read_privilege', type: 'boolean' },
			{ name: 'write_privilege', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTES_PRIVILEGES, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		],

		/**
		 * @param {Object} data
		 *
		 * TODO: waiting for server refactor
		 */
		constructor: function(data, id, raw) {
			data = raw;

			// Attribute names adapter
			data[CMDBuild.core.constants.Proxy.DESCRIPTION] = raw['privilegedObjectDescription'];
			data[CMDBuild.core.constants.Proxy.FILTER] = raw['privilegeFilter'];
			data[CMDBuild.core.constants.Proxy.ID] = raw['privilegedObjectId'];
			data[CMDBuild.core.constants.Proxy.NAME] = raw['privilegedObjectName'];

			this.callParent(arguments);
		}
	});

})();