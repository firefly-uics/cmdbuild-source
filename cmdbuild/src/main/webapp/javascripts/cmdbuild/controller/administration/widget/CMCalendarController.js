(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMCalendarController", {

		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMCalendarDefinitionForm.WIDGET_NAME
		},

		constructor: function() {
			this.callParent(arguments);
			var me = this;
			this.view.targetClass.setValue = Ext.Function.createSequence(this.view.targetClass.setValue,
				function(v) {
					onTargetClassChanged(me, v);
				},
				this.view
			);
		}
	});

	function onTargetClassChanged(me, targetClass) {
		if (Ext.isArray(targetClass)) {
			targetClass = targetClass[0];
			if (targetClass.get) {
				targetClass = targetClass.get("id");
			}
		}

		if (targetClass) {
			_CMCache.getAttributeList(targetClass, function(l) {
				fillAttributeStoresWithData(me, l);
			});
		} else {
			fillAttributeStoresWithData(me, []);
		}
	}

	function fillAttributeStoresWithData(me, attributes) {

		function fillStoreByType(store, attributes, allowedTypes) {
			store.removeAll();
			for (var i=0, l=attributes.length; i<l; ++i) {
				var a = attributes[i],
					type = a.type;

				if ((allowedTypes && allowedTypes.indexOf(type) >= 0) 
					|| !allowedTypes) {

					store.add({id: a.name, description: a.description});
				}
			}
		}

		fillStoreByType(me.view.startDate.store, attributes, ["DATE", "TIMESTAMP"]);
		fillStoreByType(me.view.endDate.store, attributes, ["DATE", "TIMESTAMP"]);
		fillStoreByType(me.view.eventTitle.store, attributes, false);
	}
})();