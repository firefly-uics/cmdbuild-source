(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous', {
		extend: 'CMDBuild.controller.administration.taskManager.task.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.event.Asynchronous'
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
			'onTaskManagerFormTaskEventAsynchronousEntryTypeSelected',
			'onTaskManagerFormTaskEventAsynchronousRowSelected = onTaskManagerFormTaskRowSelected',
			'onTaskManagerFormTaskEventAsynchronousSaveButtonClick = onTaskManagerFormTaskSaveButtonClick',
			'onTaskManagerFormTaskEventAsynchronousValidateSetup -> controllerStep3, controllerStep4',
			'onTaskManagerFormTaskModifyButtonClick',
			'onTaskManagerFormTaskRemoveButtonClick'
		],

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step1}
		 */
		controllerStep1: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step2}
		 */
		controllerStep2: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step3}
		 */
		controllerStep3: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step4}
		 */
		controllerStep4: undefined,

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
			this.controllerStep1 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step1', { parentDelegate: this });
			this.controllerStep2 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step2', { parentDelegate: this });
			this.controllerStep3 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step3', { parentDelegate: this });
			this.controllerStep4 = Ext.create('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step4', { parentDelegate: this });

			this.cmfg('taskManagerFormPanelsAdd', [
				this.controllerStep1.getView(),
				this.controllerStep2.getView(),
				this.controllerStep3.getView(),
				this.controllerStep4.getView()
			]);
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousEntryTypeSelected: function () {
			this.controllerStep1.cmfg('onTaskManagerFormTaskEventAsynchronousStep1EntryTypeSelected');
			this.controllerStep2.cmfg('onTaskManagerFormTaskEventAsynchronousStep2EntryTypeSelected');
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onTaskManagerFormTaskEventAsynchronousRowSelected: function () {
			if (!this.cmfg('taskManagerSelectedTaskIsEmpty')) {
				this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({
					forceToolbarBottomState: true,
					forceToolbarTopState: true,
					state: false
				});

				this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');

				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.administration.taskManager.task.event.Asynchronous.read({
					params: params,
					scope: this,
					success: function (rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							if (Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]))
								decodedResponse[CMDBuild.core.constants.Proxy.FILTER] = Ext.decode(decodedResponse[CMDBuild.core.constants.Proxy.FILTER]);

							// Is needed to setup filter field before load record values
							this.controllerStep2.cmfg('onTaskManagerFormTaskEventAsynchronousStep2EntryTypeSelected', {
								className: decodedResponse[CMDBuild.core.constants.Proxy.CLASS_NAME],
								scope: this,
								callback: function () {
									this.cmfg('taskManagerFormViewGet').loadRecord(
										Ext.create('CMDBuild.model.administration.taskManager.task.event.asynchronous.Asynchronous', decodedResponse)
									);

									this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: false });

									this.onTaskManagerFormTaskRowSelected(); // CallParent alias
								}
							});
						} else {
							_error('onTaskManagerFormTaskEventAsynchronousRowSelected(): unmanaged response', this, decodedResponse);
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
		onTaskManagerFormTaskEventAsynchronousSaveButtonClick: function () {
			var formData = Ext.create('CMDBuild.model.administration.taskManager.task.event.asynchronous.Asynchronous', this.cmfg('taskManagerFormViewDataGet'));

			// Validate before save
			if (this.validate(formData.get(CMDBuild.core.constants.Proxy.ACTIVE)))
				if (this.cmfg('taskManagerSelectedTaskIsEmpty')) {
					CMDBuild.proxy.administration.taskManager.task.event.Asynchronous.create({
						params: formData.getSubmitData('create'),
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.administration.taskManager.task.event.Asynchronous.update({
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

			CMDBuild.proxy.administration.taskManager.task.event.Asynchronous.remove({
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

			this.cmfg('onTaskManagerFormTaskEventAsynchronousValidateSetup', fullValidation);

			return this.callParent(arguments);
		}
	});

})();
