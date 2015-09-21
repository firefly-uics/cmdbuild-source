(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.common.field.filter.advanced.window.Window'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMFilterAttributes}
		 */
		attributePanel: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.buildFilter,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMFunctions}
		 */
		functionPanel: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMRelations}
		 */
		relationPanel: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		layout: 'border',

		initComponent: function() {
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

								handler: function(button, e) {
									this.delegate.cmfg('onFieldFilterAdvancedWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFieldFilterAdvancedWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						height: '30%',
						split: true
					}),
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						region: 'center',
						bodyCls: 'cmgraypanel-nopadding',
						border: false,
						cls: 'x-panel-body-default-framed cmbordertop',

						items: []
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforeshow: function(window, eOpts) {
				return this.delegate.cmfg('onFieldFilterAdvancedWindowBeforeShow');
			},
			show: function(window, eOpts) {
				return this.delegate.cmfg('onFieldFilterAdvancedWindowShow');
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