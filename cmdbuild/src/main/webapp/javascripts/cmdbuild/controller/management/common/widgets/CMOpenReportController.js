(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.CMOpenReportController', {
		extend:'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: ['CMDBuild.core.proxy.widgets.OpenReport'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME
		},

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.CMOpenReport}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMOpenReport} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			this.widgetPreset = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.PRESET];

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
				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
			var me = this;

			if (!Ext.isEmpty(this.widgetConf)) {
				if (me.configured && me.templateResolver) {
					this.resolveTemplate();
				} else {
					me.view.setLoading(true);

					CMDBuild.core.proxy.widgets.OpenReport.getReportParameters({
						scope: this,
						params: {
							type: 'custom',
							code: this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REPORT_CODE]
						},
						success: function(result, options, decodedResult) {
							this.attributes = decodedResult.filled ? [] : decodedResult.attribute; // filled == with no parameters
							this.view.configureForm(this.attributes, this.widgetConf);

							this.templateResolver = new CMDBuild.Management.TemplateResolver({
								clientForm: me.clientForm,
								xaVars: me.widgetPreset,
								serverVars: this.getTemplateResolverServerVars()
							});

							this.resolveTemplate();

							this.view.setLoading(false);
							this.configured = true;
						}
					});
				}
			}
		},

		/**
		 * Build server call to configure and create reports
		 */
		onSaveButtonClick: function() {
			var form = this.view.formPanel.getForm();
			var formFields = form.getFields().items;
			var params = {};

			// Build params with fields values form server call
			for (var i in formFields) {
				var field = formFields[i];

				if (typeof field.getName == 'function' && typeof field.getValue == 'function') {
					var fieldValue = field.getValue();

					// Date format check
					if (fieldValue instanceof Date)
						fieldValue = Ext.Date.format(fieldValue, 'd/m/Y');

					params[field.getName()] = fieldValue;
				}
			}

			if (form.isValid()) {
				this.view.setLoading(true);

				CMDBuild.core.proxy.widgets.OpenReport.generateReport({
					params: params,
					scope: this,
					success: function(form, action) {
						var popup = window.open(
							'services/json/management/modreport/printreportfactory?donotdelete=true',
							'Report',
							'height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable'
						);

						if (!popup)
							CMDBuild.Msg.warn(
								CMDBuild.Translation.warnings.warning_message,
								CMDBuild.Translation.warnings.popup_block
							);

						this.view.setLoading(false);
					},
					failure: function() {
						this.view.setLoading(false);
					}
				});
			}
		},

		resolveTemplate: function() {
			var me = this;

			this.templateResolver.resolveTemplates({
				attributes: Ext.Object.getKeys(me.widgetPreset),
				callback: function(out, ctx) {
					me.view.fillFormValues(out);
					me.view.forceExtension(me.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FORCE_FORMAT]);
				}
			});
		}
	});

})();