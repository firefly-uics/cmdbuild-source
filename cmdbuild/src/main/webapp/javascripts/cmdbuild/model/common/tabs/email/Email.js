(function() {

	Ext.define('CMDBuild.model.common.tabs.email.Email', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.REFERENCE, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string', defaultValue: CMDBuild.core.proxy.CMProxyConstants.DRAFT },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'auto' }
		],

		/**
		 * Converts model object to params object, used in server calls
		 *
		 * @param {Array} requiredAttributes
		 *
		 * @return {Object} params
		 */
		getAsParams: function(requiredAttributes) {
			var params = {};

			// With no parameters returns all data
			if (Ext.isEmpty(requiredAttributes)) {
				params = this.getData();
			} else {
				// Or returns only required attributes
				Ext.Array.forEach(requiredAttributes, function(item, index, allItems) {
					if (item == CMDBuild.core.proxy.CMProxyConstants.TEMPLATE) { // Support for template objects
						params[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE] =
							this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.NAME]
						|| this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
					} else {
						params[item] = this.get(item) || null;
					}
				}, this);
			}

			return params;
		}
	});

})();