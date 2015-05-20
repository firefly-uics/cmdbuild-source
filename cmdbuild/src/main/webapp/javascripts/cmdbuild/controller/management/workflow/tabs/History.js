(function () {

	/**
	 * Workflow specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.workflow.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		mixins: {
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.history.GridPanel} grid
		 *
		 * @override
		 */
		addExtraColumnsIfNeeded: function(grid) {
			this.callParent(arguments);

			Ext.apply(grid, {
				columns: grid.columns.concat([
					{
						dataIndex: 'Code', // TODO vedere server + proxy constants
						text: CMDBuild.Translation.management.modcard.history_columns.activity_name,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						flex: 1
					},
					{
						dataIndex: 'Executor', // TODO vedere server + proxy constants
						text: CMDBuild.Translation.management.modcard.history_columns.performer,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						flex: 1
					},
					{
						dataIndex: 'Status', // TODO vedere server + proxy constants
						text: CMDBuild.Translation.status,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						flex: 1
					}
				])
			});
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;

			if (processInstance.isNew()) {
				this.view.disable();
			} else {
				this.view.enable();
				if (this.view.isVisible()) {
					this.load();
				}
			}

		},

		// TODO da eliminare togliendo dal controller comune la roba specifica
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

})();
