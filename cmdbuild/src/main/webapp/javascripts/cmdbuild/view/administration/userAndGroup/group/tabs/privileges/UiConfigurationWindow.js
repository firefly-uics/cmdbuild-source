(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.tabs.privileges.UiConfigurationWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.tabs.privileges.UiConfiguration}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoHeight: true,
		border: false,
		closeAction: 'hide',
		dimensionsMode: 'absolute',
		frame: false,
		title: CMDBuild.Translation.buttonsToDisable,

		dimensions: {
			height: 'auto',
			width: 200
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
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

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick');
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
								name: CMDBuild.core.constants.Proxy.CREATE,
								fieldLabel: CMDBuild.Translation.add,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.REMOVE,
								fieldLabel: CMDBuild.Translation.remove,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.MODIFY,
								fieldLabel: CMDBuild.Translation.modify,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.CLONE,
								fieldLabel: CMDBuild.Translation.clone,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationShow');
			}
		}
	});

})();
