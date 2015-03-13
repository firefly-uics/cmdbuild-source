(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker', {

		requires: [
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.manageEmail.Attachment',
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.selection.CheckboxModel}
		 */
		attachmentGridSelectionModel: undefined,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.email}
		 */
		record: undefined,

		/**
		 * @property {Ext.data.Store.Model}
		 */
		selectedCard: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		selectedClass: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.PickerWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Attachments} configObject.parentDelegate
		 * @param {CMDBuild.model.widget.ManageEmail.email} configObject.record
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.MainWindow', {
				delegate: this
			}).show();

			this.attachmentGridSelectionModel = this.view.attachmentGrid.getSelectionModel();
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
				case 'onPickerWindowCardGridStoreLoad':
					return this.onPickerWindowCardGridStoreLoad();

				case 'onPickerWindowCardSelected':
					return this.onPickerWindowCardSelected(param);

				case 'onPickerWindowClassSelected':
					return this.onPickerWindowClassSelected();

				case 'onPickerWindowAbortButtonClick':
					return this.onPickerWindowAbortButtonClick();

				case 'onPickerWindowConfirmButtonClick':
					return this.onPickerWindowConfirmButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onPickerWindowCardGridStoreLoad: function() {
			this.view.attachmentGrid.getStore().removeAll();
		},

		/**
		 * @param {Object} record
		 */
		onPickerWindowCardSelected: function(record) {
			this.selectedCard = record;

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedCard.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedCard.get('IdClass'));

			this.view.attachmentGrid.getStore().load({
				params: params
			});
		},

		onPickerWindowClassSelected: function() {
			this.selectedClass = _CMCache.getEntryTypeById(this.view.classComboBox.getValue());

			this.view.cardGrid.updateStoreForClassId(this.selectedClass.get(CMDBuild.core.proxy.CMProxyConstants.ID));
		},

		onPickerWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onPickerWindowConfirmButtonClick: function() {
			if (this.attachmentGridSelectionModel.hasSelection()) {
				this.parentDelegate.parentDelegate.view.setLoading(true);
				Ext.Array.forEach(this.attachmentGridSelectionModel.getSelection(), function(attachment, i, allAttachments) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
					params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.selectedClass.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
					params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedCard.get(CMDBuild.core.proxy.CMProxyConstants.ID);
					params[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME] = attachment.get('Filename');

					CMDBuild.core.proxy.widgets.manageEmail.Attachment.copy({
						scope: this,
						params: params,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format(CMDBuild.Translation.errors.copyAttachmentFailure, attachment.get('Filename')),
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.parentDelegate.cmOn('attachmentAddPanel', attachment.get('Filename'));
						}
					});
				}, this);
			}

			this.onPickerWindowAbortButtonClick();
		}
	});

})();