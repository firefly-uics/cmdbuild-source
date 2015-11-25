(function() {

	Ext.define('CMDBuild.view.administration.widget.WidgetView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		delegate: undefined,

		/**
		 * @property {Mixed}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.widget.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.widget,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Widget', { delegate: this.delegate })
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.widget.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('Ext.panel.Panel', { // Placeholder for real form
						bodyCls: 'cmgraypanel',
						border: false,
						cls: 'x-panel-body-default-framed cmbordertop',
						frame: false,
						overflowY: 'auto',
						split: true,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onClassTabWidgetPanelShow');
			}
		}
	});

})();