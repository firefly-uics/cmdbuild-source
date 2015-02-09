(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONTENT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY, type: 'boolean', defaultValue: false }, // TODO: Flag to mark records with temporary id
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID, type: 'int' }, // TODO: Temporary template ID to preserve a link from generated email and template of origin
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES, type: 'auto' }
		],

		/**
		 * @return {Boolean}
		 */
		isNew: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == 'New';
		},

		getAttachmentNames: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS) || [];
		}
	});

})();