(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Configuration', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.importExport.Csv',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationConfigurationAbortButtonClick',
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
			CMDBuild.core.proxy.Constants.CLASS,
			CMDBuild.core.proxy.Constants.DOMAIN,
			CMDBuild.core.proxy.Constants.LOOKUP,
			CMDBuild.core.proxy.Constants.PROCESS
		],

		/**
		 * @cfg {CMDBuild.view.administration.localizations.ConfigurationPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.Localizations} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localizations.ConfigurationPanel', { delegate: this });

			this.configurationRead();
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to use loadData methods
		 */
		configurationRead: function() {
			CMDBuild.core.proxy.Configuration.read({
				scope: this,
				loadMask: true,
				success: function(result, options, decodedResult){
					var decodedResult = decodedResult.data;

					this.view.defaultLanguageCombobox.setValue(decodedResult['language']);
					this.view.languagePromptCheckbox.setValue(decodedResult['languageprompt']);
					this.view.enabledLanguagesGrid.setValue(decodedResult['enabled_languages'].split(', ')); // TODO: delete on server configuration refactor
				}
			}, 'cmdbuild');
		},

		onLocalizationConfigurationAbortButtonClick: function() {
			this.configurationRead();
		},

		onLocalizationConfigurationExportButtonClick: function() {
			var formValues = this.view.exportPanel.getForm().getValues();
			var params = {};
			params[CMDBuild.core.proxy.Constants.TYPE] = formValues[CMDBuild.core.proxy.Constants.SECTION];
			params[CMDBuild.core.proxy.Constants.SEPARATOR] = formValues[CMDBuild.core.proxy.Constants.SEPARATOR];
			params[CMDBuild.core.proxy.Constants.ACTIVE] = formValues[CMDBuild.core.proxy.Constants.ACTIVE_ONLY];
			params[CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD_PARAM_KEY] = true;

			CMDBuild.core.proxy.localizations.importExport.Csv.exports({
				form: this.view.exportPanel.getForm(),
				params: params,
				scope: this,
				success: function(form, action) {
					CMDBuild.core.Message.success();

					CMDBuild.LoadMask.get().hide();
				},
				failure: function(form, action) {
					CMDBuild.LoadMask.get().hide();

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

		onLocalizationConfigurationImportButtonClick: function() { // TODO: finish implementation
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.localizations.importExport.Csv.imports({
				form: this.view.importPanel.getForm(),
				scope: this,
				success: function(form, action) {
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

					CMDBuild.LoadMask.get().hide();
				},
				failure: function(form, action) {
					CMDBuild.LoadMask.get().hide();

					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
						false
					);
				}
			});
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to save directly only language configuration on another endpoint
		 */
		onLocalizationConfigurationSaveButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Configuration.read({
				scope: this,
				success: function(result, options, decodedResult){
					var params = decodedResult.data;
					params['language'] = this.view.defaultLanguageCombobox.getValue();
					params['languageprompt'] = this.view.languagePromptCheckbox.getValue();
					params['enabled_languages'] = this.view.enabledLanguagesGrid.getValue().join(', ');

					CMDBuild.core.proxy.Configuration.save({
						scope: this,
						params: params,
						success: function(result, options, decodedResult) {
							CMDBuild.LoadMask.get().hide();

							CMDBuild.core.Message.success();
						}
					}, 'cmdbuild');
				}
			}, 'cmdbuild');
		}
	});

})();