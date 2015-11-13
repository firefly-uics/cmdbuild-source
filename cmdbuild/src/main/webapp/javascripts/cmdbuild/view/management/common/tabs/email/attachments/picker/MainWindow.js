(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.picker.MainWindow', {
		extend: 'CMDBuild.PopupWindow',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Picker}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.picker.AttachmentGrid}
		 */
		attachmentGrid: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.picker.CardGrid}
		 */
		cardGrid: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		classComboBox: undefined,

		buttonAlign: 'center',
		layout: 'border',
		title: CMDBuild.Translation.chooseAttachmentFromDb,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							this.classComboBox = Ext.create('Ext.form.field.ComboBox', {
								labelWidth: CMDBuild.LABEL_WIDTH,
								fieldLabel: CMDBuild.Translation.selectAClass,
								labelAlign: 'right',
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,

								store: _CMCache.getClassesAndProcessesStore(),
								queryMode: 'local',

								listeners: {
									scope: this,

									change: function(field, newValue, oldValue) {
										this.delegate.cmfg('onPickerWindowClassSelected');
									}
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onPickerWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onPickerWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.cardGrid = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.picker.CardGrid', {
						delegate: this.delegate,
						region: 'center'
					}),
					this.attachmentGrid = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.picker.AttachmentGrid', {
						delegate: this.delegate,
						region: 'south',
						height: '30%'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();