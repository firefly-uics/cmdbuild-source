(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6Delegate', {
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

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildClassCombo: function() {
			var me = this;

			this.view.referenceMappingGrid.columns[0].setEditor({
				xtype: 'combo',
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				store: this.parentDelegate.getFilteredClassStore(),
				queryMode: 'local',

				// To make sure the filter in the store is not cleared
				triggerAction: 'all',
				lastQuery: '',

				listeners: {
					select: function(combo, records, eOpts) {
						me.buildDomainCombo(records[0].get(CMDBuild.ServiceProxy.parameter.NAME));
					}
				}
			});
		},

		/**
		 * To setup domain combo editor
		 *
		 * @param (String) className
		 */
		buildDomainCombo: function(className) {
			if (!Ext.isEmpty(className)) {
				this.view.referenceMappingGrid.columns[1].setEditor({
					xtype: 'combo',
					displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					allowBlank: false,

					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
						data: _CMCache.getDomainsBy(function(domain) {
							if (
								(domain.get(CMDBuild.ServiceProxy.parameter.NAME_CLASS_1) == className)
								|| (domain.get(CMDBuild.ServiceProxy.parameter.NAME_CLASS_2) == className)
							) {
								return true;
							}

							return false;
						})
					})
				});
			}
		},

		getData: function() {
			var data = [];

			// To validate and filter grid rows
			this.view.referenceMappingGrid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.DOMAIN_NAME))
				) {
					var buffer = [];

					buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.DOMAIN_NAME] = record.get(CMDBuild.ServiceProxy.parameter.DOMAIN_NAME);

					data.push(buffer);
				}
			});

			return data;
		},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param (String) fieldName
		 * @param (Object) rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.ServiceProxy.parameter.DOMAIN_NAME: {
					if (
						(typeof rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME] != 'undefined')
						&& !Ext.isEmpty(rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME])
					) {
						this.buildDomainCombo(rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME]);
					} else {
						var columnModel = this.view.referenceMappingGrid.columns[1];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled)
							columnModel.setEditor({
								xtype: 'combo',
								disabled: true
							});
					}
				} break;
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep6Delegate', this);

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

			this.referenceMappingGrid = Ext.create('Ext.grid.Panel', {
				title: tr.referenceMapping,
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				plugins: this.gridEditorPlugin,

				columns: [
					{
						header: CMDBuild.Translation.className,
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
					{
						header: tr.domainName,
						dataIndex: CMDBuild.ServiceProxy.parameter.DOMAIN_NAME,
						editor: {
							xtype: 'combo',
							disabled: true
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
									me.referenceMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelTasks.connector.referenceLevel',
					data: []
				}),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.referenceMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.connector.referenceLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.referenceMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To populate grid with selected classes
			 */
			show: function(view, eOpts) {
				this.delegate.buildClassCombo();
			}
		},
	});

})();