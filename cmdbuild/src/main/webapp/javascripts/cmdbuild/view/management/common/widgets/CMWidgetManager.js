(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManagerDelegate", {
		getFormForTemplateResolver: Ext.emptyFn,
		getWidgetButtonsPanel: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMTabbedWidgetDelegate", {
		getNotesPanel: Ext.emptyFn,
		getAttachmentsPanel: Ext.emptyFn,
		showWidget: Ext.emptyFn,
		activateFirstTab: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManager", {

		requires: ['CMDBuild.view.management.common.widgets.grid.GridView'],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		delegate: undefined,

		/**
		 * @param {CMDBuild.view.management.classes.CMCardPanel or CMDBuild.view.management.workflow.CMActivityPanel} mainView
		 * @param {Mixed} tabbedWidgetDelegate
		 */
		constructor: function(mainView, tabbedWidgetDelegate) {
			this.mainView = mainView;

			this.tabbedWidgetDelegate = tabbedWidgetDelegate || null;

			initBuilders(this);
		},

		buildWidget: function(widget, card) {
			this.mainView.getWidgetButtonsPanel().addWidget(widget);
			return this._buildWidget(widget, card);
		},

		showWidget: function(w, title) {
			if (this.tabbedWidgetDelegate == null
				|| !this.tabbedWidgetDelegate.showWidget(w, title)) {

				this.widgetsContainer.showWidget(w, title);
			}
		},

		hideWidgetsContainer: function() {
			if (this.widgetsContainer) {
				this.widgetsContainer.hide();
			}
		},

		buildWidgetsContainer: function() {
			return Ext.create('CMDBuild.view.management.common.widgets.CMWidgetsWindow', {
				delegate: this.delegate
			});
		},

		reset: function() {
			if (!Ext.isEmpty(this.widgetsContainer))
				this.widgetsContainer.destroy();

			// Email tab configuration reset
			// TODO: find a better implementation
			if (
				!Ext.isEmpty(this.mainView.delegate)
				&& !Ext.isEmpty(this.mainView.delegate.superController)
				&& !Ext.isEmpty(this.mainView.delegate.superController.controllerTabEmail)
				&& Ext.isFunction(this.mainView.delegate.superController.controllerTabEmail.reset)
			) {
				this.mainView.delegate.superController.controllerTabEmail.reset();
			}

			this.widgetsContainer = this.buildWidgetsContainer();

			this.mainView.getWidgetButtonsPanel().removeAllButtons();

			this.widgetsMap = {};
		},

		getFormForTemplateResolver: function getFormForTemplateResolver() {
			return this.mainView.getFormForTemplateResolver();
		},

		activateFirstTab: function() {
			if (this.tabbedWidgetDelegate != null) {
				this.tabbedWidgetDelegate.activateFirstTab();
			}
		},

		_buildWidget: function(widget, card) {
			if (this.builders[widget.type]) {
				return this.builders[widget.type](widget, card);
			} else {
				return null;
			}
		}
	});

	function initBuilders(me) {
		me.builders = {
			// OpenTabs widgets: they have to open a tab in the activityTabPanel, and not a separate window
				'.OpenNote': function(widget, card) {
					var widgetUI = null;
					if (me.tabbedWidgetDelegate) {
						widgetUI = me.tabbedWidgetDelegate.getNotesPanel() || null;

						if (widgetUI != null) {
							widgetUI.configure({
								widget: widget,
								activityInstance: card
							});
						}
					}

					return widgetUI;
				},

				'.OpenAttachment': function(widget, card) {
					var widgetUI = null;
					if (me.tabbedWidgetDelegate) {
						widgetUI = me.tabbedWidgetDelegate.getAttachmentsPanel() || null;

						if (widgetUI != null) {
							widgetUI.configure({
								widget: widget,
								activityInstance: card
							});
						}
					}

					return widgetUI;
				},

				/**
				 * @param {Object} widget
				 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
				 *
				 * @returns {Null}
				 */
				'.ManageEmail': function(widget, card) {
					if (!Ext.isEmpty(me.tabbedWidgetDelegate) && !Ext.isEmpty(me.tabbedWidgetDelegate.getEmailPanel()))
						return me.tabbedWidgetDelegate.getEmailPanel();

					return null;
				},
			// End OpenTabs widgets

			/**
			 * @param {Object} widget
			 * @param {CMDBuild.model.CMActivityInstance} activity
			 *
			 * @returns {CMDBuild.view.management.common.widgets.customForm.CustomFormView}
			 */
			'.CustomForm': function(widget, card) {
				var w = Ext.create('CMDBuild.view.management.common.widgets.customForm.CustomFormView');

				me.widgetsContainer.addWidgt(w);

				return w;
			},

			/**
			 * @param {Object} widget
			 * @param {CMDBuild.model.CMActivityInstance} activity
			 *
			 * @returns {CMDBuild.view.management.common.widgets.grid.GridView}
			 */
			'.Grid': function(widget, card) {
				var w = Ext.create('CMDBuild.view.management.common.widgets.grid.GridView');

				me.widgetsContainer.addWidgt(w);

				return w;
			}
		};

		var pkg = CMDBuild.view.management.common.widgets;

		// createModifyCard
		me.builders[pkg.CMCreateModifyCard.WIDGET_NAME] = function createModifyCardBuilder(widget, card) {
			var w = new pkg.CMCreateModifyCard(widget);
			me.widgetsContainer.addWidgt(w);

			var widgetManager = new pkg.CMWidgetManager(w);
			w.getWidgetManager = function() {
				return widgetManager;
			};

			return w;
		};

		// calendar
		me.builders[pkg.CMCalendar.WIDGET_NAME] = function() {
			var w = new pkg.CMCalendar();
			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// openReport
		me.builders[pkg.CMOpenReport.WIDGET_NAME] = function() {
			var w = new pkg.CMOpenReport();
			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// LinkCards
		me.builders[CMDBuild.view.management.common.widgets.linkCards.LinkCards.WIDGET_NAME] = function(widget, card) {
			var w = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
				widgetConf: widget
			});
			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// ManageRelation
		me.builders['.ManageRelation'] = function(widget, card) {
			var w = Ext.create('CMDBuild.view.management.common.widgets.manageRelation.CMManageRelation', {
				widget: widget
			});

			me.widgetsContainer.addWidgt(w);

			return w;
		};

		// workflow
		me.builders[pkg.CMWorkflow.WIDGET_NAME] = function(widget, card) {
			var w = new pkg.CMWorkflow();
			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// navigationTree
		me.builders[pkg.CMNavigationTree.WIDGET_NAME] = function(widget, card) {
			var w = new pkg.CMNavigationTree();
			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// ping
		me.builders[pkg.CMPing.WIDGET_NAME] = function(widget, card) {
			var w = new pkg.CMPing();
			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// WebService
		me.builders[pkg.CMWebService.WIDGET_NAME] = function(widget, card) {
			var w = new pkg.CMWebService();
			me.widgetsContainer.addWidgt(w);
			return w;
		};

		// Preset from card
		me.builders[pkg.CMPresetFromCard.WIDGET_NAME] = function(widget, card) {
			var w = new pkg.CMPresetFromCard();
			me.widgetsContainer.addWidgt(w);
			return w;
		};
	}

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManagerPopup", {
		extend: "CMDBuild.view.management.common.widgets.CMWidgetManager",
			buildWidgetsContainer: function() {
				return new CMDBuild.view.management.common.widgets.CMWidgetsWindowPopup();
			}
	});

})();