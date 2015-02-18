(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'auto' }
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

			return params;
		}
	});

	Ext.define('CMDBuild.model.widget.ManageEmail.template', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONDITION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VARIABLES, type: 'auto' }
		],

		constructor: function(data) {
			if (!Ext.isEmpty(data) && !Ext.isEmpty(data[CMDBuild.core.proxy.CMProxyConstants.ID]))
				delete data[CMDBuild.core.proxy.CMProxyConstants.ID];
			
//			if (!Ext.isEmpty(data) && !Ext.isEmpty(data[CMDBuild.core.proxy.CMProxyConstants.CONTENT]))
//				data[CMDBuild.core.proxy.CMProxyConstants.BODY] = data[CMDBuild.core.proxy.CMProxyConstants.CONTENT];

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