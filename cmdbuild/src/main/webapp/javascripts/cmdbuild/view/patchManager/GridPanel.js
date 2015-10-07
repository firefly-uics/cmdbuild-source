(function() {

	Ext.define('CMDBuild.view.patchManager.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.PatchManager'
		],

		/**
		 * @cfg {CMDBuild.controller.patchManager.PatchManager}
		 */
		delegate: undefined,

		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						header: CMDBuild.Translation.category,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CATEGORY,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1,

						renderer: function(value, meta, record, rowIndex, colIndex, store, view){
							if (Ext.isEmpty(value))
								return '<span class="cm-patch-manager-grid-empty-cell">' + CMDBuild.Translation.defaultLabel + '</span>';

							return value;
						}
					}),
					Ext.create('Ext.grid.column.Column', {
						header: CMDBuild.Translation.name,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						width: 100
					}),
					Ext.create('Ext.grid.column.Column', {
						header: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 4
					})
				],
				store: CMDBuild.core.proxy.PatchManager.getStore()
			});

			this.callParent(arguments);
		}
	});

})();
