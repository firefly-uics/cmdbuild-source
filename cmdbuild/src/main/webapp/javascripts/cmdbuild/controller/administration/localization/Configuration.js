(function() {

	Ext.define('CMDBuild.controller.administration.localization.Configuration', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.configuration.GeneralOptions',
			'CMDBuild.core.proxy.localization.Export',
			'CMDBuild.core.proxy.localization.Import'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationConfigurationAbortButtonClick',
			'onLocalizationConfigurationDefaultLanguageChange',
			'onLocalizationConfigurationSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.localization.ConfigurationPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.Localization} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.ConfigurationPanel', { delegate: this });

			this.configurationRead();
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to use loadData methods
		 *
		 * @private
		 */
		configurationRead: function() {
			CMDBuild.core.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					var decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.languagePromptCheckbox.setValue(decodedResponse['languageprompt']);
					this.view.enabledLanguagesGrid.setValue(decodedResponse['enabled_languages'].split(', ')); // TODO: delete on server configuration refactor
					this.view.defaultLanguageCombobox.setValue(decodedResponse['language']); // Must be before enabledLanguagesGrid to avoid check errors
				}
			});
		},

		onLocalizationConfigurationAbortButtonClick: function() {
			this.configurationRead();
		},

		/**
		 * Check and disable defaultLanguage relative language checkbox to avoid server wrong configurations
		 *
		 * @param {Object} parameters
		 * @param {String} parameters.defaultLanguageTag
		 * @param {String} parameters.oldDefaultLanguageTag
		 */
		onLocalizationConfigurationDefaultLanguageChange: function(parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.defaultLanguageTag) && Ext.isString(parameters.defaultLanguageTag)
			) {
				Ext.Array.forEach(this.view.enabledLanguagesGrid.getItems(), function(checkbox, i, allCheckboxes) {
					if (checkbox.getName() == parameters.defaultLanguageTag) {
						checkbox.setValue(true);
						checkbox.disable();
					}

					if (
						!Ext.isEmpty(parameters.oldDefaultLanguageTag) && Ext.isString(parameters.oldDefaultLanguageTag)
						&& checkbox.getName() == parameters.oldDefaultLanguageTag
					) {
						checkbox.setValue(false);
						checkbox.enable();
					}
				}, this);
			}
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to save directly only language configuration on another endpoint
		 */
		onLocalizationConfigurationSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					var params = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
					params['language'] = this.view.defaultLanguageCombobox.getValue();
					params['languageprompt'] = this.view.languagePromptCheckbox.getValue();
					params['enabled_languages'] = this.view.enabledLanguagesGrid.getValue().join(', ');

					CMDBuild.core.proxy.configuration.GeneralOptions.update({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							CMDBuild.core.Message.success();
						}
					});
				}
			});
		}
	});

})();