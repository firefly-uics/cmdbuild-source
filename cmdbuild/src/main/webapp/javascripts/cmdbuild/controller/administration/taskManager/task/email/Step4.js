(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.email.Step4', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEmailStep4FieldsetWorkflowExpand',
			'onTaskManagerFormTaskEmailStep4ValidateSetup = onTaskManagerFormTaskEmailValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.email.Step4View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.email.Email} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.email.Step4View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep4FieldsetWorkflowExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetWorkflow });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep4ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldWorkflow.fieldCombo.allowBlank = !(fullValidation && this.view.fieldsetWorkflow.checkboxCmp.getValue());
		}
	});

})();
