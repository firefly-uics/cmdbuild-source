(function() {
	Ext.define("CMDBuild.view.management.workflow.CMWidgetManager", {
		extend: "CMDBuild.view.management.common.CMBaseWidgetManager",

		// override
		showWidget: function(w) {
			if (w.extattrtype == "openNote"
				|| w.extattrtype == "openAttachment") {

				this.mainView.showWidget(w);
			} else {
				this.callParent(arguments);
			}
		},

		// override
		_buildWidget: function buildWidget(widget, card) {
			var me = this,
				conf = {
					widget: widget,
					activity: card,
					clientForm: me.getFormForTemplateResolver()
				},
				builders = {
					createModifyCard: function() {
						var w = new CMDBuild.view.management.common.widgets.CMCreateModifyCard(conf);
						me.widgetsContainer.addWidgt(w);
	
						var widgetManager = new CMDBuild.view.management.classes.CMWidgetManager(w);
						w.getWidgetManager = function() {
							return widgetManager;
						}

						return w;
					},
					createReport: function() {
						var w = new CMDBuild.view.management.common.widgets.CMOpenReport(conf);
						me.widgetsContainer.addWidgt(w);
	
						return w;
					},
					linkCards: function() {
						var w = new CMDBuild.view.management.workflow.widgets.CMLinkCards(conf);
						me.widgetsContainer.addWidgt(w);
	
						return w;
					},
					manageEmail: function() {
						var w = new CMDBuild.view.management.workflow.widgets.CMManageEmail(conf);
						me.widgetsContainer.addWidgt(w);
	
						return w;
					},
					manageRelation: function() {
						var w = new CMDBuild.view.management.workflow.widgets.CMManageRelation(conf);
						me.widgetsContainer.addWidgt(w);
	
						return w;
					},
					calendar: function() {
						var w = new CMDBuild.view.management.common.widgets.CMCalendar();
						me.widgetsContainer.addWidgt(w);
	
						return w;
					},
	
					// Special guests in the Widgets show, they have to open a tab in the activityTabPanel,
					// and not an indipendent window
					openNote: function() {
						var onPanel = me.mainView.getNotesPanel();
						onPanel.configure(conf);
	
						return onPanel;
					},
	
					openAttachment: function() {
						var oaPanel = me.mainView.getAttachmentsPanel();
						oaPanel.configure(conf);
	
						return oaPanel;
					}
				};
	
			if (builders[widget.extattrtype]) {
				return builders[widget.extattrtype](widget);
			} else {
				return null;
			}
		},

		// override
		getFormForTemplateResolver: function getFormForTemplateResolver() {
			return this.mainView.getActivityPanel().getForm()
		},

		// for the back button is openAttachment and openNote
		showActivityPanel: function showActivityPanel() {
			this.mainView.activateFirstTab();
		}
	});
})();