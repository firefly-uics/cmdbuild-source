(function() {

	Ext.define('CMDBuild.model.group.privileges.GridRecord', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'none_privilege', type: 'boolean' },
			{ name: 'read_privilege', type: 'boolean' },
			{ name: 'write_privilege', type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES_PRIVILEGES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		],

		/**
		 * @param {Object} data
		 *
		 * TODO: waiting for server refactor
		 */
		constructor: function(data, id, raw) {
			data = raw;

			// Attribute names adapter
			data[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = raw['privilegedObjectDescription'];
			data[CMDBuild.core.proxy.CMProxyConstants.FILTER] = raw['privilegeFilter'];
			data[CMDBuild.core.proxy.CMProxyConstants.ID] = raw['privilegedObjectId'];
			data[CMDBuild.core.proxy.CMProxyConstants.NAME] = raw['privilegedObjectName'];

			this.callParent(arguments);
		}
	});

})();