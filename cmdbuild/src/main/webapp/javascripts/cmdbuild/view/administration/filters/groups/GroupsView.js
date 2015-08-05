(function() {

	Ext.define('CMDBuild.view.administration.filters.groups.GroupsView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.filters.Groups}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.filters.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filters.groups.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.form = Ext.create('CMDBuild.view.administration.filters.groups.FormPanel', {
				delegate: this.delegate,
				region: 'center'
			});

			this.grid = Ext.create('CMDBuild.view.administration.filters.groups.GridPanel', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Add', {
								text: CMDBuild.Translation.addFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFiltersGroupsAddButtonClick');
								}
							})
						]
					})
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();