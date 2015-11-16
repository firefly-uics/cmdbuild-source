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

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.EmailView', { delegate: this });
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
			this.cmfg('tabEmailEditModeSet', true);
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			this.card = card;

			this.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
				value: false
			});

			this.cmfg('tabEmailEditModeSet', true);

			this.cmfg('tabEmailSelectedEntitySet', {
				selectedEntity: this.card,
				scope: this,
				callbackFunction: function(options, success, response) {
					this.cmfg('tabEmailRegenerateAllEmailsSet', Ext.isEmpty(this.card));
					this.forceRegenerationSet(Ext.isEmpty(this.card));
					this.cmfg('onTabEmailPanelShow');
				}
			});
		},

		onCloneCard: function() {
			this.card = null;

			this.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
				value: false
			});

			this.cmfg('tabEmailEditModeSet', true);

			this.cmfg('tabEmailSelectedEntitySet', {
				selectedEntity: this.card,
				scope: this,
				callbackFunction: function(options, success, response) {
					this.cmfg('tabEmailRegenerateAllEmailsSet', Ext.isEmpty(this.card));
					this.forceRegenerationSet(Ext.isEmpty(this.card));
					this.cmfg('onTabEmailPanelShow');
				}
			});
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} dc
		 * @param {Object} filter
		 */
		onEntryTypeSelected: function(entryType, dc, filter) {
			this.entryType = entryType;

			this.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
				value: false
			});

			this.cmfg('tabEmailEditModeSet', true);
		},

		/**
		 * Works in place of ManageEmail widget for Workflows
		 *
		 * @override
		 */
		onTabEmailModifyCardClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get('IdClass'));
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.proxy.common.tabs.email.Email.isEmailEnabledForCard({
				params: params,
				scope: this,
				loadMask: true,
				success: function(response, options, decodedResponse) {
					this.cmfg('tabEmailConfigurationSet', {
						propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
						value: !decodedResponse.response
					});
				}
			});

			this.callParent(arguments);
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.cmfg('tabEmailSendAllOnSaveSet', true);

			if (!this.grid.getStore().isLoading()) {
				this.cmfg('tabEmailRegenerateAllEmailsSet', true);
				this.cmfg('onTabEmailPanelShow');
			}
		}
	});

})();