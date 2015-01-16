(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONTENT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean' }
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