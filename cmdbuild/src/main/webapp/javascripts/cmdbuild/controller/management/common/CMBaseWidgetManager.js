(function() {
	Ext.define("CMDBuild.controller.management.common.CMBaseWidgetMananager", {

		constructor: function(view) {
			this.view = view;
			this.controllers = {};

			initBuilders(this);
		},

		setDelegate: function(delegate) {
			this.delegate = delegate;
		},

		buildControllers: function(card) {
			var me = this;
			me.removeAll();
	
			if (card) {
				var definitions = me.takeWidgetFromCard(card);
				Ext.Array.forEach(definitions, function buildController(w, i) {
					var ui = me.view.buildWidget(w, card);
					if (ui) {
						var wc = me.buildWidgetController(ui, w, card);
						if (wc) {
							me.controllers[me.getWidgetId(w)] = wc;
						}
					}
				}, this);
			}
		},
	
		onWidgetButtonClick: function(w) {
			this.delegate.ensureEditPanel();
			var me = this;
			Ext.defer(function() {
				var wc = me.controllers[me.getWidgetId(w)];
				if (wc) {
					me.view.showWidget(wc.view, me.getWidgetLable(w));
					wc.beforeActiveView();
				}
			}, 1);
		},
	
		onCardGoesInEdit: function() {
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (typeof wc.onEditMode == "function") {
					wc.onEditMode();
				}
			}
		},
	
		getWrongWFAsHTML: function getWrongWFAsHTML() {
			var out = "<ul>",
				valid = true;
	
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (!wc.isValid()) {
					valid = false;
					out += "<li>" + wc.getLabel() + "</li>";
				}
			}
			out + "</ul>";
	
			if (valid) {
				return null;
			} else {
				return out;
			}
		},
	
		removeAll: function clearWidgetControllers() {
			this.view.reset();
			for (var wcId in this.controllers) {
				var wc = this.controllers[wcId];
				wc.destroy();
				delete this.controllers[wcId];
				delete wc;
			}
		},
	
		areThereBusyWidget: function areThereBusyWidget() {
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (wc.isBusy()) {
					return true;
				} else {
					continue;
				}
			}
	
			return false;
		},
	
		waitForBusyWidgets: function waitForBusyWidgets(cb, cbScope) {
			var me = this;
	
			new _CMUtils.PollingFunction({
				success: cb,
				failure: function failure() {
					CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
				},
				checkFn: function() {
					// I want exit if there are no busy wc
					return !me.areThereBusyWidget();
				},
				cbScope: cbScope,
				checkFnScope: this
			}).run();
		},
	
		getData: function(advance) {
			var ww = {};
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
	
				if (typeof wc.getData == "function") {
					var wcData = wc.getData(advance);
					if (wcData != null) {
						ww[wc.wiewIdenrifier] = wcData;
					}
				}
			}
	
			return ww;
		},
	
		hideWidgetsContainer: function() {
			this.view.widgetsContainer.hide();
		},
	
		buildWidgetController: function buildWidgetController(ui, widgetDef, card) {
			var me = this,
				controllerClass = me.controllerClasses[widgetDef.type];

			if (controllerClass && typeof controllerClass == "function") {
				return new controllerClass(
					ui,
					superController = me,
					widgetDef,
					clientForm = me.view.getFormForTemplateResolver(),
					card
				);
			} else {
				return null;
			}
		},

		hideWidgetsContainer: function() {
			this.view.hideWidgetsContainer();
		},

		// to override in subclasses
	
		takeWidgetFromCard: function(activityInstance) {
			if (Ext.getClassName(activityInstance) == "CMDBuild.model.CMActivityInstance") {
				return activityInstance.getWidgets();
			} else {
				return [];
			}
		},
	
		getWidgetId: function(widget) {
			return widget.id;
		},
	
		getWidgetLable: function(widget) {
			return widget.label;
		}
	});

	function initBuilders(me) {
		var commonControllers = CMDBuild.controller.management.common.widgets;
		me.controllerClasses = {};

		function addControllerClass(controller) {
			me.controllerClasses[controller.WIDGET_NAME] = controller;
		}

		// TODO: move all to commonControllers

		// openNote
		addControllerClass(commonControllers.CMOpenNoteController);

		// openAttachment
		addControllerClass(CMDBuild.controller.management.workflow.widgets.CMAttachmentController);

		// createModifyCard
		addControllerClass(commonControllers.CMCreateModifyCardController);

		// calendar
		addControllerClass(commonControllers.CMCalendarController);

		// openReport
		addControllerClass(commonControllers.CMOpenReportController);

		// linkCards
		addControllerClass(CMDBuild.controller.management.workflow.widgets.CMLinkCardsController);

		// manageRelation
		addControllerClass(CMDBuild.controller.management.workflow.widgets.CMManageRelationController);

		// manageRelation
		addControllerClass(CMDBuild.controller.management.workflow.widgets.CMManageEmailController);
	}
})();