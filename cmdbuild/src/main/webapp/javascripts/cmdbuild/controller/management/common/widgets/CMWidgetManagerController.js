(function() {

	// Requires all widget controllers to avoid to include manually
	// TODO: rename of this class to use property "requires"
	Ext.require([
		'CMDBuild.controller.management.common.widgets.ManageEmail',
		'CMDBuild.controller.management.common.widgets.manageRelation.CMManageRelationController'
	]);

	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerController", {

		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.controllers = {};

			initBuilders(this);
		},

		setDelegate: function(delegate) {
			this.delegate = delegate;
		},

		/**
		 * @param {Object} widgetController
		 */
		beforeHideView: function(widgetController) {
			if (!Ext.isEmpty(widgetController) && Ext.isFunction(widgetController.beforeHideView))
				widgetController.beforeHideView();
		},

		buildControllers: function(card) {
			var me = this;
			me.removeAll();

			if (card) {
				var definitions = me.takeWidgetFromCard(card);
				for (var i=0, l=definitions.length, w=null, ui=null; i<l; ++i) {
					w = definitions[i];
					ui = me.view.buildWidget(w, card);

					if (ui) {
						var wc = me.buildWidgetController(ui, w, card);
						if (wc) {
							me.controllers[me.getWidgetId(w)] = wc;
						}
					}
				}
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
					out += "<li>" + wc.getWidgetLabel() + "</li>";
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

		/**
		 * Trigger onBeforeSave method on all widgets creating an execution chain on all widget onBeforeSave() functions
		 *
		 * @param {Function} lastCallback
		 */
		onBeforeSaveTrigger: function(lastCallback) {
			var controllersArray = Ext.Object.getValues(this.controllers);
			var chainArray = [];

			if (!Ext.isEmpty(lastCallback) && typeof lastCallback == 'function') {
				if (Ext.isEmpty(controllersArray)) { // No activity widgets
					return lastCallback();
				} else {
					Ext.Array.forEach(controllersArray, function(controller, i, allControllers) {
						var nextControllerFunction = Ext.emptyFn;
						var scope = this;

						if (typeof controller.onBeforeSave == 'function') {
							if (i + 1 < controllersArray.length) {
								nextControllerFunction = controllersArray[i + 1].onBeforeSave;
								scope = controllersArray[i + 1];
							} else {
								nextControllerFunction = lastCallback;
								scope = this;
							}

							chainArray.push({
								fn: nextControllerFunction,
								scope: scope
							});
						}
					}, this);

					// Execute first chain function
					if (!Ext.isEmpty(controllersArray[0]) && typeof controllersArray[0].onBeforeSave == 'function') {
						controllersArray[0].onBeforeSave(chainArray, 0);
					} else {
						_msg('CMDBuild.controller.management.common.CMWidgetManagerController onBeforeSaveTrigger controllersArray head function error!');
					}
				}
			} else {
				_msg('CMDBuild.controller.management.common.CMWidgetManagerController onBeforeSaveTrigger lastCallback function error!');
			}
		},

		waitForBusyWidgets: function(cb, cbScope) {
			var me = this;

			CMDBuild.LoadMask.get().show();
			this.onBeforeSaveTrigger(
				function() {
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
				}
			);
		},

		getData: function(advance) {
			var ww = {};
			for (var wc in this.controllers) {
				wc = this.controllers[wc];

				if (typeof wc.getData == "function") {
					var wcData = wc.getData(advance);
					if (wcData != null) {
						ww[wc.getWidgetId()] = wcData;
					}
				}
			}

			return ww;
		},

		hideWidgetsContainer: function() {
			this.view.widgetsContainer.hide();
		},

		/**
		 * @param {Object} view
		 * @param {Object} widgetConfiguration
		 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card
		 *
		 * @returns {Object or null} controller
		 */
		buildWidgetController: function(view, widgetConfiguration, card) {
			var controller = null;
			var controllerClass = this.controllerClasses[widgetConfiguration.type];

			if (!Ext.isEmpty(controllerClass)) {
				if (Ext.isFunction(controllerClass)) { // @deprecated
					controller = new controllerClass(
						view,
						superController = this,
						widgetConfiguration,
						clientForm = this.view.getFormForTemplateResolver(),
						card
					);
				} else if (Ext.isString(controllerClass)) { // New widget controller declaration mode
					controller = Ext.create(controllerClass, {
						view: view,
						parentDelegate: this,
						widgetConfiguration: widgetConfiguration,
						clientForm: this.view.getFormForTemplateResolver(),
						card: card
					});
				}
			}

			return controller;
		},

		hideWidgetsContainer: function() {
			this.view.hideWidgetsContainer();
		},

		takeWidgetFromCard: function(card) {
			var widgets = [];
			if (Ext.getClassName(card) == "CMDBuild.model.CMActivityInstance") {
				widgets = card.getWidgets();
			} else {
				var et = _CMCache.getEntryTypeById(card.get("IdClass"));
				if (et) {
					widgets = et.getWidgets();
				}
			}

			return widgets;
		},

		getWidgetId: function(widget) {
			return widget.id;
		},

		getWidgetLable: function(widget) {
			return widget.label;
		},

		activateFirstTab: function() {
			this.view.activateFirstTab();
		}
	});

	function initBuilders(me) {
		me.controllerClasses = {};
		me.controllerClasses['.CustomForm'] = 'CMDBuild.controller.management.common.widgets.customForm.CustomForm';
		me.controllerClasses['.Grid'] = 'CMDBuild.controller.management.common.widgets.grid.Grid';
		me.controllerClasses['.ManageEmail'] = 'CMDBuild.controller.management.common.widgets.ManageEmail';

		var commonControllers = CMDBuild.controller.management.common.widgets;

		function addControllerClass(controller) {
			me.controllerClasses[controller.WIDGET_NAME] = controller;
		}

		// openNote
		addControllerClass(commonControllers.CMOpenNoteController);

		// openAttachment
		addControllerClass(commonControllers.CMOpenAttachmentController);

		// createModifyCard
		addControllerClass(commonControllers.CMCreateModifyCardController);

		// calendar
		addControllerClass(commonControllers.CMCalendarController);

		// workflow
		addControllerClass(commonControllers.CMWorkflowController);

		// navigationTree
		addControllerClass(commonControllers.CMNavigationTreeController);

		// openReport
		addControllerClass(commonControllers.CMOpenReportController);

		// LinkCards
		addControllerClass(CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController);

		// ManageRelation
		addControllerClass(CMDBuild.controller.management.common.widgets.manageRelation.CMManageRelationController);

		// ping
		addControllerClass(commonControllers.CMPingController);

		// webService
		addControllerClass(commonControllers.CMWebServiceController);

		// presetFromCard
		addControllerClass(commonControllers.CMPresetFromCardController);
	}

	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerControllerPopup", {
		extend: "CMDBuild.controller.management.common.CMWidgetManagerController",
		buildControllers: function(widgets, card) {
			var me = this;
			me.removeAll();

			for (var w in widgets) {
				ui = me.view.buildWidget(widgets[w], card);

				if (ui) {
					var wc = me.buildWidgetController(ui, widgets[w], card);
					if (wc) {
						me.controllers[me.getWidgetId(widgets[w])] = wc;
					}
				}
			}
		}
	});

})();