(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.email.Step3', {
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
			'onTaskManagerFormTaskEmailStep3FieldsetAttachmentsExpand',
			'onTaskManagerFormTaskEmailStep3FieldsetNotificationExpand',
			'onTaskManagerFormTaskEmailStep3FieldsetParsingExpand',
			'onTaskManagerFormTaskEmailStep3ValidateSetup = onTaskManagerFormTaskEmailValidateSetup'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.email.Step3View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.email.Step3View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep3FieldsetAttachmentsExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetAttachments });

			// Read CMDBuild's alfresco configuration from server and set Combobox store
			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.SHORT] = true;
			params[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ALFRESCO_LOOKUP_CATEGORY);

			this.view.fieldAttachmentsCategory.getStore().load({ params: params });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep3FieldsetNotificationExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetNotification });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep3FieldsetParsingExpand: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionReset({ target: this.view.fieldsetParsing });
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEmailStep3ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			// Parsing fieldset setup
			this.view.fieldParsingKeyStart.allowBlank = !(fullValidation && this.view.fieldsetParsing.checkboxCmp.getValue());
			this.view.fieldParsingKeyEnd.allowBlank = !(fullValidation && this.view.fieldsetParsing.checkboxCmp.getValue());
			this.view.fieldParsingValueStart.allowBlank = !(fullValidation && this.view.fieldsetParsing.checkboxCmp.getValue());
			this.view.fieldParsingValueEnd.allowBlank = !(fullValidation && this.view.fieldsetParsing.checkboxCmp.getValue());

			// Notification fieldset setup
			this.view.fieldNotificationTemplate.allowBlank = !(fullValidation && this.view.fieldsetNotification.checkboxCmp.getValue());

			// Attachments fieldset setup
			this.view.fieldAttachmentsCategory.allowBlank = !(fullValidation && this.view.fieldsetAttachments.checkboxCmp.getValue());
		}
	});

})();
