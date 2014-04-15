(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.model.CMModelClassLevel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VALUE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', this);

			this.classLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: 'tr.classLevelMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				columns: [
					{
						header: 'tr.main',
						width: 50,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						dataIndex: 'CMDBuild.ServiceProxy.parameter.MAIN',
						renderer: function(value, metaData, record) {
							return '<input type="radio" name="main" value="' + record.get(CMDBuild.ServiceProxy.parameter.NAME) + '" />';
						}
					},
					{
						header: 'tr.viewName',
						dataIndex: CMDBuild.ServiceProxy.parameter.VIEW_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.NAME,
							valueField: CMDBuild.ServiceProxy.parameter.VALUE,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: CMDBuild.core.proxy.CMProxyTasks.getViewNames()
						},
						flex: 1
					},
					{
						header: 'tr.className',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
							queryMode: 'local',

							listeners: {
								select: function(combo, records, eOpts) {
									// TODO: set radio button value
//									me.delegate.cmOn('onSelectAttributeCombo', me.store.indexOf(me.delegate.gridField.getSelectionModel().getSelection()[0]));
								}
							}
						},
						flex: 1
					},
					{
						xtype: 'actioncolumn',
						width: 30,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							{
								icon: 'images/icons/cross.png',
								tooltip: CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove,
								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									me.classLevelMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelClassLevel',
					data: []
				}),

				plugins: Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 }),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.classLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelClassLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.classLevelMappingGrid]
			});

			this.callParent(arguments);
		}
	});

})();