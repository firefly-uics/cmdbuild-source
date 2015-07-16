(function() {

	Ext.define('CMDBuild.model.common.tabs.email.Template', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.BCC, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.CC, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.CONDITION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FROM, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TO, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VARIABLES, type: 'auto' }
		],

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			if (!Ext.isEmpty(data) && !Ext.isEmpty(data[CMDBuild.core.proxy.Constants.ID]))
				delete data[CMDBuild.core.proxy.Constants.ID];

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

			delete data[CMDBuild.core.proxy.Constants.ID];

			return data;
		}
	});

})();