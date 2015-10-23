(function() {

	Ext.define('CMDBuild.model.common.tabs.email.Template', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONDITION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VARIABLES, type: 'auto' }
		],

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			if (!Ext.isEmpty(data) && !Ext.isEmpty(data[CMDBuild.core.proxy.CMProxyConstants.ID]))
				delete data[CMDBuild.core.proxy.CMProxyConstants.ID];

			this.callParent(arguments);
		},

		/**
		 * Removes ID from data array. This model hasn't ID property but in getData is returned as undefined. Probably a bug.
		 *
		 * @param {Boolean} includeAssociated
		 *
		 * @return {Object}
		 */
		getData: function(includeAssociated) {
			var data = this.callParent(arguments);

			delete data[CMDBuild.core.proxy.CMProxyConstants.ID];

			return data;
		}
	});

})();