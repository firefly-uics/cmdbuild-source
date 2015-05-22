(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormConnectorController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.connector.CMStep4}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onBeforeEdit':
					return this.onBeforeEdit(param.fieldName, param.rowData);

				case 'onCheckDelete':
					return this.onCheckDelete(param.checked, param.rowIndex);

				case 'onStepEdit':
					return this.onStepEdit();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildDeletionTypeCombo: function() {
			var me = this;

			this.view.classLevelMappingGrid.columns[5].setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				forceSelection: true,
				editable: false,
				allowBlank: true,

				store: CMDBuild.core.proxy.CMProxyTasks.getDeletionTypes(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, eOpts) {
						me.cmOn('onStepEdit');
					}
				}
			});
		},

		// GETters functions
			/**
			 * @return {Array} data
			 */
			getData: function() {
				var data = [];

				// To validate and filter grid rows
				this.view.classLevelMappingGrid.getStore().each(function(record) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME))
						&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME))
					) {
						var buffer = {};

						buffer[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);
						buffer[CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME);
						buffer[CMDBuild.core.proxy.CMProxyConstants.CREATE] = record.get(CMDBuild.core.proxy.CMProxyConstants.CREATE);
						buffer[CMDBuild.core.proxy.CMProxyConstants.UPDATE] = record.get(CMDBuild.core.proxy.CMProxyConstants.UPDATE);
						buffer[CMDBuild.core.proxy.CMProxyConstants.DELETE] = record.get(CMDBuild.core.proxy.CMProxyConstants.DELETE);

//						// TODO: future implementation
//						if (buffer[CMDBuild.core.proxy.CMProxyConstants.DELETE])
//							buffer[CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE] = record.get(CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE);

						data.push(buffer);
					}
				});

				return data;
			},

			/**
			 * Function used from next step to get all selected class names filtered from duplicates
			 *
			 * @return {Array} selectedClassArray
			 */
			getSelectedClassArray: function() {
				var selectedClassArray = [];
				var gridData = this.getData();

				for (key in gridData)
					if (!Ext.Array.contains(selectedClassArray, gridData[key][CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME]))
						selectedClassArray.push(gridData[key][CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME]);

				return selectedClassArray;
			},

			/**
			 * Function used from next step to get all selected source names filtered from duplicates
			 *
			 * @return {Array} selectedSourceArray
			 */
			getSelectedSourceArray: function() {
				var selectedSourceArray = [];
				var gridData = this.getData();

				for (var key in gridData)
					if (!Ext.Array.contains(selectedSourceArray, gridData[key][CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME]))
						selectedSourceArray.push(gridData[key][CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME]);

				return selectedSourceArray;
			},

		/**
		 * @return {Boolean}
		 */
		isEmptyMappingGrid: function() {
			return Ext.Object.isEmpty(this.getData());
		},

		/**
		 * @param {String} cls
		 */
		markInvalidTable: function(cls) {
			this.view.classLevelMappingGrid.addBodyCls(cls);
		},

		/**
		 * @param {String} cls
		 */
		markValidTable: function(cls) {
			this.view.classLevelMappingGrid.removeBodyCls(cls);
		},

		/**
		 * Resetting deletionType cell value if checkbox is unchecked
		 *
		 * @param {Boolean} checked
		 * @param {Int} rowIndex
		 */
		onCheckDelete: function(checked, rowIndex) {
			if (!checked)
				this.view.classLevelMappingGrid.getStore().getAt(rowIndex).set(CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE, '');
		},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param {String} fieldName
		 * @param {Object} rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE: {
					if (rowData[CMDBuild.core.proxy.CMProxyConstants.DELETE]) {
						this.buildDeletionTypeCombo();
					} else {
						var columnModel = this.view.classLevelMappingGrid.columns[5];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled)
							columnModel.setEditor({
								xtype: 'combo',
								disabled: true
							});
					}
				} break;
			}
		},

		/**
		 * Step validation (at least one class/source association)
		 */
		onStepEdit: function() {
			this.view.gridEditorPlugin.completeEdit();

			if (!this.isEmptyMappingGrid()) {
				this.setDisabledButtonNext(false);
			} else {
				this.setDisabledButtonNext(true);
			}
		},

		// SETters functions
			/**
			 * @param {Object} data
			 */
			setData: function(data) {
				this.view.classLevelMappingGrid.getStore().loadData(data);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.connector.CMStep4Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		classLevelMappingGrid: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', this);

			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1,

				listeners: {
					beforeedit: function(editor, e, eOpts) {
						me.delegate.cmOn('onBeforeEdit', {
							fieldName: e.field,
							rowData: e.record.data
						});
					}
				}
			});

			this.classLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: tr.classLevelMapping,
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',
				minWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,

				plugins: [this.gridEditorPlugin],

				columns: [
					{
						header: tr.sourceName,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
							valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,

							store: CMDBuild.core.proxy.CMProxyTasks.getSourceStore(),

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.cmOn('onStepEdit');
								}
							}
						},
						flex: 1
					},
					{
						header: tr.className,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
							valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
							forceSelection: true,
							editable: false,
							allowBlank: false,

							store: CMDBuild.core.proxy.CMProxyTasks.getClassStore(),
							queryMode: 'local',

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.cmOn('onStepEdit');
								}
							}
						},
						flex: 1
					},
					{
						xtype: 'checkcolumn',
						header: tr.cudActions.createLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CREATE,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					},
					{
						xtype: 'checkcolumn',
						header: tr.cudActions.updateLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.UPDATE,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					},
					{
						xtype: 'checkcolumn',
						header: tr.cudActions.deleteLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DELETE,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						listeners: {
							checkchange: function(checkbox, rowIndex, checked, eOpts) {
								me.delegate.cmOn('onCheckDelete', {
									checked: checked,
									rowIndex: rowIndex
								});
							}
						}
					},
// TODO: future implementation
//					{
//						header: tr.deletionType,
//						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE,
//						editor: {
//							xtype: 'combo',
//							disabled: true
//						},
//						width: 120
//					},
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
								tooltip: CMDBuild.Translation.common.buttons.remove,
								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									me.classLevelMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelTasks.connector.classLevel',
					data: []
				}),

				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							{
								text: CMDBuild.Translation.common.buttons.add,
								iconCls: 'add',
								handler: function() {
									me.classLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.connector.classLevel'));
								}
							}
						]
					}
				]
			});

			Ext.apply(this, {
				items: [this.classLevelMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if grid haven't selected class
			 *
			 * @param {Object} view
			 * @param {Object} eOpts
			 */
			activate: function(view, eOpts) {
				Ext.Function.createDelayed(function() { // HACK: to fix problem which fires show event before changeTab() function
					if (this.delegate.isEmptyMappingGrid())
						this.delegate.setDisabledButtonNext(true);
				}, 1, this)();
			}
		}
	});

})();