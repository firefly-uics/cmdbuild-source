(function () {

	Ext.define('CMDBuild.routes.management.Workflow', {
		extend: 'CMDBuild.routes.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.routes.management.Workflow'
		],

		/**
		 * @property {CMDBuild.model.routes.management.Workflow}
		 *
		 * @private
		 */
		paramsModel: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		supportedPrintFormats: [
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.CSV
		],

		/**
		 * Apply clientFilter to grid
		 *
		 * @returns {Void}
		 *
		 * @private
		 *
		 * FIXME: future implementation
		 */
		applyClientFilter: Ext.emptyFn,

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.processIdentifier - process name
		 * @param {String} params.clientFilter - advanced filter object serialized
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		detail: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var accordionController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.routes.management.Workflow.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var selectedWorkflow = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(selectedWorkflow) && !Ext.Object.isEmpty(selectedWorkflow)) {
								accordionController.disableStoreLoad = true;
								accordionController.cmfg('accordionExpand', {
									scope: this,
									callback: function () {
										Ext.apply(accordionController, { // Setup accordion update callback
											scope: this,
											callback: function () {
												this.applyClientFilter();
											}
										});

										accordionController.cmfg('accordionDeselect');
										accordionController.cmfg('accordionUpdateStore', selectedWorkflow[CMDBuild.core.constants.Proxy.ID]);
									}
								});
							} else {
								CMDBuild.core.Message.error(
									CMDBuild.Translation.common.failure,
									CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
									false
								);
							}
						}
					}
				});
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		paramsValidation: function (params) {
			this.paramsModel = Ext.create('CMDBuild.model.routes.management.Workflow', params);

			// Process identifier validation
			if (!Ext.isString(this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER)) || Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			// Client filter validation
			if (!Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))) {
				// FIXME: validate filter with server side call
			}

			// Print format validation
			if (!Ext.Array.contains(this.supportedPrintFormats, this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidPrintFormat + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT) + ')',
					false
				);

				return false;
			}

			return this.callParent(arguments);
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		print: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var accordionController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
				var moduleController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.routes.management.Workflow.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var selectedWorkflow = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(selectedWorkflow) && !Ext.Object.isEmpty(selectedWorkflow)) {
								accordionController.disableStoreLoad = true;
								accordionController.cmfg('accordionExpand', {
									scope: this,
									callback: function () {
										Ext.apply(accordionController, { // Setup accordion update callback
											scope: this,
											callback: function () {
												this.applyClientFilter();

												moduleController.cmfg('workflowTreeApplyStoreEvent', {
													eventName: 'load',
													fn: function () {
														moduleController.cmfg('onWorkflowTreePrintButtonClick', this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT));
													},
													scope: this,
													options: { single: true }
												});
											}
										});

										accordionController.cmfg('accordionDeselect');
										accordionController.cmfg('accordionUpdateStore', selectedWorkflow[CMDBuild.core.constants.Proxy.ID]);
									}
								});
							} else {
								CMDBuild.core.Message.error(
									CMDBuild.Translation.common.failure,
									CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
									false
								);
							}
						}
					}
				});
			}
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		showAll: function (params, path, router) {
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerExpand', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() });
		}
	});

})();
