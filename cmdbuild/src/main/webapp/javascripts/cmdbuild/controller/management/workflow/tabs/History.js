(function () {

	/**
	 * Processes specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.workflow.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		requires: ['CMDBuild.core.proxy.common.tabs.history.Processes'],

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @property {CMDBuild.model.CMProcessInstance}
		 */
		selectedEntity: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.management.workflow.tabs.history.GridPanel', {
				delegate: this
			});

			this.view.add(this.grid);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @return {Array}
		 *
		 * @override
		 */
		getHistoryGridColumns: function() {
			var processesCustoColumns = [
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_NAME,
					text: CMDBuild.Translation.activityName,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					flex: 1
				},
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.PERFORMERS,
					text: CMDBuild.Translation.activityPerformer,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					flex: 1
				},
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
					text: CMDBuild.Translation.status,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					flex: 1
				}
			];

			return Ext.Array.push(this.callParent(arguments), processesCustoColumns);
		},

		/**
		 * @return {CMDBuild.core.proxy.common.tabs.history.Classes}
		 *
		 * @override
		 */
		getProxy: function() {
			return CMDBuild.core.proxy.common.tabs.history.Processes;
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		},

		/**
		 * Equals to onCardSelected in classes
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processInstance
		 */
		onProcessInstanceChange: function(processInstance) {
			this.selectedEntity = processInstance;

			this.view.setDisabled(processInstance.isNew());

			this.cmfg('onHistoryTabPanelShow');
		}
	});

})();