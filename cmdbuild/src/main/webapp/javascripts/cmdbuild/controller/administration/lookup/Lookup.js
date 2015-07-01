(function() {

	Ext.define('CMDBuild.controller.administration.lookup.Lookup', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.model.lookup.Type'
		],

		/**
		 * @property {CMDBuild.controller.administration.lookup.List}
		 */
		controllerList: undefined,

		/**
		 * @property {CMDBuild.controller.administration.lookup.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLookupAddButtonClick',
			'selectedLookupTypeIsEmpty',
			'selectedLookupTypeGet',
			'selectedLookupTypeSet',
			'selectedLookupSet -> controllerList'
		],

		/**
		 * @property {CMDBuild.model.lookup.Type} or null
		 */
		selectedLookupType: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.LookupView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.lookup.LookupView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view.tabPanel.removeAll();

			// Controller build
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.lookup.Properties', { parentDelegate: this });
			this.controllerList = Ext.create('CMDBuild.controller.administration.lookup.List', { parentDelegate: this });

			// Inject tabs
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerList.getView());
		},

		onLookupAddButtonClick: function() {
			this.selectedLookupTypeSet();

			this.view.tabPanel.setActiveTab(0);

			this.controllerProperties.cmfg('onLookupPropertiesAddButtonClick');
			this.controllerList.getView().disable();
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 */
		onViewOnFront: function(parameters) {
			// Manage tab state
			this.controllerProperties.getView().setDisabled(Ext.isEmpty(parameters));
			this.controllerList.getView().setDisabled(Ext.isEmpty(parameters));

			if (!Ext.isEmpty(parameters)) {
				this.selectedLookupTypeSet({ // TODO: use proxy to read domain (server side implementation)
					description: parameters.get(CMDBuild.core.proxy.Constants.TEXT), // TODO: to fix translating on server
					id: parameters.get(CMDBuild.core.proxy.Constants.ID),
					parent: parameters.get(CMDBuild.core.proxy.Constants.PARENT)
				});

				this.cmfg('selectedLookupSet'); // Reset LookupList tab selection buffer

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.Constants.TEXT));

				if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
					this.view.tabPanel.setActiveTab(0);

				this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
			}
		},

		// SelectedLookupType methods
			/**
			 * @returns {Boolean}
			 */
			selectedLookupTypeIsEmpty: function() {
				return Ext.isEmpty(this.selectedLookupType);
			},

			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.lookup.Type} or Mixed
			 */
			selectedLookupTypeGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedLookupType.get(parameterName);

				return this.selectedLookupType;
			},

			/**
			 * @property {Object} lookupTypeObject
			 */
			selectedLookupTypeSet: function(lookupTypeObject) {
				this.selectedLookupType = null;

				if (!Ext.isEmpty(lookupTypeObject) && Ext.isObject(lookupTypeObject)) {
					if (Ext.getClassName(lookupTypeObject) == 'CMDBuild.model.lookup.Type') {
						this.selectedLookupType = lookupTypeObject;
					} else {
						this.selectedLookupType = Ext.create('CMDBuild.model.lookup.Type', lookupTypeObject);
					}
				}
			}
	});

})();