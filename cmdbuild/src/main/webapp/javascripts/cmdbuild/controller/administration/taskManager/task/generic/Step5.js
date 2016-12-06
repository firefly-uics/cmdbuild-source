(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.generic.Step5', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.generic.Generic}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskGenericStep5FieldsetReportExpand',
			'onTaskManagerFormTaskGenericStep5ValidateSetup = onTaskManagerFormTaskGenericValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.generic.Step5View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.generic.Generic} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.generic.Step5View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskGenericStep5FieldsetReportExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetReport });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskGenericStep5ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldReport.combo.allowBlank = !(fullValidation && this.view.fieldsetReport.checkboxCmp.getValue());
			this.view.fieldReport.extension.allowBlank = !(fullValidation && this.view.fieldsetReport.checkboxCmp.getValue());
		}
	});

})();
