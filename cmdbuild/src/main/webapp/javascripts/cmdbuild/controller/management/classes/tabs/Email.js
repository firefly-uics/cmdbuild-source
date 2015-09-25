(function () {

	/**
	 * Classes specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.common.tabs.email.Email'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @property {Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {CMDBuild.state.CMCardModuleStateDelegate}
		 */
		cardStateDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.classes.CMModCardController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			// View build
			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.EmailView', {
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
			this.editModeSet(true);
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			var me = this;

			this.card = card;

			this.configuration.readOnly = false; // TODO: fix evaluating functionalities
			this.editModeSet(true);

			this.selectedEntitySet(this.card, function() {
				me.regenerateAllEmailsSet(Ext.isEmpty(this.card));
				me.forceRegenerationSet(Ext.isEmpty(this.card));
				me.cmfg('onEmailPanelShow');
			});
		},

		onCloneCard: function() {
			var me = this;

			this.card = null;

			this.configuration.readOnly = false; // TODO: fix evaluating functionalities
			this.editModeSet(true);

			this.selectedEntitySet(this.card, function() {
				me.regenerateAllEmailsSet(Ext.isEmpty(this.card));
				me.forceRegenerationSet(Ext.isEmpty(this.card));
				me.cmfg('onEmailPanelShow');
			});
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} dc
		 * @param {Object} filter
		 */
		onEntryTypeSelected: function(entryType, dc, filter) {
			this.entryType = entryType;

			this.configuration.readOnly = false; // TODO: fix evaluating functionalities
			this.editModeSet(true);
		},

		/**
		 * Works in place of ManageEmail widget for Workflows
		 *
		 * @override
		 */
		onModifyCardClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get('IdClass'));
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.proxy.common.tabs.email.Email.isEmailEnabledForCard({
				params: params,
				scope: this,
				loadMask: true,
				failure: function(response, options, decodedResponse) {
					_warning('Emails enabled for card (' + this.card.get(CMDBuild.core.constants.Proxy.ID) + ') unknown', this);
				},
				success: function(response, options, decodedResponse) {
					this.cmfg('configurationSet', {
						readOnly: !decodedResponse.response
					});
				}
			});

			this.callParent(arguments);
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.cmfg('sendAllOnSaveSet', true);

			if (!this.grid.getStore().isLoading()) {
				this.regenerateAllEmailsSet(true);
				this.cmfg('onEmailPanelShow');
			}
		}
	});

})();