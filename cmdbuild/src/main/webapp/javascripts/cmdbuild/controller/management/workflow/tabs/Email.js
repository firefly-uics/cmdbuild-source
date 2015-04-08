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
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		controllerGrid: undefined,

		/**
		 * Flag to mark when performing save action
		 *
		 * @cfg {Boolean}
		 */
		flagPerformSaveAction: false,

		/**
		 * Shorthand to view grid
		 *
		 * @property {CMDBuild.view.management.common.tabs.email.GridPanel}
		 */
		grid: undefined,

		/**
		 * Actually selected activity
		 *
		 * @cfg {CMDBuild.model.common.tabs.email.SelectedEntity}
		 */
		selectedEntity: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.tabs.Email}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModWorkflowController
		 * @param {Mixed} configObject.selectedEntity - Activity in edit
		 * @param {Mixed} configObject.clientForm
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			// View build
			this.view = Ext.create('CMDBuild.view.management.workflow.tabs.Email', {
				delegate: this
			});

			this.view.add(this.grid);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {CMDBuild.model.CMActivityInstance} activityIstance
		 */
		onActivityInstanceChange: Ext.emptyFn,

		onAbortCardClick: function() {
			this.setEditMode(false);
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onProcessInstanceChange event
		 */
		onAddCardButtonClick: function() {
			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		onCardSelected: Ext.emptyFn,

		onCloneCard: function() {
			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		onEntryTypeSelected: Ext.emptyFn,

		/**
		 * Initialize tab to apply all events on form fields
		 *
		 * @override
		 */
		onModifyCardClick: function() {
			this.callParent(arguments);

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true, true);
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.setEditMode(false);

			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		/**
		 * Equals to onCardSelected in classes.
		 * N.B. Enable/Disable email tab is done by widget configurationSet
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processIstance
		 */
		onProcessInstanceChange: function(processIstance) {
			if (!Ext.isEmpty(processIstance)) {
				this.configurationReset();
				this.setSelectedEntity(processIstance);

				this.controllerGrid.storeLoad();

				if (!Ext.isEmpty(this.view))
					this.setEditMode(processIstance.isNew()); // Enable/Disable tab based on model new state to separate create/view mode
			} else {
				_msg('ERROR CMDBuild.controller.management.workflow.tabs.Email: empty processIstance on onProcessInstanceChange');
			}
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