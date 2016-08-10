(function () {

	Ext.define('CMDBuild.controller.management.workflow.panel.tree.Tree', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.panel.tree.Tree',

		requires: [
			'CMDBuild.core.constants.Metadata',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.management.workflow.panel.tree.Tree'
		],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter}
		 *
		 * @private
		 */
		appliedFilter: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getView = panelGridAndFormGridGet',
			'onWorkflowTreeAddButtonClick',
			'onWorkflowTreePrintButtonClick',
			'onWorkflowTreeSaveFailure',
			'onWorkflowTreeWokflowSelect = onWorkflowWokflowSelect',
			'workflowTreeActivityOpen',
			'workflowTreeAppliedFilterGet = panelGridAndFormGridAppliedFilterGet',
			'workflowTreeFilterApply = panelGridAndFormGridFilterApply',
			'workflowTreeFilterClear = panelGridAndFormGridFilterClear',
			'workflowTreeRendererTreeColumn',
			'workflowTreeStoreGet = panelGridAndFormGridStoreGet',
			'workflowTreeStoreLoad = panelGridAndFormGridStoreLoad, onWorkflowStatusSelectionChange'
		],

		/**
		 * @property {CMDBuild.controller.common.panel.gridAndForm.panel.common.print.Window}
		 */
		controllerPrintWindow: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.runtimeParameters.RuntimeParameters}
		 */
		controllerRuntimeParameters: undefined,

		/**
		 * @proeprty {CMDBuild.controller.management.workflow.panel.tree.toolbar.Paging}
		 */
		controllerToolbarPaging: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.tree.toolbar.Top}
		 */
		controllerToolbarTop: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.tree.TreePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.panel.tree.TreePanel', { delegate: this });

			// Build sub-controllers
			this.controllerPrintWindow = Ext.create('CMDBuild.controller.common.panel.gridAndForm.panel.common.print.Window', { parentDelegate: this });
			this.controllerRuntimeParameters = Ext.create('CMDBuild.controller.common.field.filter.runtimeParameters.RuntimeParameters', { parentDelegate: this });
			this.controllerToolbarPaging = Ext.create('CMDBuild.controller.management.workflow.panel.tree.toolbar.Paging', {
				parentDelegate: this,
				enableFilterAdvanced: true,
				enableFilterBasic: true,
				enableButtonPrint: true
			});
			this.controllerToolbarTop = Ext.create('CMDBuild.controller.management.workflow.panel.tree.toolbar.Top', { parentDelegate: this });

			// Add docked
			this.view.addDocked(this.controllerToolbarTop.getView(), 'top');
			this.view.addDocked(this.controllerToolbarPaging.getView(), 'bottom');
		},

		/**
		 * @param {Object} header
		 *
		 * @returns {String} value
		 *
		 * @legacy
		 * @private
		 *
		 * FIXME: delete when old FieldManager will be replaced (applyCustomRenderer)
		 */
		addRendererToHeader: function (header) {
			header.renderer = function (value, metadata, record, rowIndex, colIndex, store, view) {
				value = record.get(header.dataIndex);

				if (typeof value == 'undefined' || value == null) {
					return '';
				} else if (typeof value == 'object') {
					/**
					 * Some values (like reference or lookup) are serialized as object {id: "", description:""}.
					 * Here we display the description
					 */
					value = value.description;
				} else if (typeof value == 'boolean') { // Localize the boolean values
					value = value ? Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
				} else if (typeof value == 'string') { // Strip HTML tags from strings in grid
					value = Ext.util.Format.stripTags(value);
				}

				return value;
			};
		},

		/**
		 * Custom renderer to work with "CMDBuild.model.management.workflow.Node" custom get method
		 *
		 * @param {Object} column
		 *
		 * @returns {Object} column
		 *
		 * @private
		 */
		applyCustomRenderer: function (column) {
			return Ext.apply(column, {
				renderer: function (value, metadata, record, rowIndex, colIndex, store, view) {
					return record.get(column.dataIndex);
				}
			});
		},

		/**
		 * @param {CMDBuild.model.management.workflow.Node} node
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		nodeRecursiveAnchestorsExpand: function (node) {
			if (
				Ext.isObject(node) && !Ext.Object.isEmpty(node)
				&& Ext.isFunction(node.bubble)
			) {
				node.bubble(function () {
					this.expand();
				});
			} else {
				_warning('nodeRecursiveAnchestorsExpand(): unmanaged node parameter', this, node);
			}
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Void}
		 */
		onWorkflowTreeAddButtonClick: function (id) {
			this.view.getSelectionModel().deselectAll();
			this.view.collapseAll();
		},

		/**
		 * @param {String} format
		 *
		 * @returns {Void}
		 */
		onWorkflowTreePrintButtonClick: function (format) {
			if (Ext.isString(format) && !Ext.isEmpty(format)) {
				var sorters = this.cmfg('workflowTreeStoreGet').getSorters();
				var visibleColumns = Ext.Array.slice(this.view.query('gridcolumn:not([hidden])'), 1); // Discard expander column
				var visibleColumnNames = [];

				// Build columns dataIndex array
				if (Ext.isArray(visibleColumns) && !Ext.isEmpty(visibleColumns))
					Ext.Array.each(visibleColumns, function (columnObject, i, allColumnObjects) {
						if (
							Ext.isObject(columnObject) && !Ext.Object.isEmpty(columnObject)
							&& !Ext.isEmpty(columnObject.dataIndex)
						) {
							visibleColumnNames.push(columnObject.dataIndex);
						}
					}, this);

				var params = Ext.clone(this.cmfg('workflowTreeStoreGet').getProxy().extraParams);
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(visibleColumnNames);
				params[CMDBuild.core.constants.Proxy.TYPE] = format;

				if (Ext.isArray(sorters) && !Ext.isEmpty(sorters))
					params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sorters);

				this.controllerPrintWindow.cmfg('panelGridAndFormPrintWindowShow', {
					format: format,
					mode: 'view',
					params: params
				});
			} else {
				_error('onWorkflowTreePrintButtonClick(): unmanaged format property', this, format);
			}
		},

		/**
		 * Reload store on save failure
		 *
		 * @returns {Void}
		 */
		onWorkflowTreeSaveFailure: function () {
			this.cmfg('workflowTreeStoreLoad');
		},

		/**
		 * NOTE: store loading is dove by paging toolbar
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 */
		onWorkflowTreeWokflowSelect: function (node) {
			this.view.reconfigure(
				this.cmfg('workflowTreeStoreGet'),
				this.workflowTreeBuildColumns()
			);

			// Forward to sub controllers
			this.controllerToolbarPaging.cmfg('onWorkflowTreeToolbarPagingWokflowSelect', node.get(CMDBuild.core.constants.Proxy.FILTER));
			this.controllerToolbarTop.cmfg('onWorkflowTreeToolbarTopWokflowSelect');
		},

		// Tree selection methods
			/**
			 * Select activity by metadata
			 *
			 * @param {String} medataValue
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectByMetadata: function (medataValue) {
				if (
					Ext.isString(medataValue) && !Ext.isEmpty(medataValue)
					&& !this.view.getSelectionModel().hasSelection()
				) {
					var nodeToSelect = this.view.getStore().getRootNode().findChildBy(function (node) {
						var nodeMetadata = node.get(CMDBuild.core.constants.Proxy.ACTIVITY_METADATA);
						var activitySubsetIdObject = Ext.Array.findBy(nodeMetadata, function (metadata, i, allMetadata) {
							return metadata[CMDBuild.core.constants.Proxy.NAME] == CMDBuild.core.constants.Metadata.getActivitySubsetId();
						}, this);

						if (Ext.isObject(activitySubsetIdObject) && !Ext.Object.isEmpty(activitySubsetIdObject))
							return activitySubsetIdObject[CMDBuild.core.constants.Proxy.VALUE] == parameters.activitySubsetId;

						return false;
					}, this, true);

					if (Ext.isObject(nodeToSelect) && !Ext.Object.isEmpty(nodeToSelect)) {
						this.view.getSelectionModel().select(nodeToSelect);

						this.nodeRecursiveAnchestorsExpand(nodeToSelect);
					}
				}
			},

			/**
			 * @param {Number} position
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectByPosition: function (position) {
				if (
					Ext.isNumber(position) && !Ext.isEmpty(position)
					&& !this.view.getSelectionModel().hasSelection()
				) {
					this.view.getSelectionModel().select(position);

					this.nodeRecursiveAnchestorsExpand(this.view.getSelectionModel().getSelection()[0]);
				}
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			selectFirst: function () {
				if (!this.view.getSelectionModel().hasSelection())
					this.view.getSelectionModel().select(0, true);
			},

		/**
		 * Find an Activity in store and open
		 *
		 * @param {Object} parameters
		 * @param {String} parameters.activitySubsetId
		 * @param {String} parameters.flowStatus
		 * @param {Number} parameters.id
		 *
		 * @returns {Void}
		 */
		workflowTreeActivityOpen: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
