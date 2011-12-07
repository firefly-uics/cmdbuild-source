(function() {
	Ext.define("CMDBuild.controller.management.workflow.CMWidgetManager", {
		constructor: function(view) {
			this.view = view;
			this.controllers = {};
		},

		buildControllers: function(card) {
			var me = this;
			me.removeAll();

			if (card) {
				var ww = me.view.buildWidgets(card);
	
				for (var identifier in ww) {
					var wc = me.buildWidgetController(ww[identifier]);
					me.controllers[identifier] = wc;
	
					// on add, the widget controllers are build after that
					// the form go in editing, so call the onEditMode to notify the editing status
	//				if (me.editMode) {
	//					wc.onEditMode();
	//				}
				}
			}
		},

		onWidgetButtonClick: function(w) {
			var wc = this.controllers[w.identifier];
			if (wc) {
				this.view.showWidget(wc.view);
				wc.beforeActiveView();
			}
		},

		buildWidgetController: function buildWidgetController(w) {
			var me = this,
				controllerPKG = CMDBuild.controller.management.workflow.widgets,
				builders = {
					createModifyCard: function(w) {
						return new controllerPKG.CMCreateModifyCard(w, me);
					},
					createReport: function(w) {
						return new controllerPKG.CMCreateReportController(w, me);
					},
					linkCards: function(w) {
						return new controllerPKG.CMLinkCardsController(w, me);
					},
					manageEmail: function(w) {
						return new controllerPKG.CMManageEmailController(w, me);
					},
					manageRelation: function(w) {
						return new controllerPKG.CMManageRelationController(w, me);
					},
					openNote: function(w) {
						return new controllerPKG.CMOpenNoteController(w, me);
					},
					openAttachment: function(w) {
						return new controllerPKG.CMAttachmentController(w, me);
					},
					calendar: function(w) {
						return new controllerPKG.CMCalendarController(w, me);
					}
				};

			return builders[w.extattrtype](w);
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
			this.view.buildWidgets(null);
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				wc.destroy();
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
		}
	});
})();
