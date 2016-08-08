(function () {

	Ext.define('CMDBuild.controller.management.workflow.Workflow', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.GridAndForm',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.workflow.management.Activity',
			'CMDBuild.proxy.workflow.management.Workflow'
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
			'onWorkflowFormReset -> controllerForm',
			'onWorkflowModuleInit = onModuleInit',
			'onWorkflowSaveFailure',
			'onWorkflowStatusSelectionChange -> controllerTree',
			'onWorkflowWokflowSelect -> controllerForm, controllerTree',
			'workflowSelectedActivityGet',
			'workflowSelectedWorkflowAttributesGet',
			'workflowSelectedWorkflowAttributesIsEmpty',
			'workflowSelectedWorkflowGet = panelGridAndFormSelectedEntryTypeGet',
			'workflowSelectedWorkflowIsEmpty = panelGridAndFormSelectedEntryTypeIsEmpty'
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
		 * @property {CMDBuild.controller.management.workflow.panel.tree.Tree}
		 */
		controllerTree: undefined,

		/**
		 * @property {CMDBuild.model.workflow.management.Activity}
		 *
		 * @private
		 */
		selectedActivity: undefined,

		/**
		 * @property {CMDBuild.model.workflow.management.Workflow}
		 *
		 * @private
		 */
		selectedWorkflow: undefined,

		/**
		 * Array of attribute models (CMDBuild.model.workflow.management.Attribute)
		 *
		 * @property {Array}
		 *
		 * @private
		 */
		selectedWorkflowAttributes: undefined,

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

			// View build
			this.view.add([
				this.controllerTree.getView(),
				this.controllerForm.getView()
			]);
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Void}
		 */
		onWorkflowAddButtonClick: function (id) {
			id = Ext.isNumber(id) && !Ext.isEmpty(id) ? id : this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);

			this.workflowSelectedActivityReset();

			this.setViewTitle();

			// Forward to sub-controllers
			this.controllerForm.cmfg('onWorkflowFormAddButtonClick', id);
			this.controllerTree.cmfg('onWorkflowTreeAddButtonClick', id);
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowActivityRemoveCallback: function () {
			this.workflowSelectedActivityReset();

			// Form setup
			// FIXME: future implementation on tab controllers refactor

			// Tree setup
			this.controllerTree.cmfg('workflowTreeStoreLoad', { callback: Ext.emptyFn }); // Avoid first row selection
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowActivitySelect: function () {
			this.workflowSelectedActivityReset();

			if (this.controllerTree.getView().getSelectionModel().hasSelection()) {
				var selectedNode = this.controllerTree.getView().getSelectionModel().getSelection()[0];

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID] = selectedNode.get(CMDBuild.core.constants.Proxy.ACTIVITY_ID);
				params[CMDBuild.core.constants.Proxy.CARD_ID] = selectedNode.get(CMDBuild.core.constants.Proxy.CARD_ID);
				params[CMDBuild.core.constants.Proxy.CLASS_ID] = selectedNode.get(CMDBuild.core.constants.Proxy.CLASS_ID);

				if ( // Sub-activity node or Workflow instance with only one activity
					Ext.isString(params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID])
					&& Ext.isNumber(params[CMDBuild.core.constants.Proxy.CARD_ID]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.CARD_ID])
					&& Ext.isNumber(params[CMDBuild.core.constants.Proxy.CLASS_ID]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.CLASS_ID])
				) {
					CMDBuild.proxy.workflow.management.Activity.read({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
								decodedResponse['rawData'] = selectedNode.get('rawData'); // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modeules refactor

								this.workflowSelectedActivitySet({ value: decodedResponse });

								// Forward event to sub controllers
								this.controllerForm.cmfg('onWorkflowFormActivitySelect');
							}
						}
					});
				} else if ( // Super-activity node
					Ext.isNumber(params[CMDBuild.core.constants.Proxy.CARD_ID]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.CARD_ID])
					&& Ext.isNumber(params[CMDBuild.core.constants.Proxy.CLASS_ID]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.CLASS_ID])
				) {
					var activityObject = selectedNode.get('rawData');
					activityObject['rawData'] = selectedNode.get('rawData'); // FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modeules refactor

					this.workflowSelectedActivitySet({ value: activityObject });

					// Forward event to sub controllers
					this.controllerForm.cmfg('onWorkflowFormActivitySelect', true);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.workflow.management.panel.form.tabs.activity.SaveResponse} responseModel
		 *
		 * @returns {Void}
		 */
		onWorkflowActivityUpdateCallback: function (responseModel) {
			this.workflowSelectedActivityReset();

			if (Ext.isObject(responseModel) && !Ext.Object.isEmpty(responseModel)) {
				// Form setup
				// FIXME: future implementation on tab controllers refactor

				// Tree setup
				this.controllerTree.cmfg('workflowTreeActivityOpen', {
					flowStatus: responseModel.get(CMDBuild.core.constants.Proxy.FLOW_STATUS),
					id: responseModel.get(CMDBuild.core.constants.Proxy.ID)
				});
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
_debug('onWorkflowModuleInit', node);
				this.readWorkflowData(
					node,
					function (records, operation, success) {
						this.setViewTitle(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.DESCRIPTION));
_debug('selectedWorkflow', this.cmfg('workflowSelectedWorkflowGet'));
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
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.workflow.management.Workflow.readAttributes({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							this.workflowSelectedWorkflowAttributesSet(decodedResponse);

							this.readWorkflowDefaultFilter(node, callback);
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
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.workflow.management.Workflow.read({
					params: params,
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

					CMDBuild.proxy.workflow.management.Workflow.readDefaultFilter({
						params: params,
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
			 *
			 * @private
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
_debug('workflowSelectedActivitySet', parameters);
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.workflow.management.Activity';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedActivity';

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
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.workflow.management.Workflow';
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
							this.selectedWorkflowAttributes.push(Ext.create('CMDBuild.model.workflow.management.Attribute', attributeObject));
					}, this);
			}
	});

})();
