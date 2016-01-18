(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.attachments.Picker', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.common.tabs.email.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTabEmailAttachmentPickerWindowAbortButtonClick',
			'onTabEmailAttachmentPickerWindowCardGridStoreLoad',
			'onTabEmailAttachmentPickerWindowCardSelected',
			'onTabEmailAttachmentPickerWindowClassSelected',
			'onTabEmailAttachmentPickerWindowConfirmButtonClick'
		],

		/**
		 * @cfg {Mixed}
		 */
		record: undefined,

		/**
		 * @property {Ext.data.Model}
		 *
		 * @private
		 */
		selectedCard: undefined,

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

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.picker.MainWindow', { delegate: this });

			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onTabEmailAttachmentPickerWindowCardGridStoreLoad: function() {
			this.view.attachmentGrid.getStore().removeAll();
		},

		/**
		 * @param {Object} record
		 */
		onTabEmailAttachmentPickerWindowCardSelected: function(record) {
			this.selectedCardSet({ value: record.getData() });

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.core.proxy.Classes.readAll({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					var selectedClass = Ext.Array.findBy(decodedResponse, function(classObject, i) {
						return this.selectedCardGet(CMDBuild.core.constants.Proxy.CLASS_ID) == classObject[CMDBuild.core.constants.Proxy.ID];
					}, this);

					// Complete selectedCard model with class name property
					this.selectedCardSet({
						propertyName: CMDBuild.core.constants.Proxy.CLASS_NAME,
						value: record.getData()
					});

					params = {};
					params[CMDBuild.core.constants.Proxy.CARD_ID] = this.selectedCardGet(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = selectedClass[CMDBuild.core.constants.Proxy.NAME];

					this.view.attachmentGrid.getStore().load({ params: params });
				}
			});
		},

		onTabEmailAttachmentPickerWindowClassSelected: function() {
			this.view.cardGrid.updateStoreForClassId(this.view.classComboBox.getValue());
		},

		onTabEmailAttachmentPickerWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		// TODO: waiting for refactor (rename)
		onTabEmailAttachmentPickerWindowConfirmButtonClick: function() {
			if (this.view.attachmentGrid.getSelectionModel().hasSelection()) {
				this.cmfg('tabEmailEmailWindowSetLoading', true);
				Ext.Array.forEach(this.view.attachmentGrid.getSelectionModel().getSelection(), function(attachment, i, allAttachments) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selectedCardGet(CMDBuild.core.constants.Proxy.CLASS_NAME);
					params[CMDBuild.core.constants.Proxy.CARD_ID] = this.selectedCardGet(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachment.get('Filename');

					CMDBuild.core.proxy.common.tabs.email.Attachment.copy({
						params: params,
						scope: this,
						failure: function(response, options, decodedResponse) {
							CMDBuild.core.Message.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format(CMDBuild.Translation.errors.copyAttachmentFailure, attachment.get('Filename')),
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.cmfg('tabEmailAttachmentAddPanel', attachment.get('Filename'));
						}
					});
				}, this);
			}

			this.onTabEmailAttachmentPickerWindowAbortButtonClick();
		},

		// SelectedCard property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 *
			 * @private
			 */
			selectedCardGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedCard';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedCardSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.attachments.SelectedCard';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedCard';

					this.propertyManageSet(parameters);
				}
			}
	});

})();