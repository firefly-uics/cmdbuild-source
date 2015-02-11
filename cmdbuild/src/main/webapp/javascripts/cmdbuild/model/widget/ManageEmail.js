(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONTENT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'auto' }, // CMDBuild.model.EmailTemplates.singleTemplate
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES, type: 'auto' },
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
		 * Converts model object to params to use in server calls
		 *
		 * @return {Object} params
		 */
		getAsParams: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT] =
				this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT] || null;
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = this.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID);
			params[CMDBuild.core.proxy.CMProxyConstants.BCC] = this.get(CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES);
			params[CMDBuild.core.proxy.CMProxyConstants.BODY] = this.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT);
			params[CMDBuild.core.proxy.CMProxyConstants.CC] = this.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES);
			params[CMDBuild.core.proxy.CMProxyConstants.FROM] = this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.FROM] || null;
			params[CMDBuild.core.proxy.CMProxyConstants.ID] = this.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = this.get(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX);
			params[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = this.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE] =
				this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)[CMDBuild.core.proxy.CMProxyConstants.NAME] || this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
			params[CMDBuild.core.proxy.CMProxyConstants.TO] = this.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);

			return params;
		},

		/**
		 * @return {Object} params
		 */
		getTemplateAsParams: function() {
			var template = this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE);

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.BCC] = template.get(CMDBuild.core.proxy.CMProxyConstants.BCC);
			params[CMDBuild.core.proxy.CMProxyConstants.BODY] = template.get(CMDBuild.core.proxy.CMProxyConstants.BODY);
			params[CMDBuild.core.proxy.CMProxyConstants.CC] = template.get(CMDBuild.core.proxy.CMProxyConstants.CC);
			params[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT] = template.get(CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT);
			params[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = template.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION);
			params[CMDBuild.core.proxy.CMProxyConstants.FROM] = template.get(CMDBuild.core.proxy.CMProxyConstants.FROM);
			params[CMDBuild.core.proxy.CMProxyConstants.NAME] = template.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
			params[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = template.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = template.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
			params[CMDBuild.core.proxy.CMProxyConstants.TO] = template.get(CMDBuild.core.proxy.CMProxyConstants.TO);
			params[CMDBuild.core.proxy.CMProxyConstants.VARIABLES] = template.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES);

			return params;
		}
	});

})();