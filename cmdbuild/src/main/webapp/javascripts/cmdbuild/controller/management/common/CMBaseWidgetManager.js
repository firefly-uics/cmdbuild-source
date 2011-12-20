Ext.define("CMDBuild.controller.management.common.CMBaseWidgetMananager", {
	constructor: function(view) {
		this.view = view;
		this.controllers = {};
	},

	buildControllers: function(card) {
		var me = this;
		me.removeAll();

		if (card) {
			var definitions = me.takeWidgetFromCard(card);
			Ext.Array.forEach(definitions, function buildController(w, i) {
				var ui = me.view.buildWidget(w, card);
				if (ui) {
					var wc = me.buildWidgetController(ui, w);
					if (wc) {
						me.controllers[me.getWidgetId(w)] = wc;
					}

				// on add, the widget controllers are build after that
				// the form go in editing, so call the onEditMode to notify the editing status
//				if (me.editMode) {
//					wc.onEditMode();
//				}
				}
			}, this);
		}
	},

	onWidgetButtonClick: function(w) {
		var wc = this.controllers[this.getWidgetId(w)];
		if (wc) {
			this.view.showWidget(wc.view, this.getWidgetLable(w));
			wc.beforeActiveView();
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
			var wcData = wc.getData(advance);

			if (wcData != null) {
				ww[wc.wiewIdenrifier] = wcData;
			}
		}

		return ww;
	},

	// To override in subclass
	buildWidgetController: function(ui) {
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