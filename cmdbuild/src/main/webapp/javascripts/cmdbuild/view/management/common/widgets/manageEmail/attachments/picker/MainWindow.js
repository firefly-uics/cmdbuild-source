(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.MainWindow', {
		extend: 'CMDBuild.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.AttachmentGrid}
		 */
		attachmentGrid: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.CardGrid}
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
			this.classComboBox = Ext.create('Ext.form.field.ComboBox', {
				labelWidth: CMDBuild.LABEL_WIDTH,
				fieldLabel: CMDBuild.Translation.selectAClass,
				labelAlign: 'right',
				valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				editable: false,

				store: _CMCache.getClassesStore(),
				queryMode: 'local',

				listeners: {
					scope: this,

					change: function(field, newValue, oldValue) {
						this.delegate.cmOn('onPickerWindowClassSelected');
					}
				}
			});

			this.cardGrid = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.CardGrid', {
				delegate: this.delegate,
				region: 'center'
			});

			this.attachmentGrid = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.AttachmentGrid', {
				delegate: this.delegate,
				region: 'south',
				height: '30%'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.classComboBox]
					}
				],
				items: [this.cardGrid, this.attachmentGrid],
				buttons: [
					Ext.create('CMDBuild.buttons.ConfirmButton', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onPickerWindowConfirmButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onPickerWindowAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();