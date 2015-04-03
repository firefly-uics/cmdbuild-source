(function () {

	/**
	 * Classes specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		controllerGrid: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

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
		 * Witch actually selected card
		 *
		 * @cfg {CMDBuild.model.common.tabs.email.SelectedEntity}
		 */
		selectedEntity: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController or CMModWorkflowController
		 * @param {CMDBuild.model.common.tabs.email.SelectedEntity} configObject.selectedEntity - Card in edit
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

					delete this.cardStateDelegate;
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
_debug('onCardSelected', card);
			this.setSelectedEntity(card);

			this.controllerGrid.storeLoad();

			// TODO: Enable/Disable tab with server call response
			if (this.view && !Ext.isEmpty(card))
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