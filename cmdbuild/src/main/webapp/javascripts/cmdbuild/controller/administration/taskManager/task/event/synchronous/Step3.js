(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.synchronous.Step3', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step3}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEventSynchronousStep3FieldsetNotificationExpand',
			'onTaskManagerFormTaskEventSynchronousStep3FieldsetWorkflowExpand',
			'onTaskManagerFormTaskEventSynchronousStep3ValidateSetup = onTaskManagerFormTaskEventSynchronousValidateSetup'
		],

		/**
		 * @cfg {CMDBuild.view.administration.taskManager.task.event.synchronous.Step3View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.event.synchronous.Synchronous} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.event.synchronous.Step3View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousStep3FieldsetNotificationExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetNotification });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousStep3FieldsetWorkflowExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetWorkflow });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousStep3ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			// Notification validation
			this.view.fieldNotificationAccount.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());
			this.view.fieldNotificationTemplate.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());

			// Workflow validation
			this.view.fieldWorkflow.fieldCombo.allowBlank = !(fullValidation && this.view.fieldsetWorkflow.checkboxCmp.getValue());
		}
	});

})();
