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
			'onLocalizationConfigurationExportButtonClick',
			'onLocalizationConfigurationExportSectionChange',
			'onLocalizationConfigurationImportButtonClick',
			'onLocalizationConfigurationSaveButtonClick'
		],

		/**
		 * Sections where activeOnly is managed on server side
		 *
		 * @cfg {Array}
		 */
		activeOnlySections: [
			CMDBuild.core.constants.Proxy.CLASS,
			CMDBuild.core.constants.Proxy.DOMAIN,
			CMDBuild.core.constants.Proxy.LOOKUP,
			CMDBuild.core.constants.Proxy.PROCESS
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

		onLocalizationConfigurationExportButtonClick: function() {
			var formValues = this.view.exportPanel.getForm().getValues();
			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = formValues[CMDBuild.core.constants.Proxy.TYPE];
			params[CMDBuild.core.constants.Proxy.SEPARATOR] = formValues[CMDBuild.core.constants.Proxy.SEPARATOR];
			params[CMDBuild.core.constants.Proxy.ACTIVE] = formValues[CMDBuild.core.constants.Proxy.ACTIVE_ONLY];
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			CMDBuild.core.proxy.localization.Export.exports({
				form: this.view.exportPanel.getForm(),
				params: params,
				scope: this,
				success: function(form, action) { // TODO: probably not these parameters
					CMDBuild.core.Message.success();
				},
				failure: function(form, action) { // TODO: probably not these parameters
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
						false
					);
				}
			});
		},

		/**
		 * ActiveOnly parameter is managed on server side only for class, domain, lookup and process
		 *
		 * @param {String} selection
		 */
		onLocalizationConfigurationExportSectionChange: function(selection) {
			this.view.exportPanel.activeOnlyCheckbox.setValue();
			this.view.exportPanel.activeOnlyCheckbox.setDisabled(!Ext.Array.contains(this.activeOnlySections, selection));
		},

		onLocalizationConfigurationImportButtonClick: function() {
			if (this.validate(this.view.importPanel)) {
				CMDBuild.core.proxy.localization.Import.imports({
					form: this.view.importPanel.getForm(),
					scope: this,
					success: function(form, action) { // TODO: probably not these parameters
						var importFailures = action.result.response.failures;

						if (Ext.isEmpty(importFailures)) {
							CMDBuild.core.Message.success();
						} else {
							// TODO: import error visualization/download
							CMDBuild.core.Message.error(
								CMDBuild.Translation.common.failure,
								importFailures.toString(),
								true
							);
						}
					},
					failure: function(form, action) { // TODO: probably not these parameters
						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
							false
						);
					}
				});
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
				success: function(result, options, decodedResult) { // TODO: rename parameters with "response"
					var params = decodedResult.data; // TODO: proxy constants
					params['language'] = this.view.defaultLanguageCombobox.getValue();
					params['languageprompt'] = this.view.languagePromptCheckbox.getValue();
					params['enabled_languages'] = this.view.enabledLanguagesGrid.getValue().join(', ');

					CMDBuild.core.proxy.configuration.GeneralOptions.update({
						scope: this,
						params: params,
						success: function(result, options, decodedResult) {
							CMDBuild.core.Message.success();
						}
					});
				}
			});
		}
	});

})();