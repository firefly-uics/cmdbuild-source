(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker', {

		requires: [
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail',
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.email}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		selectedClass: undefined,

		/**
		 * @property {Number}
		 */
		selectedCardId: undefined,

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
				case 'onPickerWindowAttachmentGridCheckChange':
					return this.onPickerWindowAttachmentGridCheckChange(param.checked, param.fileName);

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

//		loadAttachments: function() {
//			var params = {};
//			params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedCardId;
//			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.selectedCardId;
//
//			this.view.attachmentGrid.getStore().load({
//				params: params,
//				scope: this,
//				callback: function(records, operation, success) {
//					this.view.state.syncSelection(records);
//				}
//			});
//		},

		/**
		 * @param {Boolean} checked
		 * @param {String} fileName
		 */
		onPickerWindowAttachmentGridCheckChange: function(checked, fileName) {
			if (checked) {
				this.view.state.check(fileName);
			} else {
				this.view.state.uncheck(fileName);
			}
		},

		onPickerWindowCardGridStoreLoad: function() {
			this.view.attachmentGrid.getStore().removeAll();
		},

		/**
		 * @param {Object} record
		 */
		onPickerWindowCardSelected: function(record) {
_debug('onCardGridSelect record', record);
			this.selectedRecord = record;

			this.view.state.setCardId(this.selectedRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID));

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedRecord.get('IdClass'));

			this.view.attachmentGrid.getStore().load({
				params: params,
				scope: this,
				callback: function(records, operation, success) {
					this.view.state.syncSelection(records);
				}
			});
		},

		onPickerWindowClassSelected: function() {
			this.selectedClass = _CMCache.getEntryTypeById(this.view.classComboBox.getValue());

			this.view.state.setClassName(this.selectedClass.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			this.view.cardGrid.updateStoreForClassId(this.selectedClass.get(CMDBuild.core.proxy.CMProxyConstants.ID));
		},

		onPickerWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onPickerWindowConfirmButtonClick: function() {
			var data = this.view.state.getData();
_debug('AttachPicker data', data);
_debug('AttachPicker this.record', this.record);
			if (!Ext.isEmpty(data)) {
				this.parentDelegate.parentDelegate.view.setLoading(true);
				Ext.Array.forEach(data, function(attachment, i, allAttachments) {
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
					params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = attachment[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
					params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = attachment[CMDBuild.core.proxy.CMProxyConstants.CARD_ID];
					params[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME] = attachment[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME];

					CMDBuild.core.proxy.widgets.ManageEmail.attachmentCopy({
						scope: this,
						params: params,
						failure: function(response, options, decodedResponse) {
							CMDBuild.Msg.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format(CMDBuild.Translation.errors.copyAttachmentFailure, attachment[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME]),
								false
							);
						},
						success: function(response, options, decodedResponse) {
							this.parentDelegate.cmOn('attachmentAddPanel', attachment[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME]);
						}
					});
				}, this);

				this.onPickerWindowAbortButtonClick();
			} else {
				this.onPickerWindowAbortButtonClick();
			}
		}
	});

})();