(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step4', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEventAsynchronousStep4FieldsetNotificationExpand',
			'onTaskManagerFormTaskEventAsynchronousStep4ValidateSetup = onTaskManagerFormTaskEventAsynchronousValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.event.asynchronous.Step4View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.event.asynchronous.Step4View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep4FieldsetNotificationExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetNotification });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep4ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldNotificationAccount.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());
			this.view.fieldNotificationTemplate.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());
		}
	});

})();
