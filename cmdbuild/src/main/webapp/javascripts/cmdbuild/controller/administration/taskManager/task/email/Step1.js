(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.email.Step1', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEmailStep1FieldsetRejectedExpand',
			'onTaskManagerFormTaskEmailStep1FieldTypeComboChange',
			'onTaskManagerFormTaskEmailStep1ValidateSetup = onTaskManagerFormTaskEmailValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.email.step1.Step1View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.email.step1.Step1View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep1FieldsetRejectedExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetRejected });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep1FieldTypeComboChange: function () {
			var container = this.view.fieldsetFilter.wrapper;

			switch (this.view.fieldsetFilter.fieldFilterType.getValue()) {
				case CMDBuild.core.constants.Proxy.REGEX:
					return container.getLayout().setActiveItem(1);

				case CMDBuild.core.constants.Proxy.FUNCTION:
					return container.getLayout().setActiveItem(2);

				case CMDBuild.core.constants.Proxy.NONE:
				default:
					return container.getLayout().setActiveItem(0);
			}
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep1ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.view.fieldEmailAccount.allowBlank = !fullValidation;
			this.view.fieldFolderIncoming.allowBlank = !fullValidation;
			this.view.fieldFolderProcessed.allowBlank = !fullValidation;
			this.view.fieldFolderRejected.allowBlank = !(fullValidation && this.view.fieldsetRejected.checkboxCmp.getValue());
		}
	});

})();
