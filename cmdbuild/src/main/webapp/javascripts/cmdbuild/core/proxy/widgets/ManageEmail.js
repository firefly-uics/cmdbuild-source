(function() {

	Ext.define('CMDBuild.core.proxy.widgets.ManageEmail', {
		alternateClassName: 'CMDBuild.ServiceProxy.email', // Legacy class name

		statics: {
			/**
			 * @param {Ext.form.Basic} form
			 * @param {Object} conf.params
			 * @param {String} conf.params.uuid
			 * @param {Function} conf.success
			 */
			addAttachmentFromNewEmail: function(form, conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.addAttachmentFromNewEmail;
				conf.waitMsg = CMDBuild.Translation.uploading_attachment;

				form.submit(conf);
			},

			/**
			 * @param {Ext.form.Basic} form
			 * @param {Object} conf.params
			 * @param {Number} conf.params.emailId
			 * @param {Function} conf.success
			 */
			addAttachmentFromExistingEmail: function(form, conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.addAttachmentFromExistingEmail;
				conf.waitMsg = CMDBuild.Translation.uploading_attachment;

				form.submit(conf);
			},

			/**
			 * @param {Object} conf.params
			 * @param {String} conf.params.uuid
			 * @param {String} conf.params.attachments - the encoding of an array like that [{className: '...', cardId: '...', fileName: '...'}, {...}]
			 * @param {Function} conf.params.success
			 */
			copyAttachmentFromCardForNewEmail: function(conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.copyAttachmentFromCardForNewEmail;
				conf.method = 'POST';

				CMDBuild.Ajax.request(conf);
			},

			/**
			 * @param {Object} conf.params
			 * @param {Number} conf.params.id
			 * @param {String} conf.params.attachments - the encoding of an array like that [{className: '...', cardId: '...', fileName: '...'}, {...}]
			 * @param {Function} conf.params.success
			 */
			copyAttachmentFromCardForExistingEmail: function(conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.copyAttachmentFromCardForExistingEmail;
				conf.method = 'POST';

				CMDBuild.Ajax.request(conf);
			},

			/**
			 * @param {Object} conf.params
			 * @param {String} conf.params.uuid
			 * @param {String} conf.params.fileName
			 * @param {Function} conf.success
			 */
			removeAttachmentFromNewEmail: function(conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.removeAttachmentFromNewEmail;
				conf.method = 'POST';

				CMDBuild.Ajax.request(conf);
			},

			/**
			 * @param {Object} conf.params
			 * @param {Number} conf.params.emailId
			 * @param {String} conf.params.fileName
			 * @param {Function} conf.success
			 */
			removeAttachmentFromExistingEmail: function(conf) {
				conf.url = CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.removeAttachmentFromExistingEmail;
				conf.method = 'POST';

				CMDBuild.Ajax.request(conf);
			}
		}
	});

})();