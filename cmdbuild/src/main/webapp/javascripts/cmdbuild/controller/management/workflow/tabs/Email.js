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
		 * @param {Mixed} configObject.parentDelegate - CMModCardController or CMModWorkflowController
		 * @param {Mixed} configObject.selectedEntity - Activity in edit
		 * @param {Mixed} configObject.ownerEntityobject - card or activity
		 * @param {Mixed} configObject.widgetConf
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

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onProcessInstanceChange event
		 */
		onAddCardButtonClick: function() { // TODO da fare in modo che non si disabiliti nel caso ci sia il widget configurato
_debug('tab onAddCardButtonClick', this.view);
			if (this.view)
				this.view.setDisabled(true);
		},

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
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
_debug('tab onProcessClassRefChange entryType', entryType);
			if (this.view)
				this.view.setDisabled(true);
		},

		/**
		 * Equals to onCardSelected in classes
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processIstance
		 */
		onProcessInstanceChange: function(processIstance) {
_debug('tab onProcessInstanceChange entryType', processIstance);
			this.setSelectedEntity(processIstance);

			this.controllerGrid.storeLoad();

			// TODO: Enable/Disable checking configuration for widget (getConfiguration)
			if (this.view && !processIstance.isNew())
				this.view.setDisabled(false);
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
_debug('tab onSaveCardClick');
			this.flagPerformSaveAction = true;

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true);
		}
	});

})();