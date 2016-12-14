(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.event.synchronous.Step1', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.synchronous.Synchronous}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskEventSynchronousStep1EntryTypeSelected',
			'onTaskManagerFormTaskEventSynchronousStep1Show'
		],

		/**
		 * @cfg {CMDBuild.view.administration.taskManager.task.event.synchronous.Step1View}
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

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.event.synchronous.Step1View', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousStep1EntryTypeSelected: function () {
			this.cmfg('taskManagerFormNavigationSetDisableNextButton', false);
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskEventSynchronousStep1Show: function () {
			this.cmfg('taskManagerFormNavigationSetDisableNextButton', Ext.isEmpty(this.view.fieldEntryType.getValue()));
		}
	});

})();
