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
//				case 'onRadioClick':
//					return this.onRadioClick(param);

//				case 'onSelectClassCombo':
//					return this.onSelectClassCombo(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
//	,
//
//		onRadioClick: function(className) {
//			if (typeof className != 'undefined')
//				this.view.mainClassName.setValue(className);
//		},
//
//		onSelectClassCombo: function() {
//			var columnModel = this.view.classLevelMappingGrid.getSelectionModel().select(0, true);
//
//			_debug(columnModel);
//		}
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

			this.gridSelectionModel = Ext.create('Ext.selection.CheckboxModel', {
				mode: 'single',
				showHeaderCheckbox: false,
				headerText: 'tr.main',
				headerWidth: 50,
				dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_MAIN_NAME
			});

			this.classLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: 'tr.classLevelMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				selModel: this.gridSelectionModel,

				columns: [
//					{
//						header: 'tr.main',
//						width: 50,
//						align: 'center',
//						sortable: false,
//						hideable: false,
//						menuDisabled: true,
//						fixed: true,
//						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_MAIN_NAME,
//						scope: this,
//						renderer: function(value, metaData, record) {
//							return '<input type="radio" disabled="disabled" name="' + CMDBuild.ServiceProxy.parameter.CLASS_MAIN_NAME + '" onClick="' + this.delegate.cmOn('onRadioClick', record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME)) + '" />';
//						}
//					},
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
							queryMode: 'local'
//								,
//
//							listeners: {
//								select: function(combo, records, eOpts) {
//									me.delegate.cmOn('onSelectClassCombo');
//								}
//							}
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

				plugins: Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 1
				}),

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

			this.mainClassName = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_MAIN_NAME
			});

			Ext.apply(this, {
				items: [this.classLevelMappingGrid, this.mainClassName]
			});

			this.callParent(arguments);
		}
	});

})();