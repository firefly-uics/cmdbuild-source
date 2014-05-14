(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

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
				case 'onStepEdit':
					return this.onStepEdit();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return (Array) data
		 */
		getData: function() {
			var data = [];

			// To validate and filter grid rows
			this.view.classLevelMappingGrid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.SOURCE_NAME))
				) {
					var buffer = [];

					buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.SOURCE_NAME] = record.get(CMDBuild.ServiceProxy.parameter.SOURCE_NAME);

					data.push(buffer);
				}
			});

			return data;
		},

		/**
		 * Function used from next step to get all selected class names
		 *
		 * @return (Array) selectedClassArray
		 */
		getSelectedClassArray: function() {
			var selectedClassArray = [];
			var gridData = this.getData();

			for (key in gridData)
				selectedClassArray.push(gridData[key][CMDBuild.ServiceProxy.parameter.CLASS_NAME]);

			return selectedClassArray;
		},

		/**
		 * Function used from next step to get all selected source names
		 *
		 * @return (Array) selectedSourceArray
		 */
		getSelectedSourceArray: function() {
			var selectedSourceArray = [];
			var gridData = this.getData();

			for (key in gridData)
				selectedSourceArray.push(gridData[key][CMDBuild.ServiceProxy.parameter.SOURCE_NAME]);

			return selectedSourceArray;
		},

		isEmptyMappingGrid: function() {
			return CMDBuild.Utils.isEmpty(this.getData());
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

		setDisabledButtonNext: function(state) {
			this.parentDelegate.setDisabledButtonNext(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', this);

			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			});

			this.classLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: tr.classLevelMapping,
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				plugins: this.gridEditorPlugin,

				columns: [
					{
						header: tr.sourceName,
						dataIndex: CMDBuild.ServiceProxy.parameter.SOURCE_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.NAME,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
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
						header: CMDBuild.Translation.className,
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
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
					model: 'CMDBuild.model.CMModelTasks.connector.classLevel',
					data: []
				}),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.classLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.connector.classLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.classLevelMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			// Disable next button only if grid haven't selected class
			show: function(view, eOpts) {
				var me = this;

//				Ext.Function.createDelayed(function() { // HACK: to fix problem which fires show event before changeTab() function
//					if (me.delegate.isEmptyMappingGrid())
//						me.delegate.setDisabledButtonNext(true);
//				}, 1)();
			}
		}
	});

})();