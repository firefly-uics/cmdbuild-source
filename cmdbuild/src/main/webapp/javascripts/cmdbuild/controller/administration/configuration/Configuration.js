(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Configuration', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Configuration'
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
		 * @property {CMDBuild.view.administration.configuration.ConfigurationView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.configuration.ConfigurationView} view
		 */
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
		cmfg: function(name, param, callBack) {
			switch (name) {
				case 'onConfigurationRead':
					return this.onConfigurationRead(param.configFileName, param.view);

				case 'onConfigurationSave':
					return this.onConfigurationSave(param.configFileName, param.view);

				default: {
					if (!Ext.isEmpty(this.parentDelegate) && Ext.isFunction(this.parentDelegate.cmfg))
						return this.parentDelegate.cmfg(name, param, callBack);
				}
			}
		},

		/**
		 * @param {String} configFileName
		 * @param {Mixed} view
		 */
		onConfigurationRead: function(configFileName, view) {
			if (!Ext.isEmpty(configFileName) && !Ext.isEmpty(view)) {
				CMDBuild.core.proxy.Configuration.read({
					scope: this,
					loadMask: true,
					success: function(result, options, decodedResult){
						var decodedResult = decodedResult.data;

						_CMCache.setActiveTranslations(decodedResult.enabled_languages);

						// FIX bug with Firefox that breaks UI on fast configuration page switch
						if (view.isVisible())
							view.getForm().setValues(decodedResult);

						// TODO: to delete when localization module will be released
						if (configFileName == 'cmdbuild')
							view.languageGrid.setValue(decodedResult.enabled_languages.split(', '));

						if (typeof view.afterSubmit == 'function')
							view.afterSubmit(decodedResult);
					}
				}, configFileName);
			}
		},

		/**
		 * @param {String} configFileName
		 * @param {Mixed} view
		 */
		onConfigurationSave: function(configFileName, view) {
			if (!Ext.isEmpty(configFileName) && !Ext.isEmpty(view)) {
				var params = view.getValues();

				// TODO: to delete when localization module will be released
				if (configFileName == 'cmdbuild')
					params['enabled_languages'] = view.languageGrid.getValue().join(', ');

				CMDBuild.core.proxy.Configuration.save({
					scope: this,
					params: params,
					loadMask: true,
					success: function(result, options, decodedResult) {
						this.onConfigurationRead(configFileName, view);

						CMDBuild.Msg.success();
					}
				}, configFileName);
			}
		},

		/**
		 * Setup view items on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID)) {
					case 'alfresco': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Alfresco', { parentDelegate: this });
					} break;

					case 'bim': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Bim', { parentDelegate: this });
					} break;

					case 'gis': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Gis', { parentDelegate: this });
					} break;

					case 'relationGraph': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.RelationGraph', { parentDelegate: this });
					} break;

					case 'server': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Server', { parentDelegate: this });
					} break;

					case 'workflow': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Workflow', { parentDelegate: this });
					} break;

					case 'generalOptions':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.GeneralOptions', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				_CMCache.initModifyingTranslations();

				this.callParent(arguments);
			}
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