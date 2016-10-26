(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Step3', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Connector}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskConnectorStep3DatabaseTypeSelect',
			'onTaskManagerFormTaskConnectorStep3FieldsetDatabaseExpand',
			'onTaskManagerFormTaskConnectorStep3ValidateSetup = onTaskManagerFormTaskConnectorValidateSetup',
			'onTaskManagerFormTaskConnectorStep3ValueGet',
			'onTaskManagerFormTaskConnectorStep3ValueSet'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.step3.Step3View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.connector.Connector} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.connector.step3.Step3View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep3FieldsetDatabaseExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetDatabase });

			this.cmfg('onTaskManagerFormTaskConnectorStep3DatabaseTypeSelect');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep3DatabaseTypeSelect: function () {
			var selectedDatabaseType = this.view.fieldsetDatabase.fieldDatabaseType.getValue();

			this.view.fieldsetDatabase.fieldDatabaseInstanceName.setDisabled(
				!(selectedDatabaseType == CMDBuild.core.constants.Proxy.MYSQL)
			);
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep3ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldsetDatabase.fieldDatabaseAddress.allowBlank = !fullValidation;
			this.view.fieldsetDatabase.fieldDatabaseName.allowBlank = !fullValidation;
			this.view.fieldsetDatabase.fieldDatabasePassword.allowBlank = !fullValidation;

			this.view.fieldsetDatabase.fieldDatabasePort.allowBlank = !fullValidation;
			this.view.fieldsetDatabase.fieldDatabasePort.setMinValue(fullValidation ? 1 : 0);

			this.view.fieldsetDatabase.fieldDatabaseType.allowBlank = !fullValidation;
			this.view.fieldsetDatabase.fieldDatabaseUsername.allowBlank = !fullValidation;
		},

		/**
		 * @returns {Object}
		 */
		onTaskManagerFormTaskConnectorStep3ValueGet: function () {
			return this.cmfg('taskManagerFormViewGet').panelFunctionDataGet({
				includeDisabled: true,
				target: this.view.fieldsetDatabase
			});
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep3ValueSet: function (value) {
			this.cmfg('taskManagerFormViewGet').getForm().setValues(value);
		}
	});

})();
