(function() {
	Ext.define("CMDBuild.controller.management.workflow.CMWidgetManager", {
		extend: "CMDBuild.controller.management.common.CMBaseWidgetMananager",

		// override
		buildWidgetController: function buildWidgetController(ui, widgetDef) {
			var me = this,
				controllerPKG = CMDBuild.controller.management.workflow.widgets,
				builders = {
					createModifyCard: function(ui) {
						return new controllerPKG.CMCreateModifyCard(ui, me, widgetDef);
					},
					createReport: function(ui, superController, widgetDef) {
						var controller = new CMDBuild.controller.management.common.widgets.CMOpenReportController(ui, superController, widgetDef);
						controller.setWidgetReader(new CMDBuild.controller.management.common.widgets.CMWFOpenReportControllerWidgetReader());
						return controller;
					},
					linkCards: function(ui) {
						return new controllerPKG.CMLinkCardsController(ui, me, widgetDef);
					},
					manageEmail: function(ui) {
						return new controllerPKG.CMManageEmailController(ui, me, widgetDef);
					},
					manageRelation: function(ui) {
						return new controllerPKG.CMManageRelationController(ui, me, widgetDef);
					},
					openNote: function(ui) {
						return new controllerPKG.CMOpenNoteController(ui, me, widgetDef);
					},
					openAttachment: function(ui) {
						return new controllerPKG.CMAttachmentController(ui, me, widgetDef);
					},
					calendar: function(ui, me, widgetDef) {
						var reader = new CMDBuild.controller.management.workflow.widgets.CMCalendarControllerWidgetReader();
						return new CMDBuild.controller.management.common.widgets.CMCalendarController(ui, me, widgetDef, me.view.getFormForTemplateResolver(), reader);
					}
				};

			return builders[widgetDef.extattrtype](ui, me, widgetDef);
		},

		// override
		takeWidgetFromCard: function(card) {
			var data = card.raw || card.data;
			return data.CmdbuildExtendedAttributes || [];
		},

		// override
		getWidgetId: function(w) {
			return w.identifier;
		},

		// override
		getWidgetLable: function(w) {
			return w.ButtonLabel;
		},

		// for the back button is openAttachment and openNote
		showActivityPanel: function() {
			this.view.showActivityPanel();
		}
	});
})();
