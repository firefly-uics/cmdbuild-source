(function() {

	Ext.define('CMDBuild.model.group.privileges.GridRecord', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'none_privilege', type: 'boolean' },
			{ name: 'read_privilege', type: 'boolean' },
			{ name: 'write_privilege', type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.ATTRIBUTES_PRIVILEGES, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' }
		],

		/**
		 * @param {Object} data
		 *
		 * TODO: waiting for server refactor
		 */
		constructor: function(data, id, raw) {
			data = raw;

			// Attribute names adapter
			data[CMDBuild.core.proxy.Constants.DESCRIPTION] = raw['privilegedObjectDescription'];
			data[CMDBuild.core.proxy.Constants.FILTER] = raw['privilegeFilter'];
			data[CMDBuild.core.proxy.Constants.ID] = raw['privilegedObjectId'];
			data[CMDBuild.core.proxy.Constants.NAME] = raw['privilegedObjectName'];

			this.callParent(arguments);
		}
	});

})();