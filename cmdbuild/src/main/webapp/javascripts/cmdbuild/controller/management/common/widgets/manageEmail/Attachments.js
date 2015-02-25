(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Attachments', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
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
			if (CMDBuild.Config.dms.enabled == 'true') { // TODO: use a model for CMDBuild.Config to convert attributes from string
				Ext.apply(this, configObject); // Apply config
_debug('CMDBuild.Config.dms.enabled', CMDBuild.Config);
				this.view.delegate = this;
				this.view.attachmentButtonsContainer.delegate = this;
			} else {
				_error('Alfresco DMS not enabled');
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

				case 'attachmentUpdateList':
					return this.attachmentUpdateList(param);

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
		},

		/**
		 * @param {Array} attachmentNames
		 */
		attachmentUpdateList: function(attachmentNames) {
			if (Ext.isArray(attachmentNames))
				Ext.Array.forEach(attachmentNames, function(item, index, allItems) {
					this.attachmentAddPanel(item);
				}, this);
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
_debug('onAttachmentAddFromDmsButtonClick', this.record);
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
_debug('record', this.record);
_debug('this.view.', this.view);
			params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);

			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.widgets.ManageEmail.attachmentUpload({
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
			CMDBuild.core.proxy.widgets.ManageEmail.attachmentRemove({
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