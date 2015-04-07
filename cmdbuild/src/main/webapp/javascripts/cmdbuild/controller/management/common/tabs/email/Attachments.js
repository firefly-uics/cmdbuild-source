(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.Attachments', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.tabs.email.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'attachmentAddPanel',
			'onAttachmentAddFromDmsButtonClick',
			'onAttachmentChangeFile',
			'onAttachmentRemoveButtonClick'
		],

		/**
		 * @property {Mixed}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.MainContainer}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.tabs.email.EmailWindow} configObject.parentDelegate
		 * @param {Mixed} configObject.record
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.MainContainer} configObject.view
		 */
		constructor: function(configObject) {
			if (CMDBuild.Config.dms.enabled) {
				Ext.apply(this, configObject); // Apply config

				this.view.delegate = this;
				this.view.attachmentButtonsContainer.delegate = this;
			} else {
				_debug('CMDBuild.controller.management.common.tabs.email.Attachments ERROR:  Alfresco DMS not enabled');
			}
		},

		/**
		 * @param {String} fileName
		 */
		attachmentAddPanel: function(fileName) {
			this.view.addPanel(
				Ext.create('CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel', {
					delegate: this,
					fileName: fileName,
					readOnly: this.view.readOnly
				})
			);

			this.parentDelegate.view.setLoading(false);
		},

		/**
		 * @return {Array} attachmentsNames
		 */
		getAttachmentsNames: function() {
			var attachmentsNames = [];

			this.view.attachmentPanelsContainer.items.each(function(item, index, allItems) {
				attachmentsNames.push(item[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME]);
			});

			return attachmentsNames;
		},

		onAttachmentAddFromDmsButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.tabs.email.AttachmentsPicker', {
				parentDelegate: this,
				record: this.record
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
		 * @param {Object} form
		 * @param {Mixed} emailRecord
		 */
		onAttachmentChangeFile: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);

			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.common.tabs.email.Attachment.upload({
				scope: this,
				form: this.view.attachmentButtonsContainer.attachmentUploadForm.getForm(),
				params: params,
				success: function(form, options) {
					this.parentDelegate.view.setLoading(false);

					this.cmfg('attachmentAddPanel', options.result.response);
				}
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.emailWindow.FileAttacchedPanel} attachmentPanel
		 */
		onAttachmentRemoveButtonClick: function(attachmentPanel) {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME] = attachmentPanel[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME];
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);

			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.common.tabs.email.Attachment.remove({
				scope: this,
				params: params,
				success: function(response, options ,decodedResponse) {
					this.parentDelegate.view.setLoading(false);

					this.view.attachmentPanelsContainer.remove(attachmentPanel);
				}
			});
		}
	});

})();