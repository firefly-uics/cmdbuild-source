(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Step4', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Connector'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Connector}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskConnectorStep4Show = onTaskManagerCommonFieldEditableRowRemove',
			'onTaskManagerFormTaskConnectorStep4ValidateSetup = onTaskManagerFormTaskConnectorValidateSetup',
			'onTaskManagerFormTaskConnectorStep4ValueGet'
		],

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.Step4View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.connector.Step4View', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
		},

		/**
		 * Enable/Disable wizard next button based on grid validity
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep4Show: function () {
			this.grid.completeEdit();

			this.cmfg('taskManagerFormNavigationSetDisableNextButton', Ext.isEmpty(this.grid.getValue()));
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep4ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.grid.allowBlank = !fullValidation;
		},

		/**
		 * @returns {Array}
		 */
		onTaskManagerFormTaskConnectorStep4ValueGet: function () {
			return this.grid.getValue();
		}
	});

})();
