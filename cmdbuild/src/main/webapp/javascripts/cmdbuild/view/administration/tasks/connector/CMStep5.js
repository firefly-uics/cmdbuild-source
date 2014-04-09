(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', {
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
				case 'onSelectClassCombo':
					return this.onSelectClassCombo(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * To setup attribute combo store
		 *
		 * @param (Object) parameter
		 * @param (Int) parameter.classId
		 * @param (Int) parameter.rowIndex
		 */
		onSelectClassCombo: function(parameter) {

		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', this);

//			this.attributesListStore = [];
//			this.classesAttributesMap = _CMCache.getAttributesList();
//_debug(this.classesAttributesMap);
//			for (key in this.classesAttributesMap) {
//				this.attributesListStore.push({
//					name: _CMCache.getEntryTypeNameById(key),
//					attributes: this.classesAttributesMap[key]
//				});
//			}
//
//			_debug(this.attributesListStore);

			this.attributeLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: 'tr.attributeLevelMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				columns: [
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
							store: CMDBuild.core.proxy.CMProxyTasks.getViewNames(),

							listeners: {
								select: function(combo, records, eOpts) {
									// TODO: update VIEW_ATTRIBUTE_NAME combo store
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.viewAttributeName',
						dataIndex: 'CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE_NAME',
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.NAME,
							valueField: CMDBuild.ServiceProxy.parameter.VALUE,
							forceSelection: true,
							editable: false,
							allowBlank: false
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
									me.delegate.cmOn('onSelectClassCombo', {
										classId: records[0].get(CMDBuild.ServiceProxy.parameter.ID),
										rowIndex: me.attributeLevelMappingGrid.store.indexOf(me.attributeLevelMappingGrid.getSelectionModel().getSelection()[0])
									});
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.classAttributeName',
						dataIndex: CMDBuild.ServiceProxy.parameter.ATTRIBUTES,
						editor: {
							xtype: 'combo',
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							forceSelection: true,
							editable: false,
							allowBlank: false
						},
						flex: 1
					},
// TODO: Future implementation
//					{
//						header: 'tr.function',
//						dataIndex: 'CMDBuild.ServiceProxy.parameter.FUNCTION',
//						editor: {
//							xtype: 'combo',
//							valueField: CMDBuild.ServiceProxy.parameter.VALUE,
//							displayField: CMDBuild.ServiceProxy.parameter.NAME,
//							forceSelection: true,
//							editable: false,
//							allowBlank: false,
//							store: CMDBuild.core.proxy.CMProxyTasks.getFunctionStore()
//						},
//						flex: 1
//					},
					{
						xtype: 'checkcolumn',
						header: 'tr.isKey',
						width: 50,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						dataIndex: 'CMDBuild.ServiceProxy.parameter.IS_KEY'
					},
					{
						xtype: 'actioncolumn',
						width: 50,
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
									me.attributeLevelMappingGrid.store.remove(record);
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
					clicksToEdit: 1,

					listeners: {
						beforeedit: function(editor, e, eOpts) {

							// Beforeedit action to perform onSelectClassCombo, filtering store
							if (e.colIdx == 2) {
								classRowStore.clearFilter();
								classRowStore.filter('country', e.record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME));
							}
						}
					}
				}),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.attributeLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelClassLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.attributeLevelMappingGrid]
			});

			this.callParent(arguments);
		}
	});

})();
