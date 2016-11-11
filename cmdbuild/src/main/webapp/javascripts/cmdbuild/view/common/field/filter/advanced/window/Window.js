(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.core.window.AbstractCustomModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'percentage',

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.buildFilter,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView}
		 */
		columnPrivileges: {},

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView}
		 */
		fieldFilter: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		rowPrivileges: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		windowTabPanel: undefined,

		border: true,
		bodyCls: 'cmdb-gray-panel-no-padding',
		closeAction: 'hide',
		frame: false,
		overflowY: 'auto',

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
									this.delegate.cmfg('onFieldFilterAdvancedWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onFieldFilterAdvancedWindowAbortButtonClick');
								}
							})
						]
					})
				]
			});

			// Items property evaluation
			if (this.delegate.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				Ext.apply(this, {
					layout: 'fit',

					items: [
						this.windowTabPanel = Ext.create('Ext.tab.Panel', {
							border: false,
							frame: false,

							items: [
								this.rowPrivileges = Ext.create('Ext.panel.Panel', {
									title: CMDBuild.Translation.rowsPrivileges,
									layout: 'border',
									border: false,
									frame: false,

									items: [
										this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
											delegate: this.delegate,
											region: 'north',
											height: '30%',
											split: true
										}),
										this.fieldFilter = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView', {
											cls: 'x-panel-body-default-framed cmdb-border-top',
											isAdministration: true,
											region: 'center'
										})
									]
								}),
								this.columnPrivileges = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView')
							]
						})
					]
				});
			} else {
				Ext.apply(this, {
					layout: 'border',
					overflowY: 'hidden',

					items: [
						this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
							delegate: this.delegate,
							region: 'north',
							height: '30%',
							split: true
						}),
						this.fieldFilter = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView', {
							cls: 'x-panel-body-default-framed cmdb-border-top',
							isAdministration: true,
							region: 'center'
						})
					]
				});
			}

			this.callParent(arguments);
		},

		listeners: {
			beforeshow: function (window, eOpts) {
				return this.delegate.cmfg('onFieldFilterAdvancedWindowBeforeShow');
			}
		}
	});

})();
