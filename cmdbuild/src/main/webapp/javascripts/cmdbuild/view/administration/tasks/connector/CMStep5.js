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
				case 'onBeforeEdit':
					return this.onBeforeEdit(param.fieldName, param.rowData);

				case 'onSelectClassCombo':
					return this.onSelectClassCombo(param);

				case 'onSelectViewCombo':
					return this.onSelectViewCombo(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getSelectedClasses: function() {
			_debug(this.view.attributeLevelMappingGrid);
		},

		/**
		 * Function to update rows stores on beforeEdit event
		 *
		 * @param (String) fieldName
		 * @param (Object) rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE: {
					if (typeof rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME] != 'undefined') {
						this.onSelectClassCombo(
							_CMCache.getEntryTypeByName(
								rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME]
							).get(CMDBuild.ServiceProxy.parameter.ID)
						);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.getView().getHeaderCt().gridDataColumns[3];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled) {
							columnModel.setEditor({
								xtype: 'combo',
								displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
								valueField: CMDBuild.ServiceProxy.parameter.NAME,
								forceSelection: true,
								editable: false,
								allowBlank: false,
								disabled: true
							});
						}
					}
				} break;

				case CMDBuild.ServiceProxy.parameter.VIEW_NAME: {
					if (typeof rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME] != 'undefined') {
						this.onSelectViewCombo(rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME]);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.getView().getHeaderCt().gridDataColumns[1];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled) {
							columnModel.setEditor({
								xtype: 'combo',
								displayField: CMDBuild.ServiceProxy.parameter.NAME,
								valueField: CMDBuild.ServiceProxy.parameter.VALUE,
								forceSelection: true,
								editable: false,
								allowBlank: false,
								disabled: true
							});
						}
					}
				} break;
			}
		},

		/**
		 * To setup class attribute combo store
		 *
		 * @param (Int) classId
		 */
		onSelectClassCombo: function(classId) {
			var columnModel = this.view.attributeLevelMappingGrid.getView().getHeaderCt().gridDataColumns[3];
			var attributesListStore = [];

			for (var key in _CMCache.getClasses()) {
				if (key == classId)
					attributesListStore.push(this.view.classesAttributesMap[key]);
			}

			columnModel.setEditor({
				xtype: 'combo',
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				store: Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: ['name'],
					data: attributesListStore[0]
				})
			});
		},

		/**
		 * To setup view attribute combo store
		 *
		 * @param (String) viewName
		 */
		onSelectViewCombo: function(viewName) {
			var columnModel = this.view.attributeLevelMappingGrid.getView().getHeaderCt().gridDataColumns[1];
			var attributesListStore = [
				{ 'value': 'Function1', 'name': 'Function 1' },
				{ 'value': 'Function2', 'name': 'Function 2' },
				{ 'value': 'Function3', 'name': 'Function 3' }
			];

//			for (var key in _CMCache.getClasses()) {
//				if (key == classId)
//					attributesListStore.push(this.view.classesAttributesMap[key]);
//			}

			columnModel.setEditor({
				xtype: 'combo',
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				store: Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: ['name'],
					data: attributesListStore
				})
			});
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

			this.classesAttributesMap = _CMCache.getAllAttributesList();
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', this);

			this.attributeLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				layuout: 'fit',
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
									me.delegate.cmOn('onSelectViewCombo', records[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
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
							allowBlank: false,
							disabled: true
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
							store: _CMCache.getClassesStore(),
							queryMode: 'local',

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.cmOn('onSelectClassCombo', records[0].get(CMDBuild.ServiceProxy.parameter.ID));
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.classAttribute',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							disabled: true
						},
						flex: 1
					},
//					// TODO: Future implementation
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
							me.delegate.cmOn('onBeforeEdit', {
								fieldName: e.field,
								rowData: e.record.data
							});
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
