(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMWebServiceController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMWebService.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.wsCallParameters = widgetDef.callParameters;
			this.loaded = false;
		},

		// override
		beforeActiveView: function() {
			if (this.loaded) {
				return;
			}

			var me = this;
			me.view.removeAll();

			if (!me.templateResolver) {
				me.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: me.wsCallParameters,
					serverVars: this.getTemplateResolverServerVars()
				});
			}

			resolveTemplate(me);
		},

		// override
		getData: function() {
			return {
				output: this.view.getSelectedNodes()
			};
		},

		// override
		destroy: function() {
			this.callParent(arguments);
		}
	});

	function resolveTemplate(me) {
		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.wsCallParameters),
			callback: function(o) {
				var vars = me.getTemplateResolverServerVars();
				var entryTypeName = _CMCache.getEntryTypeNameById(vars.IdClass);

				var callParameters = {};
				for (var key in me.wsCallParameters) {
					callParameters[key] = o[key];
				}

				var el = me.view.getEl();
				if (el) {
					el.mask(CMDBuild.Translation.common.wait_title);
				}

				CMDBuild.Ajax.request({
					url: "services/json/widget/callwidget",
					method: "GET",
					params: {
						className: entryTypeName,
						id: vars.Id,
						activityId: _CMWFState.getActivityInstance().getId(),
						widgetId: me.getWidgetId(),
						params: Ext.encode(callParameters)
					},
					success: function(request, action, response) {
						me.loaded = true;
						var xmlString = response.response || "";
						var selectableNodeName = me.widgetConf.selectableNodeName;

						me.view.showActionResponse(xmlString, selectableNodeName);
						me.templateResolver.bindLocalDepsChange(function() {
							me.loaded = false;
						});
					},
					callback: function() {
						if (el) {
							el.unmask();
						}
					}
				});
			}
		});
	}
})();
