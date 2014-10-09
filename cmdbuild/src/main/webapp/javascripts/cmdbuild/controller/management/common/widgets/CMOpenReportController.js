(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader',{
		getType: function(w) {return 'custom'; },
		getCode: function(w) { return w.reportCode; },
		getPreset: function(w) { return w.preset; },
		getForceFormat: function(w) { return w.forceFormat; }
	});

	Ext.define('CMDBuild.controller.management.common.widgets.CMOpenReportController', {
		mixins: {
			observable: 'Ext.util.Observable',
			widgetcontroller: 'CMDBuild.controller.management.common.widgets.CMWidgetController'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMOpenReport} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetDef
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 */
		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.widgetReader = new CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader();
			this.presets = this.widgetReader.getPreset(this.widgetConf);

			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
		},

		beforeActiveView: function() {
			var me = this;
			var wr = this.widgetReader;

			if (!Ext.isEmpty(me.widgetReader)) {
				if (me.configured && me.templateResolver) {
					resolveTemplate(me);
				} else {
					me.view.setLoading(true);

					Ext.Ajax.request({
						scope: me,
						url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.createReportFactoryByTypeCode,
						params: {
							type: wr.getType(me.widgetConf),
							code: wr.getCode(me.widgetConf)
						},
						success: function(result, options, decodedResult) {
							me.attributes = decodedResult.filled ? [] : decodedResult.attribute; // filled == with no parameters
							me.view.configureForm(me.attributes, me.widgetConf);

							me.templateResolver = new CMDBuild.Management.TemplateResolver({
								clientForm: me.clientForm,
								xaVars: me.presets,
								serverVars: this.getTemplateResolverServerVars()
							});
							resolveTemplate(me);

							me.view.setLoading(false);
							me.configured = true;
						}
					});
				}
			}
		},

		destroy: function() {
			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
		}
	});

	function resolveTemplate(me) {
		var wr = me.widgetReader;

		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.presets),
			callback: function(o) {
				me.view.fillFormValues(o);
				me.view.forceExtension(wr.getForceFormat(me.widgetConf));
			}
		});
	}

	/**
	 * Build server call to configure and create reports
	 */
	function onSaveCardClick() {
		var form = this.view.formPanel.getForm();
		var formFields = this.view.formPanel.items.items;
		var params = {};

		// Build params with fields values form server call
		for (var index in formFields) {
			var field = formFields[index];

			if (typeof field.getName == 'function' && typeof field.getValue == 'function')
				params[field.getName()] = field.getValue();
		}

		if (form.isValid()) {
			CMDBuild.LoadMask.get().show();

			form.submit({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.reports.updateReportFactoryParams,
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

					CMDBuild.LoadMask.get().hide();
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}

})();