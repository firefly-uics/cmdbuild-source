(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.RowEditWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.RowEdit}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {Object}
		 */
		record: undefined,

		/**
		 * @cfg {CMDBuild.buttons.SaveButton}
		 */
		saveButton: undefined,

		buttonAlign: 'center',
		border: false,
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,
		title: CMDBuild.Translation.row_edit,

		initComponent: function() {
			// Buttons configuration
				this.saveButton = Ext.create('CMDBuild.buttons.SaveButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onRowEditWindowSaveButtonClick');
					}
				});

				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onRowEditWindowAbortButtonClick');
					}
				});
			// END: Buttons configuration

			this.form = Ext.create('Ext.form.Panel', {
				autoScroll: true,
				frame: true,
				border: false
			});

			Ext.apply(this, {
				items: [this.form],
				buttons: [this.saveButton, this.abortButton]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		}
	});

})();