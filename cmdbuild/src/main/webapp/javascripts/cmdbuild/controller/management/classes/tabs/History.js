(function () {

	/**
	 * Classes specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.tabs.history.Classes'
		],

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
		 * @property {Object}
		 */
		selectedEntity: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.classes.CMModCardController} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.management.classes.tabs.history.GridPanel', {
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
					_CMCardModuleState.removeDelegate(this.cardStateDelegate);

					delete this.cardStateDelegate;
				}, this);
		},

		/**
		 * @return {CMDBuild.core.proxy.common.tabs.history.Classes}
		 *
		 * @override
		 */
		getProxy: function() {
			return CMDBuild.core.proxy.common.tabs.history.Classes;
		},

		/**
		 * @param {Object} card
		 */
		onCardSelected: function(card) {
			this.tabHistorySelectedEntitySet(card);

			if (!Ext.isEmpty(this.entryType) && this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) != 'simpletable') // SimpleTables hasn't history
				this.view.setDisabled(Ext.isEmpty(this.tabHistorySelectedEntityGet()));

			this.cmfg('onHistoryTabPanelShow');
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onEntryTypeSelected: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		},
	});

})();