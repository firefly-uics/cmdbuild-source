(function() {

	Ext.define('CMDBuild.controller.administration.localization.ImportExport', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.localization.Export',
			'CMDBuild.core.proxy.localization.Import'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

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
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationImportExportExportButtonClick',
			'onLocalizationImportExportExportSectionChange',
			'onLocalizationImportExportImportButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ExportForm}
		 */
		exportPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ImportForm}
		 */
		importPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ImportExportView}
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

			this.view = Ext.create('CMDBuild.view.administration.localization.importExport.ImportExportView', { delegate: this });

			// Shorthands
			this.exportPanel = this.view.exportPanel;
			this.importPanel = this.view.importPanel;
		},

		onLocalizationImportExportExportButtonClick: function() {
			var formValues = this.exportPanel.getForm().getValues();
			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = formValues[CMDBuild.core.constants.Proxy.TYPE];
			params[CMDBuild.core.constants.Proxy.SEPARATOR] = formValues[CMDBuild.core.constants.Proxy.SEPARATOR];
			params[CMDBuild.core.constants.Proxy.ACTIVE] = formValues[CMDBuild.core.constants.Proxy.ACTIVE_ONLY];
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			CMDBuild.core.proxy.localization.Export.exports({
				form: this.exportPanel.getForm(),
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
		onLocalizationImportExportExportSectionChange: function(selection) {
			this.exportPanel.activeOnlyCheckbox.setValue();
			this.exportPanel.activeOnlyCheckbox.setDisabled(!Ext.Array.contains(this.activeOnlySections, selection));
		},

		onLocalizationImportExportImportButtonClick: function() {
			if (this.validate(this.importPanel))
				CMDBuild.core.proxy.localization.Import.imports({
					form: this.importPanel.getForm(),
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
	});

})();