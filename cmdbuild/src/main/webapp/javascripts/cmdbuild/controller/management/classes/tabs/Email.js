(function () {

	/**
	 * Classes specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		requires: ['CMDBuild.model.classes.tabs.email.Email'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * Card actually selected
		 *
		 * @cfg {Ext.data.Model}
		 */
		selectedEntity: undefined,

		/**
		 * Model class used for email records and stores
		 *
		 * @cfg {CMDBuild.model.classes.tabs.email.Email}
		 */
		modelEmail: 'CMDBuild.model.classes.tabs.email.Email',

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController or CMModWorkflowController
		 * @param {Ext.data.Model} configObject.selectedEntity - Card or Activity in edit
		 * @param {Mixed} configObject.ownerEntityobject - card or activity
		 * @param {Mixed} configObject.widgetConf
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.buildCardModuleStateDelegate();
		},

		buildCardModuleStateDelegate: function() {
			var me = this;

			this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();

			this.cardStateDelegate.onEntryTypeDidChange = function(state, entryType) {
				me.onEntryTypeSelected(entryType);
			};

			this.cardStateDelegate.onCardDidChange = function(state, card) {
				Ext.suspendLayouts();
				me.onCardSelected(card);
				Ext.resumeLayouts();
			};

			_CMCardModuleState.addDelegate(this.cardStateDelegate);

			if (this.view)
				this.mon(this.view, 'destroy', function(view) {
					_CMCardModuleState.removeDelegate(me.cardStateDelegate);

					delete me.cardStateDelegate;
				}, this);
		},

		onAddCardButtonClick: function() {
			if (this.view)
				this.view.setDisabled(true);
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			this.selectedEntity = card;

			this.controllerGrid.storeLoad();

			// TODO: Enable/Disable tab with server call response
			if (this.view)
				this.view.setDisabled(false);
		},

		onCloneCard: function() {
			if (this.view)
				this.view.setDisabled(true);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} dc
		 * @param {Object} filter
		 */
		onEntryTypeSelected: function(entryType, dc, filter) {
			this.entryType = entryType;
		},

		/**
		 * Initialize tab to apply all events on form fields
		 */
		onModifyCardClick: function() {
			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true, true);
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