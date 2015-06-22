(function() {

	Ext.define('CMDBuild.model.common.tabs.email.Email', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.BCC, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.CC, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.FROM, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.REFERENCE, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.STATUS, type: 'string', defaultValue: CMDBuild.core.proxy.Constants.DRAFT },
			{ name: CMDBuild.core.proxy.Constants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEMPORARY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.TO, type: 'auto' }
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
					if (item == CMDBuild.core.proxy.Constants.TEMPLATE) { // Support for template objects
						params[CMDBuild.core.proxy.Constants.TEMPLATE] =
							this.get(CMDBuild.core.proxy.Constants.TEMPLATE)[CMDBuild.core.proxy.Constants.NAME]
						|| this.get(CMDBuild.core.proxy.Constants.TEMPLATE);
					} else {
						params[item] = this.get(item) || null;
					}
				}, this);
			}

			return params;
		}
	});

})();