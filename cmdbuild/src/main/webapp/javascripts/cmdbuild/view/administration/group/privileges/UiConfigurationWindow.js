(function() {

	Ext.define('CMDBuild.view.administration.group.privileges.UiConfigurationWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.UiConfiguration}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoHeight: true,
		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.buttonsToDisable,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
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
									this.delegate.cmfg('onGroupPrivilegesGridUIConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupPrivilegesGridUIConfigurationAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						frame: true,
						border: false,

						items: [
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.proxy.Constants.CREATE,
								fieldLabel: CMDBuild.Translation.common.buttons.add,
								labelWidth: CMDBuild.LABEL_WIDTH,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.proxy.Constants.REMOVE,
								fieldLabel: CMDBuild.Translation.common.buttons.remove,
								labelWidth: CMDBuild.LABEL_WIDTH,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.proxy.Constants.MODIFY,
								fieldLabel: CMDBuild.Translation.common.buttons.modify,
								labelWidth: CMDBuild.LABEL_WIDTH,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.proxy.Constants.CLONE,
								fieldLabel: CMDBuild.Translation.common.buttons.clone,
								labelWidth: CMDBuild.LABEL_WIDTH,
								inputValue: true,
								uncheckedValue: false
							})
						]
					})
				]
			});

			this.callParent(arguments);

			// Custom window width
			this.width = 200;
		},

		listeners: {
			show: function(window, eOpts) {
				this.delegate.cmfg('onGroupPrivilegesGridUIConfigurationShow');
			}
		},

		/**
		 * Override close action to avoid destroy action. Close is called by Esc button press and using top-right close toolButton.
		 *
		 * @override
		 */
		close: function() {
			this.hide();
		}
	});

})();