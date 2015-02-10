(function() {

	Ext.define('CMDBuild.core.proxy.widgets.ManageEmail', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.widget.ManageEmail'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.post,
				params: parameters.params,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.widget.ManageEmail.email',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.getStore,
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
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.remove,
				params: parameters.params,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.put,
				params: parameters.params,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		// TODO: future refactor

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