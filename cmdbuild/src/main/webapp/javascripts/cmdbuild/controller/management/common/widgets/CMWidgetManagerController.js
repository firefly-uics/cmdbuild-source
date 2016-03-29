(function() {

	Ext.require(['CMDBuild.controller.management.common.widgets.manageRelation.CMManageRelationController']); // Legacy

	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerController", {

		/**
		 * @property {Object}
		 */
		controllerClasses: {},

		/**
		 * @property {Object}
		 */
		controllers: {},

		constructor: function(view) {
			Ext.apply(this, {
				controllerClasses: {
					'.Calendar': CMDBuild.controller.management.common.widgets.CMCalendarController,
					'.CreateModifyCard': CMDBuild.controller.management.common.widgets.CMCreateModifyCardController,
					'.CustomForm': 'CMDBuild.controller.management.widget.customForm.CustomForm',
					'.Grid': 'CMDBuild.controller.management.common.widgets.grid.Grid',
					'.LinkCards': CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController,
					'.ManageEmail': 'CMDBuild.controller.management.widget.ManageEmail',
					'.ManageRelation': CMDBuild.controller.management.common.widgets.manageRelation.CMManageRelationController,
					'.NavigationTree': CMDBuild.controller.management.common.widgets.CMNavigationTreeController,
					'.OpenAttachment': CMDBuild.controller.management.common.widgets.CMOpenAttachmentController,
					'.OpenNote': CMDBuild.controller.management.common.widgets.CMOpenNoteController,
					'.OpenReport': 'CMDBuild.controller.management.common.widgets.OpenReport',
					'.Ping': CMDBuild.controller.management.common.widgets.CMPingController,
					'.PresetFromCard': CMDBuild.controller.management.common.widgets.CMPresetFromCardController,
					'.WebService': CMDBuild.controller.management.common.widgets.CMWebServiceController,
					'.Workflow': CMDBuild.controller.management.common.widgets.CMWorkflowController
				},
				view: view
			});

			this.view.delegate = this;
		},

		setDelegate: function(delegate) {
			this.delegate = delegate;
		},

		/**
		 * Forwarder method
		 *
		 * @param {Object} controller
		 */
		beforeHideView: function(controller) {
			if (!Ext.isEmpty(controller)) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('beforeHideView');
				} else if (Ext.isFunction(controller.beforeHideView)) {
					controller.beforeHideView();
				}
			}
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

					// cmfg() implementation adapter
					if (!Ext.isEmpty(wc.cmfg) && Ext.isFunction(wc.cmfg)) {
						wc.cmfg('beforeActiveView');
					} else if (Ext.isFunction(wc.beforeActiveView)) {
						wc.beforeActiveView();
					}
				}
			}, 1);
		},

		/**
		 * Forwarder method
		 *
		 * @public
		 */
		onCardGoesInEdit: function() {
			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// FIXME: widget instance data storage should be implemented inside this class
				if (!Ext.isEmpty(controller.instancesDataStorageReset) && Ext.isFunction(controller.instancesDataStorageReset))
					controller.instancesDataStorageReset();

				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('onEditMode');
				} else if (!Ext.isEmpty(controller.onEditMode) && Ext.isFunction(controller.onEditMode)) {
					controller.onEditMode();
				}
			}, this);
		},

		/**
		 * @returns {String or null}
		 *
		 * @public
		 */
		getWrongWFAsHTML: function () {
			var out = '';
			var widgetsAreValid = true;

			Ext.Object.each(this.controllers, function (id, controller, myself) {
				// cmfg() implementation adapter
				if (
					!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)
					&& !controller.cmfg('isValid')
				) {
					widgetsAreValid = false;
					out += '<li>' + controller.cmfg('getLabel') + '</li>';
				} else if (
					!Ext.isEmpty(controller.isValid) && Ext.isFunction(controller.isValid)
					&& !Ext.isEmpty(controller.getLabel) && Ext.isFunction(controller.getLabel)
					&& !controller.isValid()
				) {
					widgetsAreValid = false;
					out += '<li>' + controller.getLabel() + '</li>';
				}
			}, this);

			return widgetsAreValid ? null : '<ul>' + out + '</ul>';
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

		/**
		 * @returns {Boolean} widgetsBusyState
		 *
		 * @private
		 */
		areThereBusyWidget: function() {
			var widgetsBusyState = false;

			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					widgetsBusyState = controller.cmfg('isBusy');

					return !widgetsBusyState;
				} else if (!Ext.isEmpty(controller.isValid) && Ext.isFunction(controller.isValid)) {
					widgetsBusyState = controller.isBusy();

					return !widgetsBusyState;
				}
			}, this);

			return widgetsBusyState;
		},

		/**
		 * Trigger onBeforeSave method on all widgets creating an execution chain on all widget onBeforeSave() functions
		 *
		 * @param {Function} lastCallback
		 *
		 * @private
		 */
		beforeSaveTriggerManager: function(lastCallback) {
			var controllersArray = Ext.Object.getValues(this.controllers);
			var chainArray = [];

			if (!Ext.isEmpty(lastCallback) && Ext.isFunction(lastCallback)) {
				if (Ext.Object.isEmpty(controllersArray)) { // No active widgets
					return lastCallback();
				} else {
					Ext.Array.forEach(controllersArray, function(controller, i, allControllers) {
						var nextControllerFunction = Ext.emptyFn;
						var scope = this;

						if (!Ext.isEmpty(controller.onBeforeSave) && Ext.isFunction(controller.onBeforeSave)) {
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
					if (!Ext.isEmpty(controllersArray[0]) && Ext.isFunction(controllersArray[0].onBeforeSave)) {
						controllersArray[0].onBeforeSave(chainArray, 0);
					} else {
						_error('onBeforeSaveTrigger controllersArray head function error', this);
					}
				}
			} else {
				_error('onBeforeSaveTrigger lastCallback function error', this);
			}
		},

		waitForBusyWidgets: function(cb, cbScope) {
			var me = this;

			CMDBuild.core.LoadMask.show();
			this.beforeSaveTriggerManager(
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

		/**
		 * Forwarder method
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Object} widgetsData
		 *
		 * @public
		 */
		getData: function(parameters) {
			var widgetsData = {};

			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					var widgetData = controller.cmfg('getData', parameters);

					if (!Ext.isEmpty(widgetData))
						widgetsData[id] = widgetData;
				} else if (Ext.isFunction(controller.getData)) {
					var widgetData = controller.getData(parameters);

					if (!Ext.isEmpty(widgetData))
						widgetsData[id] = widgetData;
				}
			}, this);

			return widgetsData;
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