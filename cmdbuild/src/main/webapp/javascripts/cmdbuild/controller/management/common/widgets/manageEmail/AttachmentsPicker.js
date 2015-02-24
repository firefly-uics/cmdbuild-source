(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail'
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

		loadAttachments: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.selectedCardId;
			params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = false;

			this.view.attachmentGrid.getStore().load({
				scope: this,
				params: params,
				callback: function(records, operation, success) {
					this.view.state.syncSelection(records);
				}
			});
		},

		onPickerWindowCardGridStoreLoad: function() {
			this.view.attachmentGrid.getStore().removeAll();
		},

		/**
		 * @param {Object} record
		 */
		onPickerWindowCardSelected: function(record) {
_debug('onCardGridSelect record', record);
			this.selectedCardId = record.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			this.view.state.setCardId(this.selectedCardId);
			this.loadAttachments(
				this.selectedClass.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
				this.selectedCardId
			);
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
_debug('AttachPicker controller', this);
			if (!Ext.isEmpty(data)) {
				var encodedAttachments = Ext.JSON.encode(data);

				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS] = encodedAttachments;

				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.widgets.ManageEmail.attachmentCopy({
					scope: this,
					params: params,
					success: function(response, options, decodedResponse) {
						this.cmOn('attachmentUpdateList', decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS]);

						this.onPickerWindowAbortButtonClick();
					},
					callback: function(options, success, response) {
						CMDBuild.LoadMask.get().hide();
					}
				});
			} else {
				this.onPickerWindowAbortButtonClick();
			}
		}
	});

})();