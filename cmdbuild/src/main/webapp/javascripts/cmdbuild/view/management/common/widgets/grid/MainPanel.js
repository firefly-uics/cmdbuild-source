(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			WIDGET_NAME: '.Grid'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Main}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		importFromCSVButton: undefined,

		autoScroll: true,
		border: false,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			// Buttons configuration
				this.addButton = Ext.create('Ext.button.Button', {
					iconCls: 'add',
					text: CMDBuild.Translation.row_add,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onAddRowButtonClick');
					}
				});

				this.importFromCSVButton = Ext.create('Ext.button.Button', {
					iconCls: 'import',
					text: CMDBuild.Translation.importFromCSV,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onCSVImportButtonClick');
					}
				});
			// END: Buttons configuration

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.grid.GridPanel');

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.addButton, this.importFromCSVButton]
					})
				],
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();