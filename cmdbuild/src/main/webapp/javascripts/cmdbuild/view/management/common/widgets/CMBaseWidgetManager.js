(function() {
	Ext.define("CMDBuild.view.management.common.CMBaseWidgetManager", {

		constructor: function(mainView) {
			this.mainView = mainView;
			initBuilders(this);
		},

		buildWidget: function(widget, card) {
			this.mainView.getWidgetButtonsPanel().addWidget(widget);
			return this._buildWidget(widget, card);
		},

		showWidget: function(w, title) {
			this.widgetsContainer.showWidget(w, title);
		},

		hideWidgetsContainer: function() {
			if (this.widgetsContainer) {
				this.widgetsContainer.hide();
			}
		},

		buildWidgetsContainer: function() {
			return new CMDBuild.view.management.workflow.CMWFWidgetsPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.options,
				autoScroll: true
			});
		},

		reset: 	function reset() {
			if (this.widgetsContainer) {
				this.widgetsContainer.destroy();
			}
	
			this.widgetsContainer = this.buildWidgetsContainer();
			this.mainView.getWidgetButtonsPanel().removeAllButtons();
			this.widgetsMap = {};
		},

		_buildWidget: function(widget, card) {
			if (this.builders[widget.type]) {
				return this.builders[widget.type](widget, card);
			} else {
				return null;
			}
		},

		getFormForTemplateResolver: Ext.emptyFn
	});

	function initBuilders(me) {
		me.builders = {
			// Special guests in the Widgets show, they have to open a tab in the activityTabPanel,
			// and not an indipendent window
			'.OpenNote': function(widget, card) {
				var widgetUI = me.mainView.getNotesPanel();
				widgetUI.configure({
					widget: widget,
					activityInstance: card
				});

				return widgetUI;
			},

			'.OpenAttachment': function(widget, card) {
				var oaPanel = me.mainView.getAttachmentsPanel();
				oaPanel.configure({
					widget: widget,
					activityInstance: card
				});

				return oaPanel;
			}
		};

		// createModifyCard
		me.builders[CMDBuild.view.management.common.widgets.CMCreateModifyCard.WIDGET_NAME] = function createModifyCardBuilder(widget, card) {
			var w = new CMDBuild.view.management.common.widgets.CMCreateModifyCard(widget);
			me.widgetsContainer.addWidgt(w);

			var widgetManager = new CMDBuild.view.management.classes.CMWidgetManager(w);
			w.getWidgetManager = function() {
				return widgetManager;
			};

			return w;
		};

		// calendar
		me.builders[CMDBuild.view.management.common.widgets.CMCalendar.WIDGET_NAME] = function() {
			var w = new CMDBuild.view.management.common.widgets.CMCalendar();
			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// openReport
		me.builders[CMDBuild.view.management.common.widgets.CMOpenReport.WIDGET_NAME] = function() {
			var w = new CMDBuild.view.management.common.widgets.CMOpenReport();
			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// linkCards
		me.builders[CMDBuild.view.management.workflow.widgets.CMLinkCards.WIDGET_NAME] = function(widget, card) {
			var w = new CMDBuild.view.management.workflow.widgets.CMLinkCards({
				widget: widget
			});

			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// manageRelation
		me.builders[CMDBuild.view.management.workflow.widgets.CMManageRelation.WIDGET_NAME] = function(widget, card) {
			var w = new CMDBuild.view.management.workflow.widgets.CMManageRelation({
				widget: widget
			});

			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// manageEmail
		me.builders[CMDBuild.view.management.workflow.widgets.CMManageEmail.WIDGET_NAME] = function(widget, card) {
			var w = new CMDBuild.view.management.workflow.widgets.CMManageEmail({
				widget: widget,
				activity: card
			});

			me.widgetsContainer.addWidgt(w);
			return w;
		};
	}
})();