(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step1', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Asynchronous}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEventAsynchronousStep1EntryTypeSelected',
			'onTaskManagerFormTaskEventAsynchronousStep1Show'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.event.asynchronous.Step1View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.event.asynchronous.Step1View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep1EntryTypeSelected: function () {
			this.cmfg('taskManagerFormNavigationSetDisableNextButton', false);
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventAsynchronousStep1Show: function () {
			this.cmfg('taskManagerFormNavigationSetDisableNextButton', Ext.isEmpty(this.view.fieldEntryType.getValue()));
		}
	});

})();
