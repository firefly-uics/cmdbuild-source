(function () {

	Ext.define('CMDBuild.controller.management.routes.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Routes',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.management.routes.Workflow'
		],

		/**
		 * @property {CMDBuild.model.management.routes.Workflow}
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
		 * @param {Object} params
		 * @param {String} params.processIdentifier
		 * @param {String} params.clientFilter
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		detail: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.management.routes.Workflow.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var workflowObject = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(workflowObject) && !Ext.Object.isEmpty(workflowObject)) {
								return this.manageIdentifierProcess(workflowObject, this.manageFilterClient);
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
		 * Apply clientFilter to grid
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageFilterClient: function () {
			if (
				Ext.isObject(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))
				&& !Ext.Object.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))
			) {
				var moduleController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
				moduleController.cmfg('workflowTreeApplyStoreEvent', {
					eventName: 'load',
					fn: function () {
						moduleController.cmfg('workflowTreeFilterApply', Ext.create('CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter', {
							configuration: this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER)
						}));
					},
					scope: this,
					options: { single: true }
				});
			}
		},

		/**
		 * @param {Object} workflowObject
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageIdentifierProcess: function (workflowObject, callback) {
			var accordionController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

			if (!Ext.isObject(workflowObject) || Ext.Object.isEmpty(workflowObject))
				return _error('manageIdentifierProcess(): invalid workflowObject parameter', this, workflowObject);

			if (Ext.isObject(accordionController) && !Ext.Object.isEmpty(accordionController)) {
				accordionController.disableStoreLoad = true;
				accordionController.cmfg('accordionExpand', {
					scope: this,
					callback: function () {
						Ext.apply(accordionController, { // Setup accordion update callback
							scope: this,
							callback: Ext.isFunction(callback) ? callback : Ext.emptyFn
						});

						accordionController.cmfg('accordionDeselect');
						accordionController.cmfg('accordionUpdateStore', workflowObject[CMDBuild.core.constants.Proxy.ID]);
					}
				});
			} else {
				_error('manageIdentifierProcess(): accordion or module controllers not found', this, CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 * @private
		 */
		paramsValidation: function (params) {
			this.paramsModel = Ext.create('CMDBuild.model.management.routes.Workflow', params);

			// Process identifier validation
			if (Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER))) {
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
		 * @param {Object} params
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		print: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var moduleController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.management.routes.Workflow.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var workflowObject = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(workflowObject) && !Ext.Object.isEmpty(workflowObject)) {
								return this.manageIdentifierProcess(
									workflowObject,
									function () {
										moduleController.cmfg('workflowTreeApplyStoreEvent', {
											eventName: 'load',
											fn: function () {
												moduleController.cmfg('onWorkflowTreePrintButtonClick', this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT));
											},
											scope: this,
											options: { single: true }
										});
									}
								);
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
