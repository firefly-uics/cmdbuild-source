(function() {

	Ext.define('CMDBuild.core.proxy.widgets.ManageEmail', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.widget.ManageEmail'
		],

		singleton: true,

		/**
		 * @param {Ext.form.Basic} form
		 * @param {Object} parameters
		 */
		addAttachmentFromExistingEmail: function(form, parameters) {
			form.submit({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.addAttachmentFromExistingEmail,
				waitMsg: CMDBuild.Translation.uploading_attachment,
				params: parameters.params,
				success: parameters.success
			});
		},

		/**
		 * @param {Ext.form.Basic} form
		 * @param {Object} parameters
		 */
		addAttachmentFromNewEmail: function(form, parameters) {
			form.submit({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.addAttachmentFromNewEmail,
				waitMsg: CMDBuild.Translation.uploading_attachment,
				params: parameters.params,
				success: parameters.success
			});
		},

		/**
		 * @param {Object} parameters
		 */
		copyAttachmentFromCardForExistingEmail: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.copyAttachmentFromCardForExistingEmail,
				method: 'POST',
				params: parameters.params,
				success: parameters.success
			});
		},

		/**
		 * @param {Object} parameters
		 */
		copyAttachmentFromCardForNewEmail: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.copyAttachmentFromCardForNewEmail,
				method: 'POST',
				params: parameters.params,
				success: parameters.success
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.widget.ManageEmail.grid',
				remoteSort: false,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.getEmailList,
					reader: {
						root: 'response',
						type: 'json'
					}
				},
				sorters: {
					property: CMDBuild.core.proxy.CMProxyConstants.STATUS,
					direction: 'ASC'
				},
				groupField: CMDBuild.core.proxy.CMProxyConstants.STATUS
			});
		},

		/**
		 * @param {Object} parameters
		 */
		removeAttachmentFromExistingEmail: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.removeAttachmentFromExistingEmail,
				method: 'POST',
				params: parameters.params,
				success: parameters.success
			});
		},

		/**
		 * @param {Object} parameters
		 */
		removeAttachmentFromNewEmail: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.removeAttachmentFromNewEmail,
				method: 'POST',
				params: parameters.params,
				success: parameters.success
			});
		}
	});

})();