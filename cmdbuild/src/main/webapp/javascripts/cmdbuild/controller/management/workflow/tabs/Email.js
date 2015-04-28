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
		 * @param {Mixed} configObject.parentDelegate - CMModWorkflowController
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
			this.editModeSet(false);
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onProcessInstanceChange event
		 *
		 * @override
		 */
		onAddCardButtonClick: function() {
			this.callParent(arguments);

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
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.editModeSet(false);

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
			var me = this;

			if (!Ext.isEmpty(processIstance)) {
				if (!processIstance.isNew())
					this.parentDelegate.activityPanelController.ensureEditPanel(); // Creates editPanel with relative form fields

				// Reset configuration attributes
				this.configurationSet();
				this.configurationTemplatesSet();

				this.selectedEntitySet(processIstance, function() {
					me.regenerateAllEmailsSet(processIstance.isNew());
					me.forceRegenerationSet(processIstance.isNew());
					me.cmfg('storeLoad');
				});

				this.editModeSet(processIstance.isNew()); // Enable/Disable tab based on model new state to separate create/view mode
				this.cmfg('setUiState');
			} else {
				_error('Empty processIstance on onProcessInstanceChange', this);
			}
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			if (!this.grid.getStore().isLoading()) {
				this.regenerateAllEmailsSet(true);
				this.cmfg('storeLoad');
			}
		}
	});

})();