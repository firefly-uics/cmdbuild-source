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
		 * @cfg {CMDBuild.controller.management.classes.CMModCardController}
		 */
		parentDelegate: undefined,

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
		 * @param {Mixed} configObject.parentDelegate - CMModCardController
		 * @param {Mixed} configObject.selectedEntity - Card in edit
		 * @param {Mixed} configObject.clientForm
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			// View build
			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.EmailPanel', {
				delegate: this
			});

			this.view.add(this.grid);

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

			if (!Ext.isEmpty(this.view))
				this.mon(this.view, 'destroy', function(view) {
					_CMCardModuleState.removeDelegate(me.cardStateDelegate);

					delete this.cardStateDelegate;
				}, this);
		},

		onAbortCardClick: function() {
			this.setEditMode(false);
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onCardSelect event
		 */
		onAddCardButtonClick: function() {
			this.setEditMode(true);
			this.controllerGrid.setUiState();

			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			this.selectedEntitySet(card);

			this.controllerGrid.storeLoad();

			// TODO: Enable/Disable tab with server call response
			if (!Ext.isEmpty(this.view)) {
				this.view.setDisabled(false);
				this.setEditMode(Ext.isEmpty(card)); // Enable/Disable tab based on model state to separate create/view mode
			}
		},

		onCloneCard: function() {
			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} dc
		 * @param {Object} filter
		 */
		onEntryTypeSelected: function(entryType, dc, filter) {
			this.entryType = entryType;

			this.setEditMode(false);
		},

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
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.flagPerformSaveAction = true;

			if (!this.grid.getStore().isLoading())
				this.controllerGrid.storeLoad(true);
		}
	});

})();