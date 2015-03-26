(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Main', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyWorkflow',
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @cfg {Array}
		 */
		subSections: [
			'generalOptions', // Default
			'alfresco',
			'bim',
			'gis',
			'relationGraph',
			'server',
			'workflow'
		],

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @property {Mixed}
		 */
		view: undefined,

		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view.delegate = this;
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onConfigurationAbortButtonClick':
					return this.onConfigurationAbortButtonClick();

				case 'onConfigurationClearCacheButtonClick':
					return this.onConfigurationClearCacheButtonClick();

				case 'onConfigurationSaveButtonClick':
					return this.onConfigurationSaveButtonClick();

				case 'onConfigurationServiceSynchButtonClick':
					return this.onConfigurationServiceSynchButtonClick();

				case 'onConfigurationUnlockCardsButtonClick':
					return this.onConfigurationUnlockCardsButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onConfigurationAbortButtonClick: function() {
			this.readConfiguration();
		},

		onConfigurationClearCacheButtonClick: function() {
			CMDBuild.core.proxy.Utils.clearCache({
				success: CMDBuild.Msg.success
			});
		},

		onConfigurationSaveButtonClick: function() {
			var params = this.sectionView.getValues();
			params['enabled_languages'] = Ext.encode(this.sectionView.languageGrid.getValue()); // TODO: to delete when localization module will be released

			CMDBuild.core.proxy.Configuration.save({
				scope: this,
				params: params,
				success: function(result, options, decodedResult) {
					this.readConfiguration();

					CMDBuild.Msg.success();
				}
			}, this.sectionView.configFileName);
		},

		onConfigurationServiceSynchButtonClick: function() {
			CMDBuild.core.proxy.CMProxyWorkflow.synchronize({
				success: CMDBuild.Msg.success
			});
		},

		onConfigurationUnlockCardsButtonClick: function() {
			CMDBuild.core.proxy.Card.unlockAllCards({
				success: CMDBuild.Msg.success
			});
		},

		/**
		 * Setup view items on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
_debug('parameters', parameters);
			if (!Ext.Object.isEmpty(parameters)) {
				var subSection = Ext.Array.contains(this.subSections, parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					? parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID) : this.subSections[0];

				this.view.removeAll(true);

				switch(subSection) {
					case 'alfresco': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.AlfrescoPanel', {
							delegate: this
						});
					} break;

					case 'bim': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.BimPanel', {
							delegate: this
						});
					} break;

					case 'gis': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.GisPanel', {
							delegate: this
						});
					} break;

					case 'relationGraph': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.RelationGraphPanel', {
							delegate: this
						});
					} break;

					case 'server': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.ServerPanel', {
							delegate: this
						});
					} break;

					case 'workflow': {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.WorkflowPanel', {
							delegate: this
						});
					} break;

					case 'generalOptions':
					default: {
						this.sectionView = Ext.create('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
							delegate: this
						});
					}
				}

				this.view.add(this.sectionView);

				this.readConfiguration();

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				_CMCache.initModifyingTranslations();

				this.callParent(arguments);
			}
		},

		readConfiguration: function() {
			CMDBuild.core.proxy.Configuration.read({
				scope: this,
				success: function(result, options, decodedResult){
					var decodedResult = decodedResult.data;

					_CMCache.setActiveTranslations(decodedResult.enabled_languages);

					this.sectionView.getForm().setValues(decodedResult);

					// TODO: to delete when localization module will be released
					if (this.sectionView.configFileName == 'cmdbuild')
						this.sectionView.languageGrid.setValue(Ext.decode(decodedResult.enabled_languages));

					if (typeof this.sectionView.afterSubmit == 'function')
						this.sectionView.afterSubmit(decodedResult);
				}
			}, this.sectionView.configFileName);
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (!Ext.isEmpty(titlePart))
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
		}
	});

})();