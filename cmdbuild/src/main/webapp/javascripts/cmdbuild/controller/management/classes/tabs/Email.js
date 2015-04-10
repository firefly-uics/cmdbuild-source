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
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

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
			this.editModeSet(false);
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onCardSelect event
		 *
		 * @override
		 */
		onAddCardButtonClick: function() {
			this.callParent(arguments);

			if (!Ext.isEmpty(this.view))
				this.view.setDisabled(true);
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			var me = this;

			this.selectedEntitySet(card, function() {
				me.regenerateAllEmailsSet(Ext.isEmpty(card));
				me.forceRegenerationSet(Ext.isEmpty(card));
				me.cmfg('storeLoad');
			});

			// TODO: Enable/Disable tab with server call response
			if (!Ext.isEmpty(this.view)) {
				this.view.setDisabled(false);
				this.editModeSet(Ext.isEmpty(card)); // Enable/Disable tab based on model state to separate create/view mode
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

			this.editModeSet(false);
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.cmfg('sendAllOnSaveSet', true);

			if (!this.grid.getStore().isLoading()) {
				this.regenerateAllEmailsSet(true);
				this.cmfg('storeLoad');
			}
		}
	});

})();