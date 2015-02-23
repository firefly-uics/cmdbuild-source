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
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Main}
		 */
		widgetController: undefined,

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
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Main} configObject.widgetController
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer} configObject.view
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view.delegate = this;
			this.view.attachmentButtonsContainer.delegate = this;
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
				case 'addAttachmentPanel':
					return this.addAttachmentPanel(param);

				case 'onAddAttachmentFromDmsButtonClick':
					return this.onAddAttachmentFromDmsButtonClick();

				case 'onRemoveAttachmentButtonClick':
					return this.onRemoveAttachmentButtonClick(param);

				case 'onChangeAttachmentFile':
					return this.onChangeAttachmentFile();

				case 'updateAttachmentList':
					return this.updateAttachmentList(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {String} fileName
		 */
		addAttachmentPanel: function(fileName) {
			this.view.addPanel(
				Ext.create('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowFileAttacchedPanel', { // TODO spostare annidando + rename
					delegate: this,
					fileName: fileName
				})
			);
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

		onAddAttachmentFromDmsButtonClick: function() {
_debug('onAddAttachmentFromDmsButtonClick', this.record);
			Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker', {
				parentDelegate: this,
				record: this.record,
				widgetConf: this.widgetConf,
				widgetController: this.widgetController
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
		 * @param {Object} form
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
		 */
		onChangeAttachmentFile: function() {
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

					this.cmOn('addAttachmentPanel', options.result.response);
				}
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.EmailWindowFileAttacchedPanel} attachmentPanel
		 */
		onRemoveAttachmentButtonClick: function(attachmentPanel) {
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
		},

		/**
		 * @param {Array} attachmentNames
		 */
		updateAttachmentList: function(attachmentNames) {
			if (Ext.isArray(attachmentNames))
				Ext.Array.forEach(attachmentNames, function(item, index, allItems) {
					this.addAttachmentPanel(item);
				}, this);
		}
	});

})();