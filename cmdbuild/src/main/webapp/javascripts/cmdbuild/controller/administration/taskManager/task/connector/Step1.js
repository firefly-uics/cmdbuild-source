(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Step1', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Connector}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskConnectorStep1FieldsetNotificationExpand',
			'onTaskManagerFormTaskConnectorStep1ValidateSetup = onTaskManagerFormTaskConnectorValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.Step1View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.connector.Step1View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep1FieldsetNotificationExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetNotification });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep1ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldNotificationAccount.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());
			this.view.fieldNotificationTemplate.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());
		}
	});

})();
