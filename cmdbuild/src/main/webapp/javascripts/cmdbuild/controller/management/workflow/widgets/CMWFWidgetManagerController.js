(function() {
	Ext.define("CMDBuild.controller.management.workflow.CMWidgetManager", {
		extend: "CMDBuild.controller.management.common.CMBaseWidgetMananager",

		// override
		buildWidgetController: function buildWidgetController(ui, widgetDef, card) {
			var me = this,
				controllerPKG = CMDBuild.controller.management.workflow.widgets,
				builders = {
					createModifyCard: function(ui) {
						var widgetControllerManager = new CMDBuild.controller.management.classes.CMWidgetManager(ui.getWidgetManager());
						return new controllerPKG.CMCreateModifyCard(ui, me, widgetDef, widgetControllerManager);
					},
					createReport: function(ui, superController, widgetDef, card) {
						return new CMDBuild.controller.management.common.widgets.CMOpenReportController(
							ui,
							superController,
							widgetDef,
							me.view.getFormForTemplateResolver(),
							new CMDBuild.controller.management.common.widgets.CMWFOpenReportControllerWidgetReader(),
							card
						);
						
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
						return new controllerPKG.CMAttachmentController(ui, me, widgetDef, card);
					},
					calendar: function(ui, me, widgetDef,card) {
						return new CMDBuild.controller.management.common.widgets.CMCalendarController(
							ui,
							me,
							widgetDef,
							me.view.getFormForTemplateResolver(),
							new CMDBuild.controller.management.workflow.widgets.CMCalendarControllerWidgetReader(),
							card
						);
					}
				};

			return builders[widgetDef.extattrtype](ui, me, widgetDef, card);
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
