(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'string' },
//			{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'auto' }, // CMDBuild.model.EmailTemplates.singleTemplate
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'auto' },
		],

		/**
		 * @return {Boolean}
		 *
		 * TODO: will be deleted in future when i won't need 2 services to save attachments
		 */
		isNew: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
		},

		/**
		 * TODO: refactor
		 */
		getAttachmentNames: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS) || [];
		},

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

			// Eliminate attributes unused from server
			delete params[CMDBuild.core.proxy.CMProxyConstants.STATUS];

			return params;
		}
	});

})();