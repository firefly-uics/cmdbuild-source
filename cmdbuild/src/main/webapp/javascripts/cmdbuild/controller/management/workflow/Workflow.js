(function () {

	Ext.define('CMDBuild.controller.management.workflow.Workflow', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.GridAndForm',

		requires: [
			'CMDBuild.core.constants.Metadata',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.WorkflowStates',
			'CMDBuild.core.interfaces.service.LoadMask',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.management.workflow.Activity',
			'CMDBuild.proxy.management.workflow.Instance',
			'CMDBuild.proxy.management.workflow.Workflow'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowAbortButtonClick',
			'onWorkflowActivityRemoveCallback',
			'onWorkflowActivitySelect',
			'onWorkflowActivityUpdateCallback',
			'onWorkflowAddButtonClick',
			'onWorkflowFormActivityItemDoubleClick -> controllerForm',
			'onWorkflowInstanceSelect',
			'onWorkflowModuleInit = onModuleInit',
			'onWorkflowSaveFailure',
			'onWorkflowTreePrintButtonClick -> controllerTree',
			'onWorkflowWokflowSelect -> controllerForm, controllerTree',
			'panelGridAndFormToolsArrayBuild',
			'workflowFormReset -> controllerForm',
			'workflowIsStartActivityGet',
			'workflowLocalCacheWorkflowGetAll',
			'workflowSelectedActivityGet',
			'workflowSelectedActivityIsEmpty',
			'workflowSelectedActivityReset',
			'workflowSelectedInstanceGet',
			'workflowSelectedInstanceIsEmpty',
			'workflowSelectedInstanceReset',
			'workflowSelectedPreviousActivityGet',
			'workflowSelectedPreviousActivityIsEmpty',
			'workflowSelectedWorkflowAttributesGet',
			'workflowSelectedWorkflowAttributesIsEmpty',
			'workflowSelectedWorkflowGet',
			'workflowSelectedWorkflowIsEmpty',
			'workflowTreeActivitySelect -> controllerTree',
			'workflowTreeApplyStoreEvent -> controllerTree',
			'workflowTreeFilterApply -> controllerTree',
			'workflowTreeToolbarTopStatusValueSet -> controllerTree'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {Boolean}
		 *
		 * @private
		 */
		isStartActivity: false,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.Form}
		 */
		controllerForm: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.tree.Tree}
		 */
		controllerTree: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.form.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {Object}
		 */
		localCacheWorkflow: {
			byId: {},
			byName: {}
		},

		/**
		 * @property {CMDBuild.model.management.workflow.Activity}
		 *
		 * @private
		 */
		selectedActivity: undefined,

		/**
		 * @property {CMDBuild.model.management.workflow.Instance}
		 *
		 * @private
		 */
		selectedInstance: undefined,

		/**
		 * @property {CMDBuild.model.management.workflow.PreviousActivity}
		 *
		 * @private
		 */
		selectedPreviousActivity: undefined,

		/**
		 * @property {CMDBuild.model.management.workflow.Workflow}
		 *
		 * @private
		 */
		selectedWorkflow: undefined,

		/**
		 * Array of attribute models (CMDBuild.model.management.workflow.Attribute)
		 *
		 * @property {Array}
		 *
		 * @private
		 */
		selectedWorkflowAttributes: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.tree.TreePanel}
		 */
		tree: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.WorkflowView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.WorkflowView', { delegate: this });

			// View reset
			this.view.removeAll();
			this.view.removeDocked(this.view.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP));

			// Build sub-controllers
			this.controllerForm = Ext.create('CMDBuild.controller.management.workflow.panel.form.Form', { parentDelegate: this });
			this.controllerTree = Ext.create('CMDBuild.controller.management.workflow.panel.tree.Tree', { parentDelegate: this });

			// Build view (shorthands)
			this.view.add([
				this.tree = this.controllerTree.getView(),
				this.form = this.controllerForm.getView()
			]);
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildLocalCache: function (node, callback) {
			this.buildLocalCacheWorkflow(node, function () {
				this.buildLocalCacheWorkflowAttributes(function () {
					this.buildLocalCacheDefaultFilter(node, callback);
				});
			});
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildLocalCacheDefaultFilter: function (node, callback) {
			// Error handling
				if (this.cmfg('workflowSelectedWorkflowIsEmpty'))
					return _error('buildLocalCacheDefaultFilter(): empty selected workflow', this, this.cmfg('workflowSelectedWorkflowGet'));

				if (!Ext.isObject(node) || Ext.Object.isEmpty(node))
					return _error('buildLocalCacheWorkflow(): unmanaged node parameter', this, node);
			// END: Error handling

			var filter = node.get(CMDBuild.core.constants.Proxy.FILTER);

			if (Ext.isEmpty(filter)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.GROUP] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);

				CMDBuild.proxy.management.workflow.Workflow.readDefaultFilter({
					params: params,
					loadMask: false,
					scope: this,
					callback: callback,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.ELEMENTS][0];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var filterConfiguration = decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION];

							if (
								Ext.isString(filterConfiguration) && !Ext.isEmpty(filterConfiguration)
								&& CMDBuild.core.Utils.isJsonString(filterConfiguration)
							) {
								decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.decode(filterConfiguration);
							}

							node.set(CMDBuild.core.constants.Proxy.FILTER, Ext.create('CMDBuild.model.management.workflow.panel.tree.filter.advanced.Filter', decodedResponse));
						}
					}
				});
			} else {
				Ext.callback(callback, this);
			}
		},

		/**
		 * Builds local workflows cache and find selected one
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildLocalCacheWorkflow: function (node, callback) {
			// Error handling
				if (!Ext.isObject(node) || Ext.Object.isEmpty(node))
					return _error('buildLocalCacheWorkflow(): unmanaged node parameter', this, node);

				if (!Ext.isNumber(node.get(CMDBuild.core.constants.Proxy.ENTITY_ID)) || Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ENTITY_ID)))
					return _error('buildLocalCacheWorkflow(): unmanaged node entityId property', this, node.get(CMDBuild.core.constants.Proxy.ENTITY_ID));
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.management.workflow.Workflow.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
						this.workflowLocalCacheWorkflowSet(decodedResponse);

						var selectedWorkflow = this.workflowLocalCacheWorkflowGet({ id: node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) });

						if (Ext.isObject(selectedWorkflow) && !Ext.Object.isEmpty(selectedWorkflow)) {
							this.workflowIsStartActivityReset();
							this.workflowSelectedWorkflowSet({ value: selectedWorkflow });

							Ext.callback(callback, this);
						} else {
							_error('buildLocalCacheWorkflow(): workflow not found', this, id);
						}
					} else {
						_error('buildLocalCacheWorkflow(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildLocalCacheWorkflowAttributes: function (callback) {
			// Error handling
				if (this.cmfg('workflowSelectedWorkflowIsEmpty'))
					return _error('buildLocalCacheWorkflowAttributes(): empty selected workflow', this, this.cmfg('workflowSelectedWorkflowGet'));
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

			CMDBuild.proxy.management.workflow.Workflow.readAttributes({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

					if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
						this.workflowSelectedWorkflowAttributesSet(decodedResponse);

						Ext.callback(callback, this);
					} else {
						_error('buildLocalCacheWorkflowAttributes(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowAbortButtonClick: function () {
			this.cmfg('workflowSelectedInstanceReset');
			this.cmfg('workflowSelectedActivityReset');
			this.workflowIsStartActivityReset();

			// Forward to sub controllers
			this.controllerForm.cmfg('onWorkflowFormAbortButtonClick');
			this.controllerTree.cmfg('onWorkflowTreeAbortButtonClick');
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowActivityRemoveCallback: function () {
			this.cmfg('workflowSelectedActivityReset');

			// Form setup
			this.controllerForm.cmfg('workflowFormReset');

			// Tree setup
			this.controllerTree.cmfg('workflowTreeStoreLoad', { disableFirstRowSelection: true });
		},

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.model.management.workflow.Node} parameters.record
		 * @param {Function} parameters.callback
		 * @param {Boolean} parameters.loadMask
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onWorkflowActivitySelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true;

			// Error handling
				if (!Ext.isObject(parameters.record) || Ext.Object.isEmpty(parameters.record) || !Ext.isFunction(parameters.record.get))
					return _error('onWorkflowActivitySelect(): unmanaged record parameter', this, record);

				if (
					!Ext.isString(parameters.record.get(CMDBuild.core.constants.Proxy.ACTIVITY_ID)) || Ext.isEmpty(parameters.record.get(CMDBuild.core.constants.Proxy.ACTIVITY_ID))
					|| !Ext.isNumber(parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID)) || Ext.isEmpty(parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID))
					|| !Ext.isNumber(parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_ID)) || Ext.isEmpty(parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_ID))
				) {
					return _error('onWorkflowActivitySelect(): not correctly filled record model', this, parameters.record);
				}
			// END: Error handling

			this.cmfg('workflowSelectedActivityReset');
			this.workflowIsStartActivityReset();

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID] = parameters.record.get(CMDBuild.core.constants.Proxy.ACTIVITY_ID);
			params[CMDBuild.core.constants.Proxy.CARD_ID] = parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID);
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_ID);

			CMDBuild.proxy.management.workflow.Activity.read({
				params: params,
				loadMask: parameters.loadMask,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
						decodedResponse['rawData'] = decodedResponse; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

						this.workflowSelectedActivitySet({ value: decodedResponse });

						// Forward to sub controllers
						this.controllerForm.cmfg('onWorkflowFormActivitySelect');

						if (!Ext.isEmpty(parameters.callback) && Ext.isFunction(parameters.callback))
							Ext.callback(
								parameters.callback,
								Ext.isObject(parameters.scope) ? parameters.scope : this
							);
					} else {
						_error('onWorkflowActivitySelect(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.management.workflow.panel.form.tabs.activity.SaveResponse} responseModel
		 *
		 * @returns {Void}
		 */
		onWorkflowActivityUpdateCallback: function (responseModel) {
			this.cmfg('workflowSelectedActivityReset');

			// Error handling
				if (!Ext.isObject(responseModel) || Ext.Object.isEmpty(responseModel))
					return _error('onWorkflowActivityUpdateCallback(): unmanaged responseModel parameter', this, responseModel);
			// END: Error handling

			if (
				Ext.isString(responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS)) && !Ext.isEmpty(responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS))
				&& (
					responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS) == CMDBuild.core.constants.WorkflowStates.getCompletedCapitalized()
					|| responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS) == CMDBuild.core.constants.WorkflowStates.getSuspendedCapitalized()
				)
			) {
				_CMWFState.setProcessInstance(Ext.create('CMDBuild.model.CMProcessInstance'));
				_CMUIState.onlyGridIfFullScreen();

				// Form setup
				this.controllerForm.cmfg('workflowFormReset');

				// Tree setup
				this.controllerTree.cmfg('workflowTreeReset');
				this.controllerTree.cmfg('workflowTreeStoreLoad', { disableFirstRowSelection: true });
			} else {
				// Form setup
				// FIXME: future implementation on tab controllers refactor

				// Tree setup
				var params = {};
				params[CMDBuild.core.constants.Proxy.INSTANCE_ID] = responseModel.get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.METADATA] = responseModel.get(CMDBuild.core.constants.Proxy.METADATA);

				this.cmfg('workflowTreeActivitySelect', params);
			}
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Void}
		 */
		onWorkflowAddButtonClick: function (id) {
			id = Ext.isNumber(id) && !Ext.isEmpty(id) ? id : this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);

			this.cmfg('workflowSelectedActivityReset');

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = id;

			CMDBuild.proxy.management.workflow.Workflow.readStart({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
						decodedResponse['rawData'] = decodedResponse; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

						this.workflowIsStartActivitySet();
						this.workflowSelectedActivitySet({ value: decodedResponse });

						this.setViewTitle();

						// Forward to sub-controllers
						this.controllerForm.cmfg('onWorkflowFormAddButtonClick', id);
						this.controllerTree.cmfg('onWorkflowTreeAddButtonClick', id);
					} else {
						_error('onWorkflowAddButtonClick(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.model.management.workflow.Node} parameters.record
		 * @param {Function} parameters.callback
		 * @param {Boolean} parameters.loadMask
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onWorkflowInstanceSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true;

			this.cmfg('workflowSelectedInstanceReset');
			this.workflowIsStartActivityReset();

			// Error handling
				if (!Ext.isObject(parameters.record) || Ext.Object.isEmpty(parameters.record) || !Ext.isFunction(parameters.record.get))
					return _error('onWorkflowInstanceSelect(): unmanaged record parameter', this, parameters.record);

				if (
					!Ext.isNumber(parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID)) || Ext.isEmpty(parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID))
					|| !Ext.isString(parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_NAME)) || Ext.isEmpty(parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_NAME))
				) {
					return _error('onWorkflowInstanceSelect(): not correctly filled record model', this, parameters.record);
				}
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.CARD_ID] = parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_NAME);

			CMDBuild.proxy.management.workflow.Instance.read({
				params: params,
				loadMask: parameters.loadMask,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
						var instanceObject = decodedResponse;
						instanceObject['rawData'] = decodedResponse; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

						this.workflowSelectedInstanceSet({ value: instanceObject });

						// Forward to sub controllers
						this.controllerForm.cmfg('onWorkflowFormInstanceSelect');

						if (!Ext.isEmpty(parameters.callback) && Ext.isFunction(parameters.callback))
							Ext.callback(
								parameters.callback,
								Ext.isObject(parameters.scope) ? parameters.scope : this
							);
					} else {
						_error('onWorkflowInstanceSelect(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWorkflowModuleInit: function (node) {
			if (Ext.isObject(node) && !Ext.Object.isEmpty(node)) {
				CMDBuild.core.interfaces.service.LoadMask.manage(true, true); // Manual loadMask manage (show)

				this.buildLocalCache(node, function () {
					CMDBuild.core.interfaces.service.LoadMask.manage(true, false); // Manual loadMask manage (hide)

					this.setViewTitle(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

					this.cmfg('onWorkflowWokflowSelect', node); // FIXME: node rawData property is for legacy mode with workflowState module

					this.onModuleInit(node); // Custom callParent() implementation
				});
			}
		},

		/**
		 * Forward to sub-controllers
		 *
		 * @returns {Void}
		 *
		 * FIXME: to fix on activity tab refactor
		 */
		onWorkflowSaveFailure: function () {
			this.controllerTree.cmfg('onWorkflowTreeSaveFailure');
		},

		// LocalCacheWorkflow property functions
			/**
			 * @returns {Boolean}
			 *
			 * @private
			 */
			workflowLocalCacheWorkflowReset: function () {
				this.localCacheWorkflow = {
					byId: {},
					byName: {}
				};
			},

			/**
			 * @param {Object} parameters
			 * @param {Number} parameters.id
			 * @param {String} parameters.name
			 *
			 * @returns {CMDBuild.model.management.workflow.Workflow or null}
			 *
			 * @private
			 */
			workflowLocalCacheWorkflowGet: function (parameters) {
				parameters = Ext.isObject(parameters) ? parameters : {};

				if (Ext.isNumber(parameters.id) && !Ext.isEmpty(parameters.id))
					return this.localCacheWorkflow.byId[parameters.id];

				if (Ext.isString(parameters.name) && !Ext.isEmpty(parameters.name))
					return this.localCacheWorkflow.byName[parameters.name];

				return null;
			},

			/**
			 * @returns {Array}
			 */
			workflowLocalCacheWorkflowGetAll: function () {
				return Ext.Object.getValues(this.localCacheWorkflow.byName);
			},

			/**
			 * @param {Array} workflows
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowLocalCacheWorkflowSet: function (workflows) {
				this.workflowLocalCacheWorkflowReset();

				if (Ext.isArray(workflows) && !Ext.isEmpty(workflows))
					Ext.Array.each(workflows, function (workflowObject, i, allWorkflowObjects) {
						if (Ext.isObject(workflowObject) && !Ext.Object.isEmpty(workflowObject)) {
							workflowObject['rawData'] = workflowObject; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

							var model = Ext.create('CMDBuild.model.management.workflow.Workflow', workflowObject);

							this.localCacheWorkflow.byId[model.get(CMDBuild.core.constants.Proxy.ID)] = model;
							this.localCacheWorkflow.byName[model.get(CMDBuild.core.constants.Proxy.NAME)] = model;
						}
					}, this);
			},

		// SelectedActivity property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowSelectedActivityGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedActivity';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			workflowSelectedActivityIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedActivity';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			workflowSelectedActivityReset: function (parameters) {
				this.propertyManageReset('selectedActivity');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowSelectedActivitySet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.management.workflow.Activity';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedActivity';

					this.propertyManageSet(parameters);

					// Manage previous selected activity
					if (!this.cmfg('workflowIsStartActivityGet')) {
						var selectedPreviousActivityObject = {};
						selectedPreviousActivityObject[CMDBuild.core.constants.Proxy.INSTANCE_ID] = this.cmfg('workflowSelectedInstanceGet', CMDBuild.core.constants.Proxy.ID);
						selectedPreviousActivityObject[CMDBuild.core.constants.Proxy.METADATA] = this.cmfg('workflowSelectedActivityGet',  CMDBuild.core.constants.Proxy.METADATA);
						selectedPreviousActivityObject[CMDBuild.core.constants.Proxy.WORKFLOW_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

						this.workflowSelectedPreviousActivitySet({ value: selectedPreviousActivityObject });
					}
				}
			},

		// SelectedInstance property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowSelectedInstanceGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedInstance';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			workflowSelectedInstanceIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedInstance';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			workflowSelectedInstanceReset: function (parameters) {
				this.propertyManageReset('selectedInstance');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowSelectedInstanceSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.management.workflow.Instance';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedInstance';

					this.propertyManageSet(parameters);

					// Manage previous selected activity
					var selectedPreviousActivityObject = {};
					selectedPreviousActivityObject[CMDBuild.core.constants.Proxy.INSTANCE_ID] = this.cmfg('workflowSelectedInstanceGet', CMDBuild.core.constants.Proxy.ID);
					selectedPreviousActivityObject[CMDBuild.core.constants.Proxy.WORKFLOW_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

					this.workflowSelectedPreviousActivitySet({ value: selectedPreviousActivityObject });
				}
			},

		// SelectedPreviousActivity property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowSelectedPreviousActivityGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedPreviousActivity';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			workflowSelectedPreviousActivityIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedPreviousActivity';
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
			workflowSelectedPreviousActivitySet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.management.workflow.PreviousActivity';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedPreviousActivity';

					this.propertyManageSet(parameters);
				}
			},

		// SelectedWorkflow property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowSelectedWorkflowGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			workflowSelectedWorkflowIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';
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
			workflowSelectedWorkflowSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.management.workflow.Workflow';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';

					this.propertyManageSet(parameters);
				}
			},

		// SelectedWorkflowAttributes property functions
			/**
			 * @returns {Array}
			 */
			workflowSelectedWorkflowAttributesGet: function () {
				return this.selectedWorkflowAttributes;
			},

			/**
			 * @returns {Boolean}
			 */
			workflowSelectedWorkflowAttributesIsEmpty: function () {
				return Ext.isEmpty(this.selectedWorkflowAttributes);
			},

			/**
			 * @param {Array} attributes
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowSelectedWorkflowAttributesSet: function (attributes) {
				this.selectedWorkflowAttributes = [];

				if (Ext.isArray(attributes) && !Ext.isEmpty(attributes))
					Ext.Array.each(attributes, function (attributeObject, i, allAttributeObjects) {
						if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject))
							this.selectedWorkflowAttributes.push(Ext.create('CMDBuild.model.management.workflow.Attribute', attributeObject));
					}, this);
			},

			// IsStartActivity flag functions
				/**
				 * @returns {Boolean}
				 */
				workflowIsStartActivityGet: function () {
					return this.isStartActivity;
				},

				/**
				 * @returns {Void}
				 *
				 * @private
				 */
				workflowIsStartActivityReset: function () {
					this.isStartActivity = false;
				},

				/**
				 * @returns {Void}
				 *
				 * @private
				 */
				workflowIsStartActivitySet: function () {
					this.isStartActivity = true;
				}
	});

})();
