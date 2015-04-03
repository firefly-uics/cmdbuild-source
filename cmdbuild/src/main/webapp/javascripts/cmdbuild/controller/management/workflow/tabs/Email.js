(function () {

	/**
	 * Workflow specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.workflow.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController or CMModWorkflowController
		 * @param {Mixed} configObject.selectedEntity - Card or Activity in edit
		 * @param {Mixed} configObject.ownerEntityobject - card or activity
		 * @param {Mixed} configObject.widgetConf
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {CMDBuild.model.CMActivityInstance} activityIstance
		 */
		onActivityInstanceChange: Ext.emptyFn,

		onAddCardButtonClick: Ext.emptyFn,

		onCardSelected: Ext.emptyFn,

		onCloneCard: function() {
			if (this.view)
				this.view.setDisabled(true);
		},

		onEntryTypeSelected: Ext.emptyFn,

		/**
		 * Initialize tab to apply all events on form fields
		 */
		onModifyCardClick: function() {
			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true, true);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * TODO: aka onEntryTypeSelected
		 */
		onProcessClassRefChange: function(entryType) {
_debug('onProcessInstanceChange entryType', entryType);
			if (this.view)
				this.view.setDisabled(true);
		},

		/**
		 * @param {CMDBuild.model.CMProcessInstance} processIstance
		 *
		 * TODO: aka onCardSelected
		 */
		onProcessInstanceChange: function(processIstance) {
_debug('onProcessInstanceChange entryType', processIstance);
			this.selectedEntity = processIstance;

			this.controllerGrid.storeLoad();

			// TODO: Enable/Disable tab with server call response
			if (this.view)
				this.view.setDisabled(false);
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.flagPerformSaveAction = true;

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true);
		}
	});

})();