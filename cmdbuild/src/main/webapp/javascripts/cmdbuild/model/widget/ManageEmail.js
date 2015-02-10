(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.widget.ManageEmail.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONTENT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STATUS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: true },
		],

		/**
		 * @return {Boolean}
		 *
		 * TODO: refactor
		 */
		isNew: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == 'New';
		},

		/**
		 * TODO: refactor
		 */
		getAttachmentNames: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS) || [];
		},

		/**
		 * @WIP TODO
		 *
		 * Converts model object to params to use in server calls
		 *
		 * @param {Number} activityId
		 *
		 * @return {Object} params
		 */
		toParams: function(activityId) {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = activityId;
			params[CMDBuild.core.proxy.CMProxyConstants.BCC] = this.get(CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES);
			params[CMDBuild.core.proxy.CMProxyConstants.BODY] = this.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT);
			params[CMDBuild.core.proxy.CMProxyConstants.CC] = this.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES);
			params[CMDBuild.core.proxy.CMProxyConstants.FROM] = this.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS);
			params[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = this.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
			params[CMDBuild.core.proxy.CMProxyConstants.TO] = this.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);

			params['from'] = 'asd@asd.asd'; // TODO: delete dopo aver chiesto a DAVIDE
			params['notifyWith'] = 'asd'; // TODO: delete dopo aver chiesto a DAVIDE
			params['account'] = 'asd'; // TODO: delete dopo aver chiesto a DAVIDE

			return params;
		}
	});

})();