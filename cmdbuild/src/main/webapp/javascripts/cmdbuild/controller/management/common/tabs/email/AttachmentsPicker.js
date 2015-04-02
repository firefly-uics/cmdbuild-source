(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.AttachmentsPicker', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.tabs.email.Attachment',
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Attachments}
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
		 * @property {CMDBuild.model.tabs.Email.email}
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
		 * @param {CMDBuild.controller.management.common.tabs.email.Attachments} configObject.parentDelegate
		 * @param {CMDBuild.model.tabs.Email.email} configObject.record
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

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

					CMDBuild.core.proxy.tabs.email.Attachment.copy({
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
							this.parentDelegate.cmfg('attachmentAddPanel', attachment.get('Filename'));
						}
					});
				}, this);
			}

			this.onPickerWindowAbortButtonClick();
		}
	});

})();