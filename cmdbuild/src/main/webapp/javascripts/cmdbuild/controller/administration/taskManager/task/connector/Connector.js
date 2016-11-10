(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Connector', {
		extend: 'CMDBuild.controller.administration.taskManager.task.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.administration.taskManager.task.Connector'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.Form}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskAbortButtonClick',
			'onTaskManagerFormTaskAddButtonClick',
			'onTaskManagerFormTaskCloneButtonClick',
			'onTaskManagerFormTaskConnectorRowSelected = onTaskManagerFormTaskRowSelected',
			'onTaskManagerFormTaskConnectorSaveButtonClick = onTaskManagerFormTaskSaveButtonClick',
			'onTaskManagerFormTaskModifyButtonClick',
			'onTaskManagerFormTaskRemoveButtonClick',
			'onTaskManagerFormTaskConnectorValidateSetup -> controllerStep1, controllerStep2, controllerStep3, controllerStep4, controllerStep5',
			'taskManagerFormTaskConnectorClassesStoreGet',
			'taskManagerFormTaskConnectorExternalEntityStoreGet',
		],

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.connector.Step1}
		 */
		controllerStep1: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.connector.Step2}
		 */
		controllerStep2: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.connector.Step3}
		 */
		controllerStep3: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.connector.Step4}
		 */
		controllerStep4: undefined,

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.connector.Step5}
		 */
		controllerStep5: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.Form} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Build sub controllers
			this.controllerStep1 = Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Step1', { parentDelegate: this });
			this.controllerStep2 = Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Step2', { parentDelegate: this });
			this.controllerStep3 = Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Step3', { parentDelegate: this });
			this.controllerStep4 = Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Step4', { parentDelegate: this });
			this.controllerStep5 = Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Step5', { parentDelegate: this });

			this.cmfg('taskManagerFormPanelsAdd', [
				this.controllerStep1.getView(),
				this.controllerStep2.getView(),
				this.controllerStep3.getView(),
				this.controllerStep4.getView(),
				this.controllerStep5.getView()
			]);
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onTaskManagerFormTaskConnectorRowSelected: function () {
			if (!this.cmfg('taskManagerSelectedTaskIsEmpty')) {
				this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({
					forceToolbarBottomState: true,
					forceToolbarTopState: true,
					state: false
				});

				this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');

				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.administration.taskManager.task.Connector.read({
					params: params,
					scope: this,
					success: function (rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var record = Ext.create('CMDBuild.model.administration.taskManager.task.connector.Connector', decodedResponse);

							this.cmfg('taskManagerFormViewGet').loadRecord(record);

							this.controllerStep3.cmfg('onTaskManagerFormTaskConnectorStep3ValueSet', record.get(CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION));

							this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: false });

							this.onTaskManagerFormTaskRowSelected(arguments); // CallParent alias
						} else {
							_error('onTaskManagerFormTaskConnectorRowSelected(): unmanaged response', this, decodedResponse);
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
		onTaskManagerFormTaskConnectorSaveButtonClick: function () {
			var formData = Ext.create('CMDBuild.model.administration.taskManager.task.connector.Connector', this.cmfg('taskManagerFormViewDataGet'));
			formData.set(CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION, this.controllerStep3.cmfg('onTaskManagerFormTaskConnectorStep3ValueGet'));

			// Validate before save
			if (this.validate(formData.get(CMDBuild.core.constants.Proxy.ACTIVE)))
				if (this.cmfg('taskManagerSelectedTaskIsEmpty')) {
					CMDBuild.proxy.administration.taskManager.task.Connector.create({
						params: formData.getSubmitData('create'),
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.administration.taskManager.task.Connector.update({
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

			CMDBuild.proxy.administration.taskManager.task.Connector.remove({
				params: params,
				scope: this,
				success: this.success
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		taskManagerFormTaskConnectorClassesStoreGet: function () {
			var data = [],
				stepMappingClassData = this.controllerStep4.cmfg('onTaskManagerFormTaskConnectorStep4ValueGet');

			Ext.Array.forEach(stepMappingClassData, function (rowObject, i, allRowObjects) {
				if (Ext.isObject(rowObject) && !Ext.Object.isEmpty(rowObject))
					data.push([rowObject[CMDBuild.core.constants.Proxy.CLASS_NAME]]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME],
				data: data,
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		taskManagerFormTaskConnectorExternalEntityStoreGet: function () {
			var data = [],
				stepMappingClassData = this.controllerStep4.cmfg('onTaskManagerFormTaskConnectorStep4ValueGet');

			Ext.Array.forEach(stepMappingClassData, function (rowObject, i, allRowObjects) {
				if (Ext.isObject(rowObject) && !Ext.Object.isEmpty(rowObject))
					data.push([rowObject[CMDBuild.core.constants.Proxy.SOURCE_NAME]]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME],
				data: data,
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
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

			this.cmfg('onTaskManagerFormTaskConnectorValidateSetup', fullValidation);

			return this.callParent(arguments);
		}
	});

})();
