(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader",{
		getType: function(w) {return "custom"},
		getCode: function(w) {return w.reportCode},
		getPreset: function(w) {return w.preset},
		getForceFormat: function(w) {return w.forceFormat}
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenReportController", {
		mixins: {
			observable: 'Ext.util.Observable'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef) {
			if (typeof view != "object") {
				throw "The view of a WFWidgetController must be an object"
			}

			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.view = view;
			this.ownerController = ownerController;
			this.widget = this.view.widgetConf || widgetDef;

			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
		},

		setWidgetReader: function(widgetReader) {
			this.widgetReader = widgetReader;
		},

		beforeActiveView: function() {
			if (this.configured || !this.widgetReader) {
				return;
			}

			this.view.setLoading(true);
			var me = this,
				wr = this.widgetReader;

			Ext.Ajax.request({
				url : 'services/json/management/modreport/createreportfactorybytypecode',
				params : {
					type: wr.getType(me.widget),
					code: wr.getCode(me.widget)
				},
				success : function(response) {
					var ret = Ext.JSON.decode(response.responseText),
						attributes = ret.filled ? [] : ret.attribute; // filled == with no parameters

					me.view.configureForm(attributes, wr.getPreset(me.widget));
					me.view.fillFormValues(wr.getPreset(me.widget));
					me.view.forceExtension(wr.getForceFormat(me.widget));

					me.view.setLoading(false);
					me.configured = true;
				},
				scope: me
			});
		},

		destroy: function() {
			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
		}
	});

	function onSaveCardClick() {
		var form = this.view.formPanel.getForm();
		
		var formatName = this.view.formatCombo.getName(),
			formatValue = this.view.formatCombo.getValue(),
			params = {};

		params[formatName] = formatValue;

		if (form.isValid()) {
			CMDBuild.LoadMask.get().show();

			form.submit({
				method : 'POST',
				url : 'services/json/management/modreport/updatereportfactoryparams',
				params : params,
				scope: this,
				success : function(form, action) {
					var popup = window.open("services/json/management/modreport/printreportfactory?donotdelete=true", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
					CMDBuild.LoadMask.get().hide();
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}
})();
