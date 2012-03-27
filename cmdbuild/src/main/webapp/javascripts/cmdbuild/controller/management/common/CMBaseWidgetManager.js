Ext.define("CMDBuild.controller.management.common.CMBaseWidgetMananager", {

	constructor: function(view) {
		this.view = view;
		this.controllers = {};
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
				out += "<li>" + wc.widgetConf.ButtonLabel + "</li>";
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

	// To override in subclass
	buildWidgetController: function(ui, widget, card) {
		throw "Must be implemented";
	},

	takeWidgetFromCard: function(card) {
		throw "Must be implemented";
	},

	getWidgetId: function(widget) {
		throw "Must be implemented";
	},

	getWidgetLable: function(widget) {
		throw "Must be implemented";
	}
});