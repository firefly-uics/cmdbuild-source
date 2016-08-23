(function () {

	Ext.define('CMDBuild.controller.management.workflow.panel.tree.Tree', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.panel.tree.Tree',

		requires: [
			'CMDBuild.controller.management.workflow.Utils',
			'CMDBuild.core.constants.Metadata',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.WorkflowStates',
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
			'workflowTreeApplyStoreEvent',
			'workflowTreeFilterApply = panelGridAndFormGridFilterApply',
			'workflowTreeFilterClear = panelGridAndFormGridFilterClear',
			'workflowTreeRendererTreeColumn',
			'workflowTreeReset',
			'workflowTreeStoreGet = panelGridAndFormGridStoreGet',
			'workflowTreeStoreLoad = panelGridAndFormGridStoreLoad',
			'workflowTreeToolbarTopStatusValueSet -> controllerToolbarTop'
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
		 * Apply interceptor with default actions
		 *
		 * @param {Function} callback
		 *
		 * @returns {Function}
		 *
		 * @private
		 */
		buildLoadCallback: function (callback) {
			return Ext.Function.createInterceptor(callback, function (records, options, success) {
				if (this.workflowTreeAppliedFilterIsEmpty())
					this.controllerToolbarPaging.cmfg('workflowTreeToolbarPagingFilterAdvancedReset');
			}, this);
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

				var params = Ext.clone(this.storeExtraParamsGet());
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

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.failure
		 * @param {Object} parameters.params
		 * @param {String} parameters.params.filter
		 * @param {String} parameters.params.flowStatus
		 * @param {Number} parameters.params.instanceId
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.success
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		positionActivityGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.params = Ext.isObject(parameters.params) ? parameters.params : {};

			var filter = parameters.params[CMDBuild.core.constants.Proxy.FILTER],
				flowStatus = parameters.params[CMDBuild.core.constants.Proxy.FLOW_STATUS],
				instanceId = parameters.params[CMDBuild.core.constants.Proxy.INSTANCE_ID],
				sort = this.cmfg('workflowTreeStoreGet').getSorters();

			// Error handling
				if (this.cmfg('workflowSelectedWorkflowIsEmpty'))
					return _error('positionActivityGet(): empty selected workflow', this);

				if (!Ext.isFunction(parameters.failure))
					return _error('positionActivityGet(): wrong failure function parameter', this, parameters.failure);

				if (!Ext.isFunction(parameters.success))
					return _error('positionActivityGet(): wrong success function parameter', this, parameters.success);

				if (!Ext.isNumber(instanceId) || Ext.isEmpty(instanceId))
					return _error('positionActivityGet(): wrong instanceId parameter', this, instanceId);
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.CARD_ID] = instanceId;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

			if (Ext.isString(filter) && !Ext.isEmpty(filter))
				params[CMDBuild.core.constants.Proxy.FILTER] = filter;

			if (Ext.isString(flowStatus) && !Ext.isEmpty(flowStatus))
				params[CMDBuild.core.constants.Proxy.FLOW_STATUS] = CMDBuild.controller.management.workflow.Utils.translateStatusFromCapitalizedMode(flowStatus);

			if (Ext.isArray(sort) && !Ext.isEmpty(sort))
				params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sort);

			CMDBuild.proxy.management.workflow.panel.tree.Tree.readPosition({
				params: params,
				scope: Ext.isObject(parameters.scope) ? parameters.scope : this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					// Error handling
					if (!Ext.isObject(decodedResponse) || Ext.Object.isEmpty(decodedResponse))
						return _error('positionActivityGet(): unmanaged response', this, decodedResponse);

					// Card found
					if (Ext.isBoolean(decodedResponse[CMDBuild.core.constants.Proxy.HAS_POSITION]) && decodedResponse[CMDBuild.core.constants.Proxy.HAS_POSITION]) {
						Ext.callback(
							parameters.success,
							Ext.isObject(parameters.scope) ? parameters.scope : this,
							[response, options, decodedResponse]
						);
					} else { // Card not found
						Ext.callback(
							parameters.failure,
							Ext.isObject(parameters.scope) ? parameters.scope : this,
							[response, options, decodedResponse]
						);
					}
				}
			});
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		positionActivityGetFailure: function (response, options, decodedResponse) {
			if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
				var flowStatus = decodedResponse[CMDBuild.core.constants.Proxy.FLOW_STATUS];

				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					Ext.String.format(
						CMDBuild.Translation.errors.reasons.CARD_NOTFOUND,
						this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID)
					)
				);

				// Sync UI with parameter filter property value
				if (Ext.isEmpty(options.params[CMDBuild.core.constants.Proxy.FILTER])) {
					this.controllerToolbarPaging.cmfg('workflowTreeToolbarPagingFilterBasicReset');
					this.cmfg('workflowTreeFilterClear', { disableStoreLoad: true });
				}

				// Sync UI with flow status returned value
				if (Ext.isString(flowStatus) && !Ext.isEmpty(flowStatus))
					this.cmfg('workflowTreeToolbarTopStatusValueSet', {
						silently: true,
						value: CMDBuild.controller.management.workflow.Utils.translateStatusFromCapitalizedMode(flowStatus)
					});

				this.cmfg('workflowTreeStoreLoad', {
					scope: this,
					callback: function (records, operation, success) { // Avoid first row selection and reset form status
						this.cmfg('workflowTreeReset');
						this.cmfg('workflowFormReset');
					}
				});
			} else {
				_error('positionActivityGetFailure(): unmanaged decodedResponse parameter', this, decodedResponse);
			}
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 * @param {String} activitySubsetId
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		positionActivityGetSuccess: function (response, options, decodedResponse, activitySubsetId) {
			if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
				var flowStatus = decodedResponse[CMDBuild.core.constants.Proxy.FLOW_STATUS],
					filter = options.params[CMDBuild.core.constants.Proxy.FILTER],
					position = decodedResponse[CMDBuild.core.constants.Proxy.POSITION];

				// Sync UI with parameter filter property value
				if (!Ext.isString(filter) || Ext.isEmpty(filter)) {
					this.controllerToolbarPaging.cmfg('workflowTreeToolbarPagingFilterBasicReset');
					this.cmfg('workflowTreeFilterClear', { disableStoreLoad: true });
				}

				// Sync UI with flowStatus returned value
				if (Ext.isString(flowStatus) && !Ext.isEmpty(flowStatus))
					this.cmfg('workflowTreeToolbarTopStatusValueSet', {
						silently: true,
						value: CMDBuild.controller.management.workflow.Utils.translateStatusFromCapitalizedMode(flowStatus)
					});

				this.cmfg('workflowTreeStoreLoad', {
					page: CMDBuild.core.Utils.getPageNumber(position),
					scope: this,
					callback: function (records, operation, success) {
						this.view.getSelectionModel().deselectAll();

						this.selectByMetadata(activitySubsetId);
						this.selectByPosition(position % CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT));
					}
				});
			} else {
				_error('positionActivityGetSuccess(): unmanaged decodedResponse parameter', this, decodedResponse);
			}
		},

		// Store extra params methods
			/**
			 * @param {String} name
			 *
			 * @returns {Mixed}
			 *
			 * @private
			 */
			storeExtraParamsGet: function (name) {
				var extraParams = this.cmfg('workflowTreeStoreGet').getProxy().extraParams;

				if (Ext.isString(name) && !Ext.isEmpty(name))
					return extraParams[name];

				return extraParams;
			},

			/**
			 * @param {String} name
			 *
			 * @returns {Mixed}
			 *
			 * @private
			 */
			storeExtraParamsRemove: function (name) {
				if (Ext.isString(name) && !Ext.isEmpty(name))
					delete this.storeExtraParamsGet()[name];
			},

			/**
			 * @param {Object} valueObject
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			storeExtraParamsSet: function (valueObject) {
				if (Ext.isObject(valueObject))
					this.cmfg('workflowTreeStoreGet').getProxy().extraParams = valueObject;
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
						this.view.getSelectionModel().on('selectionchange', function (selectionModel, selected, eOpts) {
							this.nodeRecursiveAnchestorsExpand(nodeToSelect);
						}, this, { single: true });

						this.view.getSelectionModel().select(nodeToSelect);
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
					this.view.getSelectionModel().on('selectionchange', function (selectionModel, selected, eOpts) {
						this.nodeRecursiveAnchestorsExpand(selected[0]);
					}, this, { single: true });

					this.view.getSelectionModel().select(position);
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
		 * Find an Activity in store and open, follows 3 steps:
		 * 	1. full call
		 * 	2. without filter
		 * 	3. without filter and flow status
		 *
		 * @param {Object} parameters
		 * @param {String} parameters.activitySubsetId
		 * @param {String} parameters.enableForceFlowStatus
		 * @param {String} parameters.forceFilter - only for internal use, false as default
		 * @param {String} parameters.forceFlowStatus - only for internal use, false as default
		 * @param {Number} parameters.instanceId
		 *
		 * @returns {Void}
		 */
		workflowTreeActivityOpen: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.enableForceFlowStatus = Ext.isBoolean(parameters.enableForceFlowStatus) ? parameters.enableForceFlowStatus : false;
			parameters.forceFilter = Ext.isBoolean(parameters.forceFilter) ? parameters.forceFilter : false;
			parameters.forceFlowStatus = Ext.isBoolean(parameters.forceFlowStatus) ? parameters.forceFlowStatus : false;

			// Error handling
				if (this.cmfg('workflowSelectedWorkflowIsEmpty'))
					return _error('workflowTreeActivityOpen(): no selected workflow found', this);

				if (!Ext.isNumber(parameters[CMDBuild.core.constants.Proxy.INSTANCE_ID]) || Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.INSTANCE_ID]))
					return _error('workflowTreeActivityOpen(): unmanaged instanceId parameter', this, parameters[CMDBuild.core.constants.Proxy.INSTANCE_ID]);
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.INSTANCE_ID] = parameters[CMDBuild.core.constants.Proxy.INSTANCE_ID];

			if (!parameters.forceFilter && !this.workflowTreeAppliedFilterIsEmpty())
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(this.workflowTreeAppliedFilterGet(CMDBuild.core.constants.Proxy.CONFIGURATION));

			if (parameters.enableForceFlowStatus) {
				if (!parameters.forceFlowStatus)
					params[CMDBuild.core.constants.Proxy.FLOW_STATUS] = this.controllerToolbarTop.cmfg('workflowTreeToolbarTopStatusValueGet');
			} else {
				params[CMDBuild.core.constants.Proxy.FLOW_STATUS] = this.controllerToolbarTop.cmfg('workflowTreeToolbarTopStatusValueGet');
			}

			this.positionActivityGet({ // Full call: with flow status and filter
				params: params,
				scope: this,
				failure: function (response, options, decodedResponse) {
					if (Ext.isString(params[CMDBuild.core.constants.Proxy.FILTER]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.FILTER])) {
						parameters.forceFilter = true;

						return this.workflowTreeActivityOpen(parameters);
					} else if (
						Ext.isString(params[CMDBuild.core.constants.Proxy.FLOW_STATUS]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.FLOW_STATUS])
						&& params[CMDBuild.core.constants.Proxy.FLOW_STATUS] != CMDBuild.core.constants.WorkflowStates.getAll()
						&& parameters.enableForceFlowStatus
					) {
						parameters.forceFlowStatus = true;

						return this.workflowTreeActivityOpen(parameters);
					} else {
						return Ext.callback(this.positionActivityGetFailure, this, [response, options, decodedResponse]); // Card not found and store reload
					}
				},
				success: function (response, options, decodedResponse) {
					Ext.callback(this.positionActivityGetSuccess, this, [response, options, decodedResponse, parameters[CMDBuild.core.constants.Proxy.ACTIVITY_SUBSET_ID]]);
				}
			});
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
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			workflowTreeAppliedFilterIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
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
		 * @param {Object} parameters
		 * @param {String} parameters.eventName
		 * @param {Function} parameters.fn
		 * @param {Object} parameters.scope
		 * @param {Object} parameters.options
		 *
		 * @returns {Void}
		 */
		workflowTreeApplyStoreEvent: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			// Error handling
				if (!Ext.isString(parameters.eventName) || Ext.isEmpty(parameters.eventName))
					return _error('workflowTreeApplyStoreEvent(): unmanaged eventName parameter', this, parameters.eventName);

				if (!Ext.isFunction(parameters.fn))
					return _error('workflowTreeApplyStoreEvent(): unmanaged fn parameter', this, parameters.fn);
			// END: Error handling

			this.view.getStore().on(
				parameters.eventName,
				parameters.fn,
				Ext.isObject(parameters.scope) ? parameters.scope : this,
				Ext.isObject(parameters.options) ? parameters.options : {}
			);
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

				if (Ext.isArray(emptyRuntimeParameters) && !Ext.isEmpty(emptyRuntimeParameters))
					return this.controllerRuntimeParameters.cmfg('fieldFilterRuntimeParametersShow', filter);

				filter.resolveCalculatedParameters();

				this.workflowTreeAppliedFilterSet({ value: filter });

				this.cmfg('workflowTreeStoreLoad');
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

			if (!parameters.disableStoreLoad)
				this.cmfg('workflowTreeStoreLoad');
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
		 * @returns {Void}
		 */
		workflowTreeReset: function () {
			this.view.getSelectionModel().deselectAll();

			this.cmfg('workflowSelectedActivityReset');

			_CMWFState.setActivityInstance(Ext.create('CMDBuild.model.CMActivityInstance'));
			_CMUIState.onlyGridIfFullScreen();
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

			this.cmfg('workflowTreeStoreGet').getRootNode().removeAll();

			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				var sorters = this.cmfg('workflowTreeStoreGet').getSorters();

				var params = Ext.isObject(parameters.params) ? parameters.params : {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.STATE] = this.controllerToolbarTop.cmfg('workflowTreeToolbarTopStatusValueGet');

				if (!this.workflowTreeAppliedFilterIsEmpty())
					params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(this.workflowTreeAppliedFilterGet(CMDBuild.core.constants.Proxy.CONFIGURATION));

				if (Ext.isArray(sorters) && !Ext.isEmpty(sorters))
					params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(sorters);

				this.storeExtraParamsSet(params); // Setup extraParams to work also with sorters and print report

				this.cmfg('workflowTreeStoreGet').loadPage(parameters.page, {
					params: params,
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
					callback: this.buildLoadCallback(
						Ext.isFunction(parameters.callback) ? parameters.callback : this.selectFirst
					)
				});
			}
		}
	});

})();
