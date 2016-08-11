(function () {

	Ext.define('CMDBuild.routes.management.Instance', {
		extend: 'CMDBuild.routes.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.routes.management.Instance'
		],

		/**
		 * @property {CMDBuild.model.routes.management.Instance}
		 *
		 * @private
		 */
		paramsModel: undefined,

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.processIdentifier - process name
		 * @param {Number} params.instanceIdentifier - instance id
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		detail: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var accordionController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
				var moduleController = CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.routes.management.Instance.readWorkflow({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var selectedWorkflow = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) == workflowObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(selectedWorkflow) && !Ext.Object.isEmpty(selectedWorkflow)) {
								if (!Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER))) { // Single card selection
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
															moduleController.cmfg('workflowTreeActivityOpen', { id: this.paramsModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER) });
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
										CMDBuild.Translation.errors.routesInvalidInstanceIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER) + ')',
										false
									);
								}
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
		 * @returns  {Boolean}
		 *
		 * @private
		 */
		paramsValidation: function (params) {
			this.paramsModel = Ext.create('CMDBuild.model.routes.management.Instance', params);

			// Process identifier validation
			if (
				Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER))
				|| !_CMCache.isEntryTypeByName(this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER))
			) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			// Instance identifier validation
			if (Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidInstanceIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			return this.callParent(arguments);
		}
	});

})();
