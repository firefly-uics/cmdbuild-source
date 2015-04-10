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
				this.configurationReset();

				this.selectedEntitySet(processIstance, function() {
					me.regenerateAllEmailsSet(processIstance.isNew());
					me.forceRegenerationSet(processIstance.isNew());
					me.cmfg('storeLoad');
				});

				if (!Ext.isEmpty(this.view))
					this.editModeSet(processIstance.isNew()); // Enable/Disable tab based on model new state to separate create/view mode
			} else {
				_msg('ERROR CMDBuild.controller.management.workflow.tabs.Email: empty processIstance on onProcessInstanceChange');
			}
		},

		onSaveCardClick: Ext.emptyFn
	});

})();