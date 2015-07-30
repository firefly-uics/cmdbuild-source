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
		 * @property {CMDBuild.view.management.workflow.tabs.Email}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configObject.parentDelegate
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
		 * It's the change of processIstance step (called activity)
		 *
		 * @param {CMDBuild.model.CMActivityInstance} activityIstance
		 */
		onActivityInstanceChange: Ext.emptyFn,

		onAbortCardClick: function() {
			this.editModeSet(false);
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onProcessInstanceChange event
		 *
		 * @override
		 */
		onAddCardButtonClick: function() {
			var me = this;

			this.callParent(arguments);

			 // Reset selected entity, regenerate email and load store
			this.selectedEntitySet(null, function() {
				me.regenerateAllEmailsSet(true);
				me.forceRegenerationSet(true);
				me.cmfg('onEmailPanelShow');
			});
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.editModeSet(false);
		},

		/**
		 * Equals to onCardSelected in classes.
		 * N.B. Enable/Disable email tab is done by widget configurationSet
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processInstance
		 */
		onProcessInstanceChange: function(processInstance) {
			var me = this;

			if (!Ext.isEmpty(processInstance) && processInstance.isStateOpen()) {
				if (!processInstance.isNew())
					this.parentDelegate.activityPanelController.ensureEditPanel(); // Creates editPanel with relative form fields

				this.selectedEntitySet(processInstance, function() {
					me.regenerateAllEmailsSet(processInstance.isNew());
					me.forceRegenerationSet(processInstance.isNew());
					me.cmfg('onEmailPanelShow');
				});

				this.editModeSet(processInstance.isNew()); // Enable/Disable tab based on model new state to separate create/view mode
				this.cmfg('setUiState');
			} else { // We have a closed process instance
				this.selectedEntitySet(processInstance, function() {
					me.cmfg('onEmailPanelShow');
				});
			}
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			if (!this.grid.getStore().isLoading()) {
				this.regenerateAllEmailsSet(true);
				this.cmfg('onEmailPanelShow');
			}
		}
	});

})();