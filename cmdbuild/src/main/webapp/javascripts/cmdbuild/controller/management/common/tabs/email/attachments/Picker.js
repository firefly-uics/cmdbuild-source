(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.attachments.Picker', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.common.tabs.email.Attachment',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPickerWindowAbortButtonClick',
			'onPickerWindowCardGridStoreLoad',
			'onPickerWindowCardSelected',
			'onPickerWindowClassSelected',
			'onPickerWindowConfirmButtonClick'
		],

		/**
		 * @property {Ext.selection.CheckboxModel}
		 */
		attachmentGridSelectionModel: undefined,

		/**
		 * @property {Mixed}
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
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.picker.MainWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.attachments.Attachments} configurationObject.parentDelegate
		 * @param {Mixed} configurationObject.record
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.picker.MainWindow', {
				delegate: this
			}).show();

			this.attachmentGridSelectionModel = this.view.attachmentGrid.getSelectionModel();
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
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.selectedCard.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedCard.get('IdClass'));

			this.view.attachmentGrid.getStore().load({
				params: params
			});
		},

		onPickerWindowClassSelected: function() {
			this.selectedClass = _CMCache.getEntryTypeById(this.view.classComboBox.getValue());

			this.view.cardGrid.updateStoreForClassId(this.selectedClass.get(CMDBuild.core.constants.Proxy.ID));
		},

		onPickerWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onPickerWindowConfirmButtonClick: function() {
			if (this.attachmentGridSelectionModel.hasSelection()) {
				this.parentDelegate.parentDelegate.view.setLoading(true);
				Ext.Array.forEach(this.attachmentGridSelectionModel.getSelection(), function(attachment, i, allAttachments) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selectedClass.get(CMDBuild.core.constants.Proxy.NAME);
					params[CMDBuild.core.constants.Proxy.CARD_ID] = this.selectedCard.get(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachment.get('Filename');

					CMDBuild.core.proxy.common.tabs.email.Attachment.copy({
						scope: this,
						params: params,
						failure: function(response, options, decodedResponse) {
							CMDBuild.core.Message.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format(CMDBuild.Translation.errors.copyAttachmentFailure, attachment.get('Filename')),
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.parentDelegate.cmfg('attachmentAddPanel', attachment.get('Filename'));
						}
					});
				}, this);
			}

			this.onPickerWindowAbortButtonClick();
		}
	});

})();