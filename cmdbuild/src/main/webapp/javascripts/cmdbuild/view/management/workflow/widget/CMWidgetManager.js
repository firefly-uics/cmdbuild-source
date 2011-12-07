(function() {
	Ext.define("CMDBuild.view.management.workflow.CMWidgetManager", {
		constructor: function(mainView) {
			this.mainView = mainView;

			this.widgetsContainer = new CMDBuild.view.management.workflow.CMWFWidgetsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.options,
				autoScroll: true
			});
		},

		buildWidgets: function(card) {
			if (this.widgetsContainer) {
				this.widgetsContainer.destroy();
			}

			this.widgetsContainer = new CMDBuild.view.management.workflow.CMWFWidgetsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.options,
				autoScroll: true
			});

			this.mainView.getWidgetButtonsPanel().updateWidgets([]);
			this.widgetsMap = {};

			if (card) {
				var data = card.raw || card.data,
					widgets = data.CmdbuildExtendedAttributes || [];

				Ext.Array.forEach(widgets, function(w, i) {
					var ui = buildWidget.call(this, w, card);
					if (ui) {
						this.widgetsMap[w.identifier] = ui;
					}
				}, this);

				this.mainView.getActivityPanel().updateWidgets(widgets);
			}

			return this.widgetsMap;
		},

		getWFWidgets: function() {
			return this.widgetsMap;
		},

		showWidget: function(w) {
			this.widgetsContainer.showWidget(w);
		}
	});

	function buildWidget(widget, card) {
		var me = this,
			conf = {
				widget: widget,
				activity: card,
				clientForm: this.mainView.getActivityPanel().getForm()
			},
			builders = {
				createModifyCard: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMCreateModifyCard(conf);
					me.widgetsContainer.addWidgt(w);

					return w;
				},
				createReport: function() {
					var w = new CMDBuild.view.management.workflow.widgets.CMCreateReport(conf);
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
					var w = new CMDBuild.view.management.workflow.widgets.CMCalendar(conf);
					me.widgetsContainer.addWidgt(w);

					return w;
				},
				openNote: function() {
					me.openNotePanel.configure(conf);

					return me.openNotePanel;
				},
				openAttachment: function() {
					me.openAttachmentPanel.configure(conf);

					return me.openAttachmentPanel;
				}
			};

		if (builders[widget.extattrtype]) {
			return builders[widget.extattrtype](widget);
		} else {
			return null;
		}
	}
})();
