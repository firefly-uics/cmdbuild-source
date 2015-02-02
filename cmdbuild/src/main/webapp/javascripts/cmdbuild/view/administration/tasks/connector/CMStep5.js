(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormConnectorController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.connector.CMStep5}
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

			this.view.attributeLevelMappingGrid.columns[2].setEditor({
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
						me.buildClassAttributesCombo(this.getValue());
					}
				}
			});
		},

		/**
		 * To setup class attribute combo editor
		 *
		 * @param {String} className
		 * @param {Boolean} onStepEditExecute
		 */
		buildClassAttributesCombo: function(className, onStepEditExecute) {
			if (!Ext.isEmpty(className)) {
				var me = this;
				var attributesListStore = [];

				if (Ext.isEmpty(onStepEditExecute))
					var onStepEditExecute = true;

				for (var key in _CMCache.getClasses())
					if (key == _CMCache.getEntryTypeByName(className).get(CMDBuild.core.proxy.CMProxyConstants.ID))
						attributesListStore.push(this.view.classesAttributesMap[key]);

				this.view.attributeLevelMappingGrid.columns[3].setEditor({
					xtype: 'combo',
					displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
					valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
					forceSelection: true,
					editable: false,
					allowBlank: false,

					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
						data: attributesListStore[0]
					}),
					queryMode: 'local',

					listeners: {
						select: function(combo, records, eOpts) {
							me.cmOn('onStepEdit');
						}
					}
				});

				if (onStepEditExecute)
					this.onStepEdit();
			}
		},

		buildSourceCombo: function() {
			var me = this;

			this.view.attributeLevelMappingGrid.columns[0].setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,

				store: this.parentDelegate.getStoreFilteredSource(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, eOpts) {
						me.buildSourceAttributesCombo(this.getValue());
					}
				}
			});
		},

		/**
		 * To setup source attribute combo editor
		 *
		 * @param {String} sourceName
		 * @param {Boolean} onStepEditExecute
		 */
		buildSourceAttributesCombo: function(sourceName, onStepEditExecute) {
			if (!Ext.isEmpty(sourceName)) {
				var me = this;
				var attributesListStore = [];

				if (Ext.isEmpty(onStepEditExecute))
					var onStepEditExecute = true;

// TODO: to finish implementation when stores will be ready
//				for (var key in _CMCache.getClasses())
//					if (key == classId)
//						attributesListStore.push(this.view.classesAttributesMap[key]);

				this.view.attributeLevelMappingGrid.columns[1].setEditor({
					xtype: 'combo',
					displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
					valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,

					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
						data: attributesListStore
					}),

					listeners: {
						select: function(combo, records, eOpts) {
							me.cmOn('onStepEdit');
						}
					}
				});

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

				// To validate and filter grid rows
				this.view.attributeLevelMappingGrid.getStore().each(function(record) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME))
						&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE))
						&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME))
						&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE))
					) {
						var buffer = {};

						buffer[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);
						buffer[CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE);
						buffer[CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME);
						buffer[CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE] = record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE);
						buffer[CMDBuild.core.proxy.CMProxyConstants.IS_KEY] = record.get(CMDBuild.core.proxy.CMProxyConstants.IS_KEY);

						data.push(buffer);
					}
				});

				return data;
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
			this.view.attributeLevelMappingGrid.addBodyCls(cls);
		},

		/**
		 * @param {String} cls
		 */
		markValidTable: function(cls) {
			this.view.attributeLevelMappingGrid.removeBodyCls(cls);
		},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param {String} fieldName
		 * @param {Object} rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE: {
					if (!Ext.isEmpty(rowData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME])) {
						this.buildClassAttributesCombo(rowData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME], false);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.columns[3];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled)
							columnModel.setEditor({
								xtype: 'combo',
								disabled: true
							});
					}
				} break;

				case CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE: {
					if (!Ext.isEmpty(rowData[CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME])) {
						this.buildSourceAttributesCombo(rowData[CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME], false);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.columns[1];
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

// TODO: re-enable when step6 will be implemented
//			if (!this.isEmptyMappingGrid()) {
//				this.setDisabledButtonNext(false);
//			} else {
//				this.setDisabledButtonNext(true);
//			}
		},

		// SETters functions
			/**
			 * @param {Object} data
			 */
			setData: function(data) {
				this.view.attributeLevelMappingGrid.getStore().loadData(data);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.connector.CMStep5Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		attributeLevelMappingGrid: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.classesAttributesMap = _CMCache.getAllAttributesList();
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', this);

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

			this.attributeLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: tr.attributeLevelMapping,
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
							disabled: true
						},
						flex: 1
					},
					{
						header: tr.sourceAttribute,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
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
						header: tr.classAttribute,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
					{
						xtype: 'checkcolumn',
						header: tr.isKey,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_KEY,
						width: 50,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
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
									me.attributeLevelMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelTasks.connector.attributeLevel',
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
									me.attributeLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.connector.attributeLevel'));
								}
							}
						]
					}
				]
			});

			Ext.apply(this, {
				items: [this.attributeLevelMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if grid haven't selected class and setup class and source combo editors
			 *
			 * @param {Object} view
			 * @param {Object} eOpts
			 */
			activate: function(view, eOpts) {
				this.delegate.buildSourceCombo();
				this.delegate.buildClassCombo();

				// Step validate
				this.delegate.parentDelegate.validateStepGrid(this.attributeLevelMappingGrid.getStore());

				Ext.Function.createDelayed(function() { // HACK: to fix problem witch fires show event before changeTab() function
					if (this.delegate.isEmptyMappingGrid())
						this.delegate.setDisabledButtonNext(true);
				}, 1, this)();
			}
		}
	});

})();