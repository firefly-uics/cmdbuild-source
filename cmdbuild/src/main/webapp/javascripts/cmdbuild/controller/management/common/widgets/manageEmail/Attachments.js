(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Attachments', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.manageEmail.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.email}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow} configObject.parentDelegate
		 * @param {CMDBuild.model.widget.ManageEmail.email} configObject.record
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer} configObject.view
		 */
		constructor: function(configObject) {
			if (CMDBuild.Config.dms.enabled) {
				Ext.apply(this, configObject); // Apply config

				this.view.delegate = this;
				this.view.attachmentButtonsContainer.delegate = this;
			} else {
				_debug('CMDBuild.controller.management.common.widgets.manageEmail.Attachments ERROR:  Alfresco DMS not enabled');
			}
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'attachmentAddPanel':
					return this.attachmentAddPanel(param);

				case 'onAttachmentAddFromDmsButtonClick':
					return this.onAttachmentAddFromDmsButtonClick();

				case 'onAttachmentChangeFile':
					return this.onAttachmentChangeFile();

				case 'onAttachmentRemoveButtonClick':
					return this.onAttachmentRemoveButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {String} fileName
		 */
		attachmentAddPanel: function(fileName) {
			this.view.addPanel(
				Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.FileAttacchedPanel', {
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
			Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker', {
				parentDelegate: this,
				record: this.record
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
		 * @param {Object} form
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
		 */
		onAttachmentChangeFile: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);

			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.widgets.manageEmail.Attachment.upload({
				scope: this,
				form: this.view.attachmentButtonsContainer.attachmentUploadForm.getForm(),
				params: params,
				success: function(form, options) {
					this.parentDelegate.view.setLoading(false);

					this.cmOn('attachmentAddPanel', options.result.response);
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
			CMDBuild.core.proxy.widgets.manageEmail.Attachment.remove({
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