_debug('workflowTreeActivityOpen', parameters);
			if (
				!this.cmfg('workflowSelectedWorkflowIsEmpty')
				&& Ext.isString(parameters[CMDBuild.core.constants.Proxy.FLOW_STATUS]) && !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.FLOW_STATUS])
				&& Ext.isNumber(parameters[CMDBuild.core.constants.Proxy.ID]) && !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.ID])
			) {
				var sorters = this.cmfg('workflowTreeStoreGet').getSorters();

				var params = Ext.clone(this.cmfg('workflowTreeStoreGet').getProxy().extraParams); // Take the current store configuration to have the sort and filter
				params[CMDBuild.core.constants.Proxy.CARD_ID] = parameters[CMDBuild.core.constants.Proxy.ID];
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.RETRY_WITHOUT_FILTER] = false;

				if (Ext.isArray(sorters) && !Ext.isEmpty(sorters))
					params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sorters);

				CMDBuild.proxy.management.workflow.panel.tree.Tree.readPosition({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						var position = decodedResponse[CMDBuild.core.constants.Proxy.POSITION];

						if (position >= 0) { // Card found
							// Card is out of current filter so clear filter to select card
							if (decodedResponse['outOfFilter']) {
								this.controllerToolbarTop.cmfg('workflowTreeToolbarTopStatusValueSet', decodedResponse['FlowStatus']);
								this.controllerToolbarPaging.cmfg('workflowTreeToolbarPagingFilterBasicReset');

								this.cmfg('workflowTreeFilterClear', { disableStoreLoad: true });
							}

							this.cmfg('workflowTreeStoreLoad', {
								page: CMDBuild.core.Utils.getPageNumber(position),
								scope: this,
								callback: function (records, operation, success) {
									this.view.getSelectionModel().deselectAll();
_debug('tererer');
									this.selectByMetadata(parameters[CMDBuild.core.constants.Proxy.ACTIVITY_SUBSET_ID]);
									this.selectByPosition(position % CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT));
								}
							});
						} else { // Card not found
							if (parameters[CMDBuild.core.constants.Proxy.FLOW_STATUS] == 'COMPLETED') {
								_CMWFState.setProcessInstance(Ext.create('CMDBuild.model.CMProcessInstance'));
								_CMUIState.onlyGridIfFullScreen();
							} else {
								CMDBuild.core.Message.info(undefined, CMDBuild.Translation.cardNotMatchFilter);
							}

							this.cmfg('workflowTreeStoreLoad', {
								scope: this,
								callback: function (records, operation, success) { // Avoid first row selection and reset form status
									this.view.getSelectionModel().deselectAll();

									this.cmfg('onWorkflowFormReset');
								}
							});
						}
					}
				});
			}
		},

		// AppliedFilter property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowTreeAppliedFilterGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowTreeAppliedFilterReset: function () {
				this.propertyManageReset('appliedFilter');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowTreeAppliedFilterSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Array} columnsDefinition
		 *
		 * @private
		 */
		workflowTreeBuildColumns: function () {
			var columnsDefinition = [
				Ext.create('CMDBuild.view.management.workflow.panel.tree.TreeColumn', {
					scope: this,
					dataIndex: CMDBuild.core.constants.Proxy.ACTIVITY_DESCRIPTION
				})
			];

			if (!this.cmfg('workflowSelectedWorkflowIsEmpty') && !this.cmfg('workflowSelectedWorkflowAttributesIsEmpty')) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				if (this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.IS_SUPER_CLASS))
					columnsDefinition.push({
						dataIndex: CMDBuild.core.constants.Proxy.CLASS_DESCRIPTION,
						text: CMDBuild.Translation.subClass
					});

				Ext.Array.each(this.cmfg('workflowSelectedWorkflowAttributesGet'), function (attributeModel, i, allAttributeModels) {
					if (
						Ext.isObject(attributeModel) && !Ext.Object.isEmpty(attributeModel)
						&& attributeModel.get(CMDBuild.core.constants.Proxy.NAME) != CMDBuild.core.constants.Proxy.CLASS_DESCRIPTION
					) {
						if (fieldManager.isAttributeManaged(attributeModel.get(CMDBuild.core.constants.Proxy.TYPE))) {
							fieldManager.attributeModelSet(attributeModel);
							fieldManager.push(
								columnsDefinition,
								this.applyCustomRenderer(fieldManager.buildColumn())
							);
						} else if (attributeModel.get(CMDBuild.core.constants.Proxy.TYPE) != 'ipaddress') { // FIXME: future implementation - @deprecated - Old field manager
							var column = CMDBuild.Management.FieldManager.getHeaderForAttr(attributeModel.get(CMDBuild.core.constants.Proxy.SOURCE_OBJECT));

							delete column.flex; // Remove flex property by default to be compatible with forceFit property

							if (Ext.isObject(column) && !Ext.Object.isEmpty(column)) {
								this.addRendererToHeader(column);

								fieldManager.push(columnsDefinition, column);
							}
						}
					}
				}, this);
			}

			return columnsDefinition;
		},

		/**
		 * @param {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		workflowTreeFilterApply: function (filter) {
			if (
				Ext.isObject(filter) && !Ext.Object.isEmpty(filter)
				&& Ext.getClassName(filter) == 'CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter'
			) {
				var emptyRuntimeParameters = filter.getEmptyRuntimeParameters();
				var sorters = this.cmfg('workflowTreeStoreGet').getSorters();

				if (Ext.isArray(emptyRuntimeParameters) && !Ext.isEmpty(emptyRuntimeParameters))
					return this.controllerRuntimeParameters.cmfg('fieldFilterRuntimeParametersShow', filter);

				filter.resolveCalculatedParameters();

				this.workflowTreeAppliedFilterSet({ value: filter });

				var params = {};
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(filter.get(CMDBuild.core.constants.Proxy.CONFIGURATION));

				if (Ext.isArray(sorters) && !Ext.isEmpty(sorters))
					params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sorters);

				this.cmfg('workflowTreeStoreLoad', { params: params });
			} else {
				_error('workflowTreeFilterApply(): unmanaged filter object', this, filter);
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.disableStoreLoad
		 *
		 * @returns {Void}
		 */
		workflowTreeFilterClear: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.disableStoreLoad = Ext.isBoolean(parameters.disableStoreLoad) ? parameters.disableStoreLoad : false;

			this.workflowTreeAppliedFilterReset();

			if (!parameters.disableStoreLoad) {
				var params = {};
				var sorters = this.cmfg('workflowTreeStoreGet').getSorters();

				if (Ext.isArray(sorters) && !Ext.isEmpty(sorters))
					params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sorters);

				this.cmfg('workflowTreeStoreLoad', { params: params });
			}
		},

		/**
		 * Apply colspan property to be visually equal to old grid
		 *
		 * @param {Object} parameters
		 * @param {Object} parameters.metadata
		 * @param {CMDBuild.model.management.workflow.Node} parameters.record
		 *
		 * @returns {Void}
		 */
		workflowTreeRendererTreeColumn: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isObject(parameters.metadata) && !Ext.Object.isEmpty(parameters.metadata)
				&& Ext.isObject(parameters.record) && !Ext.Object.isEmpty(parameters.record)
				&& !parameters.record.parentNode.isRoot()
			) {
				parameters.metadata.tdAttr = 'colspan="' + this.view.columns.length + '"' ;
			}
		},

		/**
		 * @returns {Ext.data.TreeStore}
		 */
		workflowTreeStoreGet: function () {
			return this.view.getStore();
		},

		/**
		 * On load action sends by default node parameter witch isn't managed by server
		 *
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Number} parameters.page
		 * @param {Object} parameters.params - additional load custom parameters
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		workflowTreeStoreLoad: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : { page: 1 };
			parameters.page = Ext.isNumber(parameters.page) ? parameters.page : 1;

			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				var params = Ext.isObject(parameters.params) ? parameters.params : {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.STATE] = this.controllerToolbarTop.cmfg('workflowTreeToolbarTopStatusValueGet');

				this.cmfg('workflowTreeStoreGet').getProxy().extraParams = params; // Setup extraParams to works also with sorters and print report
				this.cmfg('workflowTreeStoreGet').loadPage(parameters.page, {
					params: params,
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
					callback: Ext.isFunction(parameters.callback) ? parameters.callback : this.selectFirst
				});
			}
		}
	});

})();
