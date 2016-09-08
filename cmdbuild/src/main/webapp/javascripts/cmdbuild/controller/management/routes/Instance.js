(function () {

	Ext.define('CMDBuild.controller.management.routes.Instance', {
		extend: 'CMDBuild.controller.common.abstract.Routes',

		requires: [
			'CMDBuild.controller.management.workflow.Utils',
			'CMDBuild.core.configurations.Routes',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.WorkflowStates',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.management.routes.Instance'
		],

		/**
		 * @property {CMDBuild.model.management.routes.Instance}
		 *
		 * @private
		 */
		parametersModel: undefined,

		/**
		 * @param {Object} params
		 * @param {String} params.processIdentifier
		 * @param {Number} params.instanceIdentifier
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		detail: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.management.routes.Instance.readWorkflow({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var workflowObject = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(workflowObject) && !Ext.Object.isEmpty(workflowObject)) {
								if (!Ext.isEmpty(this.parametersModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER))) // Single card selection
									return this.readInstanceDetails(workflowObject);

								if (!Ext.Object.isEmpty(this.parametersModel.get(CMDBuild.core.constants.Proxy.SIMPLE_FILTER))) // SimpleFilter
									return this.manageFilterSimple(workflowObject);
							}

							return CMDBuild.core.Message.error(
								CMDBuild.Translation.common.failure,
								CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
								false
							);
						} else {
							_error('detail(): unmanaged decodedResponse value', this, decodedResponse);
						}
					}
				});
			}
		},

		/**
		 * Get instance id and flowStatus from filtered cards
		 *
		 * @param {Object} workflowObject
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageFilterSimple: function (workflowObject) {
			var simpleFilterDefinitionObject = this.parametersModel.get(CMDBuild.core.constants.Proxy.SIMPLE_FILTER);

			var params = {};
			params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(['Description']);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER);
			params[CMDBuild.core.constants.Proxy.FILTER] = '{"attribute":{"simple":{"attribute":"'
				+ simpleFilterDefinitionObject[CMDBuild.core.constants.Proxy.KEY] + '","operator":"equal","value":["'
				+ simpleFilterDefinitionObject[CMDBuild.core.constants.Proxy.VALUE] + '"]}}}';
			params[CMDBuild.core.constants.Proxy.STATE] = CMDBuild.core.constants.WorkflowStates.getAll();

			CMDBuild.proxy.management.routes.Instance.readAll({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (decodedResponse[CMDBuild.core.constants.Proxy.RESULTS] == 1) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS][0];

						this.manageIdentifierInstance(
							workflowObject,
							decodedResponse[CMDBuild.core.constants.Proxy.ID],
							decodedResponse[CMDBuild.core.constants.Proxy.FLOW_STATUS]
						);
					} else {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.routesInvalidSimpleFilter,
							false
						);
					}
				}
			});
		},

		/**
		 * @param {Object} workflowObject
		 * @param {Number} instanceIdentifier
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageIdentifierInstance: function (workflowObject, instanceIdentifier) {
			var accordionController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerWithNodeWithIdGet', workflowObject[CMDBuild.core.constants.Proxy.ID]);
			var moduleController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

			// Error handling
				if (!Ext.isObject(workflowObject) || Ext.Object.isEmpty(workflowObject))
					return _error('manageIdentifierInstance(): unmanaged workflowObject parameter', this, workflowObject);

				if (!Ext.isNumber(instanceIdentifier) || Ext.isEmpty(instanceIdentifier))
					return _error('manageIdentifierInstance(): unmanaged instanceIdentifier parameter', this, instanceIdentifier);

				if (!Ext.isObject(accordionController) || Ext.Object.isEmpty(accordionController) || !Ext.isFunction(accordionController.cmfg))
					return _error('manageIdentifierInstance(): accordionController not found', this, accordionController);

				if (!Ext.isObject(moduleController) || Ext.Object.isEmpty(moduleController) || !Ext.isFunction(moduleController.cmfg))
					return _error('manageIdentifierInstance(): moduleController not found', this, moduleController);
			// END: Error handling

			accordionController.disableStoreLoad = true;
			accordionController.cmfg('accordionExpand', {
				scope: this,
				callback: function () {
					Ext.apply(accordionController, { // Setup accordion update callback
						scope: this,
						callback: function () {
							moduleController.cmfg('workflowTreeApplyStoreEvent', {
								eventName: 'load',
								fn: function () {
									var params = {};
									params['enableForceFlowStatus'] = true;
									params[CMDBuild.core.constants.Proxy.INSTANCE_ID] = instanceIdentifier;

									moduleController.cmfg('workflowTreeActivityOpen', params);
								},
								scope: this,
								options: { single: true }
							});
						}
					});

					accordionController.cmfg('accordionDeselect');
					accordionController.cmfg('accordionUpdateStore', { selectionId: workflowObject[CMDBuild.core.constants.Proxy.ID] });
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns  {Boolean}
		 *
		 * @override
		 * @private
		 */
		paramsValidation: function (parameters) {
			this.parametersModel = Ext.create('CMDBuild.model.management.routes.Instance', parameters);

			// Process identifier validation
			if (Ext.isEmpty(this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			// Instance identifier validation
			if (
				Ext.isEmpty(this.parametersModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER))
				&& Ext.Object.isEmpty(this.parametersModel.get(CMDBuild.core.constants.Proxy.SIMPLE_FILTER))
			) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidInstanceIdentifier + ' (' + this.parametersModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			return this.callParent(arguments);
		},

		/**
		 * Get instance flowStatus
		 *
		 * @param {Object} workflowObject
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		readInstanceDetails: function (workflowObject) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(['Description']);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.parametersModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER);
			params[CMDBuild.core.constants.Proxy.STATE] = CMDBuild.core.constants.WorkflowStates.getAll();

			CMDBuild.proxy.management.routes.Instance.readAll({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.ROWS];

					if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
						var instanceObject = Ext.Array.findBy(decodedResponse, function (instanceObject, i) {
							return this.parametersModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER) == instanceObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (Ext.isObject(instanceObject) && !Ext.Object.isEmpty(instanceObject))
							this.manageIdentifierInstance(
								workflowObject,
								instanceObject[CMDBuild.core.constants.Proxy.ID],
								instanceObject[CMDBuild.core.constants.Proxy.FLOW_STATUS]
							);
					} else {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.routesInvalidSimpleFilter,
							false
						);
					}
				}
			});
		}
	});

})();
