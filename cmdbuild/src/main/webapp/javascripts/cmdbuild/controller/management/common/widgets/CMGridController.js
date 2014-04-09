(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMGridController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMGrid.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);
			this.ownerController = ownerController;
			this.classType = _CMCache.getEntryTypeByName(widgetDef.className);
			this.view = view;
			this.view.delegate = this;

			var me = this;
			this.view.loadAttributes( //
				this.classType.get("id"), //
				function(attributes) { //
					me.view.setColumnsForClass(attributes);
				} //
			);

		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onAdd" :
					this.view.newRow();
					break;
				case "onDelete" :
					alert("onDelete");
					break;
				case "onEdit" :
					alert("onEdit");
					break;
				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate === 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
			return undefined;
		},
		
		getCurrentClass: function() {
			return this.classType;
		},

		// override
		beforeActiveView: function() {
		},

		// override
		getData: function() {
			var out = null;
			if (!this.readOnly) {
				out = {};
				out["output"] = this.view.getData();
			}

			return out;
		},

		destroy: function() {
			this.callParent(arguments);
		}
	});
})();
