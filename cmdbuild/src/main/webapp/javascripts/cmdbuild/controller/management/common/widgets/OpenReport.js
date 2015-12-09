(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.OpenReport', {
		extend:'CMDBuild.controller.common.AbstractWidgetController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widget.OpenReport'
		],

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onOpenReportSaveButtonClick',
			'showReport',
			'widgetOpenReportSelectedReportRecordGet = selectedReportRecordGet'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.OpenReport}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.openReport.Configuration',

		/**
		 * @override
		 */
		beforeActiveView: function() {
			if (
				!this.widgetConfigurationIsEmpty()
				&& Ext.isEmpty(this.templateResolver)
			) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.CUSTOM;
				params[CMDBuild.core.constants.Proxy.CODE] = this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.REPORT_CODE);

				CMDBuild.core.proxy.widget.OpenReport.createFactory({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						if (!decodedResponse.filled)
							this.configureForm(decodedResult[CMDBuild.core.constants.Proxy.ATTRIBUTE]);

						new CMDBuild.Management.TemplateResolver({
							clientForm: this.clientForm,
							xaVars: this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.PRESET),
							serverVars: this.getTemplateResolverServerVars()
						}).resolveTemplates({
							attributes: Ext.Object.getKeys(this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.PRESET)),
							scope: this,
							callback: function(out, ctx) {
								this.fillFormValues(out);
								this.forceExtension(this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.FORCE_FORMAT));
							}
						});
					}
				});
			}
		},

		/**
		 * Add the required attributes and disable fields if in readOnlyAttributes array
		 *
		 * @param {Array} attributes
		 *
		 * @private
		 */
		configureForm: function(attributes) {
			this.view.fieldContainer.removeAll();

			if (!Ext.isEmpty(attributes) && Ext.isArray(attributes))
				Ext.Array.forEach(attributes, function(attribute, i, allAttributes) {
					var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);

					if (!Ext.isEmpty(field)) {
						// To disable if field name is contained in widgetConfiguration.readOnlyAttributes
						field.setDisabled(
							Ext.Array.contains(this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES), attribute[CMDBuild.core.constants.Proxy.NAME])
						);

						this.view.fieldContainer.add(field);
					}
				}, this);
		},

		/**
		 * Fixes date format
		 *
		 * @param {Object} parameters - Ex: { input_name: value, ...}
		 *
		 * @private
		 */
		fillFormValues: function(parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				Ext.Object.each(parameters, function(key, value, myself) {
					if (Ext.isDate(value))
						parameters[key] =  new Date(value);
				}, this);

				this.view.loadRecord(Ext.create('CMDBuild.DummyModel', parameters));
			}
		},

		/**
		 * @param {String} extension
		 *
		 * @private
		 */
		forceExtension: function(extension) {
			if (!Ext.isEmpty(extension)) {
				this.view.formatCombo.setValue(extension);
				this.view.formatCombo.disable();
			} else {
				this.view.formatCombo.enable();
			}
		},

		/**
		 * Build server call to configure and create reports
		 */
		onOpenReportSaveButtonClick: function() {
			var params = {};

			// Build params with fields values form server call
			this.view.getForm().getFields().each(function(field, i, len) {
				if (Ext.isFunction(field.getName) && Ext.isFunction(field.getValue)) {
					var fieldValue = field.getValue();

					// Date format check
					if (Ext.isDate(fieldValue))
						fieldValue = Ext.Date.format(fieldValue, 'd/m/Y');

					params[field.getName()] = fieldValue;
				}
			}, this);

			params['reportExtension'] = params[CMDBuild.core.constants.Proxy.EXTENSION]; // TODO: waiting for refactor (rename)

			if (this.view.getForm().isValid())
				CMDBuild.core.proxy.widget.OpenReport.update({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) { // Pop-up display mode
						Ext.create('CMDBuild.controller.management.report.Modal', {
							parentDelegate: this,
							extension: params[CMDBuild.core.constants.Proxy.EXTENSION]
						});
					}
				});
		},

		/**
		 * Get created report from server and display it in popup window
		 *
		 * @param {Boolean} forceDownload
		 */
		showReport: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			var form = Ext.create('Ext.form.Panel', {
				standardSubmit: true,
				url: CMDBuild.core.proxy.Index.report.printReportFactory + '?donotdelete=true' // Add parameter to avoid report delete
			});

			form.submit({
				target: '_blank',
				params: params
			});

			Ext.defer(function() { // Form cleanup
				form.close();
			}, 100);
		},

		/**
		 * Avoids not managed function warning
		 */
		widgetOpenReportSelectedReportRecordGet: Ext.emptyFn
	});

})();