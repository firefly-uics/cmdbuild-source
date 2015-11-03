(function() {

	Ext.define('CMDBuild.view.common.field.translatable.window.Window', {
		extend: 'CMDBuild.core.PopupWindow',

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		autoHeight: true,
		autoScroll: true,
		border: false,
		buttonAlign: 'center',
		frame: false,
		layout: 'fit',

		title: CMDBuild.Translation.translations,

		initComponent: function() {
			this.form = Ext.create('CMDBuild.view.common.field.translatable.window.FormPanel', {
				delegate: this.delegate
			});

			Ext.apply(this, {
				items: [this.form],
				buttons: [
					Ext.create('CMDBuild.core.buttons.Save', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onTranslatableWindowConfirmButtonClick');
						}
					}),
					Ext.create('CMDBuild.core.buttons.Abort', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onTranslatableWindowAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();