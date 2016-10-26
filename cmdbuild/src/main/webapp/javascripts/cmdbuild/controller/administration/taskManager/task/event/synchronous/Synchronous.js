(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.synchronous.Synchronous', {
		extend: 'CMDBuild.controller.administration.taskManager.task.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.event.Synchronous'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.Event}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskAbortButtonClick',
			'onTaskManagerFormTaskAddButtonClick',
			'onTaskManagerFormTaskCloneButtonClick',
			'onTaskManagerFormTaskEventSynchronousEntryTypeSelected',
			'onTaskManagerFormTaskEventSynchronousRowSelected = onTaskManagerFormTaskRowSelected',
			'onTaskManagerFormTaskEventSynchronousSaveButtonClick = onTaskManagerFormTaskSaveButtonClick',
			'onTaskManagerFormTaskEventSynchronousValidateSetup -> controllerStep3',
			'onTaskManagerFormTaskModifyButtonClick',
			'onTaskManagerFormTaskRemoveButtonClick'
		],

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step1}
		 */
		controllerStep1: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step2}
		 */
		controllerStep2: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step3}
		 */
		controllerStep3: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.event.Event} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Build sub controllers
			this.controllerStep1 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.synchronous.Step1', { parentDelegate: this });
			this.controllerStep2 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.synchronous.Step2', { parentDelegate: this });
			this.controllerStep3 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.synchronous.Step3', { parentDelegate: this });

			this.cmfg('taskManagerFormPanelsAdd', [
				this.controllerStep1.getView(),
				this.controllerStep2.getView(),
				this.controllerStep3.getView()
			]);
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousEntryTypeSelected: function () {
			this.controllerStep1.cmfg('onTaskManagerFormTaskEventSynchronousStep1EntryTypeSelected');
			this.controllerStep2.cmfg('onTaskManagerFormTaskEventSynchronousStep2EntryTypeSelected');
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onTaskManagerFormTaskEventSynchronousRowSelected: function () {
			if (!this.cmfg('taskManagerSelectedTaskIsEmpty')) {
				this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');

				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.administration.taskManager.task.event.Synchronous.read({
					params: params,
					scope: this,
					success: function (rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							if (Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]))
								decodedResponse[CMDBuild.core.constants.Proxy.FILTER] = Ext.decode(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]);

							// Is needed to setup filter field before load record values
							this.controllerStep2.cmfg('onTaskManagerFormTaskEventSynchronousStep2EntryTypeSelected', {
								className: decodedResponse[CMDBuild.core.constants.Proxy.CLASS_NAME],
								scope: this,
								callback: function () {
									this.cmfg('taskManagerFormViewGet').loadRecord(
										Ext.create('CMDBuild.model.administration.taskManager.task.event.synchronous.Synchronous', decodedResponse)
									);

									this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: false });

									this.onTaskManagerFormTaskRowSelected(); // CallParent alias
								}
							});
						} else {
							_error('onTaskManagerFormTaskEventSynchronousRowSelected(): unmanaged response', this, decodedResponse);
						}
					}
				});
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onTaskManagerFormTaskEventSynchronousSaveButtonClick: function () {
			var formData = Ext.create('CMDBuild.model.administration.taskManager.task.event.synchronous.Synchronous', this.cmfg('taskManagerFormViewDataGet'));

			// Validate before save
			if (this.validate(formData.get(CMDBuild.core.constants.Proxy.ACTIVE)))
				if (this.cmfg('taskManagerSelectedTaskIsEmpty')) {
					CMDBuild.proxy.administration.taskManager.task.event.Synchronous.create({
						params: formData.getSubmitData('create'),
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.administration.taskManager.task.event.Synchronous.update({
						params: formData.getSubmitData('update'),
						scope: this,
						success: this.success
					});
				}

			this.onTaskManagerFormTaskSaveButtonClick(); // CallParent alias
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 * @private
		 */
		removeItem: function () {
			// Error handling
				if (this.cmfg('taskManagerSelectedTaskIsEmpty'))
					return _error('removeItem(): empty selected task property', this, this.cmfg('taskManagerSelectedTaskGet'));
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.administration.taskManager.task.event.Synchronous.remove({
				params: params,
				scope: this,
				success: this.success
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 * @private
		 */
		validate: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.cmfg('onTaskManagerFormTaskEventSynchronousValidateSetup', fullValidation);

			return this.callParent(arguments);
		}
	});

})();
