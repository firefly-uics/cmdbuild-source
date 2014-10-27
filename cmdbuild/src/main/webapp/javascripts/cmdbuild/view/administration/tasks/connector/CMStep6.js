(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onBeforeEdit':
					return this.onBeforeEdit(param.fieldName, param.rowData);

				case 'onStepEdit':
					return this.onStepEdit();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildClassCombo: function() {
			var me = this;

			this.view.referenceMappingGrid.columns[0].setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				store: this.parentDelegate.getStoreFilteredClass(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, eOpts) {
						me.buildDomainCombo(this.getValue());
					}
				}
			});
		},

		/**
		 * To setup domain combo editor
		 *
		 * @param {String} className
		 * @param (Boolean) onStepEditExecute
		 */
		buildDomainCombo: function(className, onStepEditExecute) {
			if (!Ext.isEmpty(className)) {
				var me = this;
				var domainStore = _CMCache.getDomainsBy(function(domain) {
					return (
						(domain.get(CMDBuild.core.proxy.CMProxyConstants.NAME_CLASS_1) == className)
						|| (domain.get(CMDBuild.core.proxy.CMProxyConstants.NAME_CLASS_2) == className)
					);
				});

				if (Ext.isEmpty(onStepEditExecute))
					var onStepEditExecute = true;

				if (domainStore.length > 0) {
					this.view.referenceMappingGrid.columns[1].setEditor({
						xtype: 'combo',
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: Ext.create('Ext.data.Store', {
							autoLoad: true,
							fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
							data: domainStore
						}),

						listeners: {
							select: function(combo, records, eOpts) {
								me.cmOn('onStepEdit');
							}
						}
					});
				} else {
					this.view.referenceMappingGrid.columns[1].setEditor({
						xtype: 'combo',
						disabled: true
					});
				}

				if (onStepEditExecute)
					this.onStepEdit();
			}
		},

		// GETters functions
			/**
			 * @return {Array} data
			 */
			getData: function() {
				var data = [];

				if (!Ext.isEmpty(this.view.gridSelectionModel))
					// To validate and filter grid rows
					this.view.referenceMappingGrid.getStore().each(function(record) {
						if (
							!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME))
							&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME))
						) {
							var buffer = {};

							buffer[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);
							buffer[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME);

							data.push(buffer);
						}
					});

				return data;
			},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param {String} fieldName
		 * @param {Object} rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME: {
					if (!Ext.isEmpty(rowData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME])) {
						this.buildDomainCombo(rowData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME]);
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
		},

		/**
		 * Step validation (at least one class/source association)
		 */
		onStepEdit: function() {
			this.view.gridEditorPlugin.completeEdit();
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		frame: true,
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
				minWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,

				plugins: [this.gridEditorPlugin],

				columns: [
					{
						header: tr.className,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
					{
						header: tr.domainName,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME,
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
								tooltip: CMDBuild.Translation.common.buttons.remove,
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
									me.referenceMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.connector.referenceLevel'));
								}
							}
						]
					}
				]
			});

			Ext.apply(this, {
				items: [this.referenceMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			// To populate grid with selected classes
			activate: function(view, eOpts) {
				this.delegate.buildClassCombo();

				// Step validate
				this.delegate.parentDelegate.validateStepGrid(this.referenceMappingGrid.getStore());
			}
		},
	});

})();