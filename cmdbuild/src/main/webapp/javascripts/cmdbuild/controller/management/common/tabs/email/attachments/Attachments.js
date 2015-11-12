(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.attachments.Attachments', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
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
			'onAttachmentDownloadButtonClick',
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
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.EmailWindow} configurationObject.parentDelegate
		 * @param {Mixed} configurationObject.record
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.MainContainer} configurationObject.view
		 */
		constructor: function(configurationObject) {
			if (CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED)) {
				this.callParent(arguments);

				this.view.delegate = this;
				this.view.attachmentButtonsContainer.delegate = this;
			} else {
				_error('Alfresco DMS not enabled', this);
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
				attachmentsNames.push(item[CMDBuild.core.constants.Proxy.FILE_NAME]);
			});

			return attachmentsNames;
		},

		onAttachmentAddFromDmsButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.tabs.email.attachments.Picker', {
				parentDelegate: this,
				record: this.record
			});
		},

		onAttachmentChangeFile: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

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
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel} attachmentPanel
		 */
		onAttachmentDownloadButtonClick: function(attachmentPanel) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachmentPanel[CMDBuild.core.constants.Proxy.FILE_NAME];
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

			CMDBuild.core.proxy.common.tabs.email.Attachment.download({
				params: params
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel} attachmentPanel
		 */
		onAttachmentRemoveButtonClick: function(attachmentPanel) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachmentPanel[CMDBuild.core.constants.Proxy.FILE_NAME];
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

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