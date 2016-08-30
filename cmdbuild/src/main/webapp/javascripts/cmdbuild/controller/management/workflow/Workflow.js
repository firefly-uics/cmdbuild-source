(function () {

	Ext.define('CMDBuild.controller.management.workflow.Workflow', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.GridAndForm',

		requires: [
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
			'workflowFormReset -> controllerForm',
			'workflowSelectedActivityGet',
			'workflowSelectedActivityReset',
			'workflowSelectedInstanceGet',
			'workflowSelectedInstanceReset',
			'workflowSelectedWorkflowAttributesGet',
			'workflowSelectedWorkflowAttributesIsEmpty',
			'workflowSelectedWorkflowGet = panelGridAndFormSelectedEntryTypeGet',
			'workflowSelectedWorkflowIsEmpty = panelGridAndFormSelectedEntryTypeIsEmpty',
			'workflowTreeActivityOpen -> controllerTree',
			'workflowTreeApplyStoreEvent -> controllerTree',
			'workflowTreeFilterApply -> controllerTree',
			'workflowTreeToolbarTopStatusValueSet -> controllerTree'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.Form}
		 */
		controllerForm: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.form.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.tree.Tree}
		 */
		controllerTree: undefined,

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

			// Shorthands
			this.tree = this.controllerTree.getView();
			this.form = this.controllerForm.getView();

			// View build
			this.view.add([this.tree, this.form]);
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowActivityRemoveCallback: function () {
			this.cmfg('workflowSelectedActivityReset');

			// Form setup
			this.controllerTree.cmfg('workflowFormReset');

			// Tree setup
			this.controllerTree.cmfg('workflowTreeStoreLoad', { callback: Ext.emptyFn }); // Avoid first row selection
		},

		/**
		 * @param {CMDBuild.model.management.workflow.Node} record
		 *
		 * @returns {Void}
		 */
		onWorkflowActivitySelect: function (record) {
			if (Ext.isObject(record) && !Ext.Object.isEmpty(record) && Ext.isFunction(record.get)) {
				this.cmfg('workflowSelectedActivityReset');

				var activityId = record.get(CMDBuild.core.constants.Proxy.ACTIVITY_ID),
					cardId = record.get(CMDBuild.core.constants.Proxy.CARD_ID),
					classId = record.get(CMDBuild.core.constants.Proxy.CLASS_ID);

				if (
					Ext.isString(activityId) && !Ext.isEmpty(activityId)
					&& Ext.isNumber(cardId) && !Ext.isEmpty(cardId)
					&& Ext.isNumber(classId) && !Ext.isEmpty(classId)
				) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID] = activityId;
					params[CMDBuild.core.constants.Proxy.CARD_ID] = cardId;
					params[CMDBuild.core.constants.Proxy.CLASS_ID] = classId;

					CMDBuild.proxy.management.workflow.Activity.read({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
								decodedResponse['rawData'] = decodedResponse; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

								this.workflowSelectedActivitySet({ value: decodedResponse });

								// Forward to sub controllers
								this.controllerForm.cmfg('onWorkflowFormActivitySelect');
							} else {
								_error('onWorkflowActivitySelect(): unmanaged read activity call response', this, record);
							}
						}
					});
				} else {
					_error('onWorkflowActivitySelect(): not correctly filled record model', this, record);
				}
			} else {
				_error('onWorkflowActivitySelect(): unmanaged record parameter', this, record);
			}
		},

		/**
		 * @param {CMDBuild.model.management.workflow.panel.form.tabs.activity.SaveResponse} responseModel
		 *
		 * @returns {Void}
		 */
		onWorkflowActivityUpdateCallback: function (responseModel) {
			this.cmfg('workflowSelectedActivityReset');

			if (Ext.isObject(responseModel) && !Ext.Object.isEmpty(responseModel)) {
				if (
					Ext.isString(responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS)) && !Ext.isEmpty(responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS))
					&& responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS) == CMDBuild.core.constants.WorkflowStates.getCompletedCapitalized()
				) {
					_CMWFState.setProcessInstance(Ext.create('CMDBuild.model.CMProcessInstance'));
					_CMUIState.onlyGridIfFullScreen();

					// Form setup
					this.controllerForm.cmfg('workflowFormReset');

					// Tree setup
					this.controllerTree.cmfg('workflowTreeReset');
				} else {
					// Form setup
					// FIXME: future implementation on tab controllers refactor

					// Tree setup
					var activityData = {};
					activityData[CMDBuild.core.constants.Proxy.ACTIVITY_SUBSET_ID] = responseModel.get(CMDBuild.core.constants.Proxy.ACTIVITY_SUBSET_ID);
					activityData[CMDBuild.core.constants.Proxy.INSTANCE_ID] = responseModel.get(CMDBuild.core.constants.Proxy.ID);

					this.cmfg('workflowTreeActivityOpen', activityData);
				}
			} else {
				_error('onWorkflowActivityUpdateCallback(): unmanaged responseModel parameter', this, responseModel);
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

			this.setViewTitle();

			// Forward to sub-controllers
			this.controllerForm.cmfg('onWorkflowFormAddButtonClick', id);
			this.controllerTree.cmfg('onWorkflowTreeAddButtonClick', id);
		},

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.model.management.workflow.Node} parameters.record
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.success
		 *
		 * @returns {Void}
		 */
		onWorkflowInstanceSelect: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isObject(parameters.record) && !Ext.Object.isEmpty(parameters.record) && Ext.isFunction(parameters.record.get)
			) {
				this.cmfg('workflowSelectedInstanceReset');

				var cardId = parameters.record.get(CMDBuild.core.constants.Proxy.CARD_ID),
					className = parameters.record.get(CMDBuild.core.constants.Proxy.CLASS_NAME);

				if (
					Ext.isNumber(cardId) && !Ext.isEmpty(cardId)
					&& Ext.isString(className) && !Ext.isEmpty(className)
				) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.CARD_ID] = cardId;
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;

					CMDBuild.proxy.management.workflow.Instance.read({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
								var instanceObject = decodedResponse;
								instanceObject['rawData'] = decodedResponse; // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor

								this.workflowSelectedInstanceSet({ value: instanceObject });

								// Forward to sub controllers
								this.controllerForm.cmfg('onWorkflowFormInstanceSelect');

								if (!Ext.isEmpty(parameters.success) && Ext.isFunction(parameters.success))
									Ext.callback(parameters.success, parameters.scope);
							} else {
								_error('onWorkflowInstanceSelect(): unmanaged read instance call response', this, parameters.record);
							}
						}
					});
				} else {
					_error('onWorkflowInstanceSelect(): not correctly filled record model', this, parameters.record);
				}
			} else {
				_error('onWorkflowInstanceSelect(): unmanaged parameters object', this, parameters);
			}
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
				this.readWorkflowData(
					node,
					function (records, operation, success) {
						CMDBuild.core.interfaces.service.LoadMask.manage(true, false); // Manual loadMask manage

						this.setViewTitle(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

						this.cmfg('onWorkflowWokflowSelect', node); // FIXME: node rawData property is for legacy mode with workflowState module

						this.onModuleInit(node); // Custom callParent() implementation
					}
				);
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

		/**
		 * Data gatherer chain (step 2)
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		readWorkflowAttributes: function (node, callback) {
			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
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
							this.readWorkflowDefaultFilter(node, callback);
						} else {
							_error('readWorkflowAttributes(): unmanaged response', this, decodedResponse);
						}
					}
				});
			} else {
				_error('readWorkflowAttributes(): empty selected workflow', this);
			}
		},

		/**
		 * Data gatherer chain (step 1)
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		readWorkflowData: function (node, callback) {
			if (
				Ext.isObject(node) && !Ext.Object.isEmpty(node)
				&& Ext.isNumber(node.get(CMDBuild.core.constants.Proxy.ENTITY_ID)) && !Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ENTITY_ID))
			) {
				CMDBuild.core.interfaces.service.LoadMask.manage(true, true); // Manual loadMask manage

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.management.workflow.Workflow.read({
					params: params,
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						var id = node.get(CMDBuild.core.constants.Proxy.ENTITY_ID);

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var selectedWorkflow = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return id == workflowObject[CMDBuild.core.constants.Proxy.ID];
							}, this);

							if (Ext.isObject(selectedWorkflow) && !Ext.Object.isEmpty(selectedWorkflow)) {
								this.workflowSelectedWorkflowSet({ value: selectedWorkflow });

								this.readWorkflowAttributes(node, callback);
							} else {
								_error('readWorkflowData(): workflow not found', this, id);
							}
						}
					}
				});
			} else {
				_error('readWorkflowData(): unmanaged id parameter', this, id);
			}
		},

		/**
		 * Data gatherer chain (step 3)
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		readWorkflowDefaultFilter: function (node, callback) {
			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				var filter = node.get(CMDBuild.core.constants.Proxy.FILTER);

				if (Ext.isEmpty(filter)) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
					params[CMDBuild.core.constants.Proxy.GROUP] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);

					CMDBuild.proxy.management.workflow.Workflow.readDefaultFilter({
						params: params,
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.ELEMENTS][0];

							if (!Ext.isEmpty(decodedResponse)) {
								var filterConfiguration = decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION];

								if (
									Ext.isString(filterConfiguration) && !Ext.isEmpty(filterConfiguration)
									&& CMDBuild.core.Utils.isJsonString(filterConfiguration)
								) {
									decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.decode(filterConfiguration);
								}

								node.set(CMDBuild.core.constants.Proxy.FILTER, Ext.create('CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter', decodedResponse));
							}
						},
						callback: callback
					});
				} else {
					Ext.callback(callback, this);
				}
			} else {
				_error('readWorkflowDefaultFilter(): empty selected workflow', this);
			}
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
			}
	});

})();
