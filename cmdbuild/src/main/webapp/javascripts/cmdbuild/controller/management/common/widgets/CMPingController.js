(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMPingController", {
		mixins: {
			observable: 'Ext.util.Observable'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMPing.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			if (typeof view != "object") {
				throw "The view of a WFWidgetController must be an object"
			}

			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.view = view;
			this.ownerController = ownerController;
			this.widget = this.view.widgetConf || widgetDef;
			this.clientForm = clientForm;
			this.card = card;
		},

		beforeActiveView: function() {
			var me = this;
			me.view.removeAll();

			if (!me.templateResolver) {
				var xaVars = me.widget.templates || {};
				xaVars["_address"] = me.widget.address;

				me.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: me.card.raw || me.card.data
				});
			}

			resolveTemplate(me);
		},

		destroy: function() {
			this.callParent(arguments);
		}
	});

	function resolveTemplate(me) {
		me.templateResolver.resolveTemplates({
			attributes: ["_address"],
			callback: function(o) {

				var pingParams = {
					IdClass: me.card.get("IdClass"),
					Id: me.card.get("Id"),
					widgetId: me.widget.id,
					action: "legacytr",
					params: Ext.encode({
						address: o._address
					})
				};

				var el = me.view.getEl();
				if (el) {
					el.mask(CMDBuild.Translation.common.wait_title);
				}

				CMDBuild.Ajax.request({
					url: "services/json/management/modcard/callwidget",
					method: "GET",
					params: pingParams,
					success: function(request, action, response) {
						me.view.showPingResult(response.response);
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
