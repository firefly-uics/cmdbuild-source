(function() {
	Ext.define("CMDBuild.view.management.common.widget.CMWidgetButton", {
		extend: "Ext.Button",

		widgetDefinition: undefined, // pass on instantiation

		constructor: function() {
			this.callParent(arguments);

			var widget = this.widgetDefinition;

			Ext.apply(this, {
				margins:'0 0 5 0',
				text: widget.btnLabel
					|| widget.label
					|| CMDBuild.Translation.management.modworkflow[widget.labelId],
				disabled: !widget.alwaysenabled
			});
		},

		disable: function() {
			if (this.widgetDefinition && this.widgetDefinition.alwaysenabled) {
				return this.enable();
			} else {
				return this.callParent(arguments);
			}

			return this.enable();
		}
	});
})();

Ext.define("CMDBuild.view.management.common.widget.CMWidgetButtonsPanel", {
	extend: "Ext.panel.Panel",
	statics: {
		CMEVENTS: {
			widgetButtonClick: "widget-click"
		}
	},

	initComponent: function() {
		Ext.apply(this, {
			frame: false,
			border: false,
			layout : 'vbox',
			bodyCls: "x-panel-body-default-framed",
			bodyStyle: {
				padding: "30px 5px 0 5px"
			}
		});

		this.callParent(arguments);

		this.CMEVENTS = this.self.CMEVENTS;
	},

	addWidget: function addWidget(widget) {
		var me = this;
		if (me._hidden) {
			me.show();
			me._hidden = false;
		}

		me.add(new CMDBuild.view.management.common.widget.CMWidgetButton({
			widgetDefinition: widget,
			handler: function() {
				me.fireEvent(me.CMEVENTS.widgetButtonClick, widget);
			}
		}));

		me.updateButtonsWidth();
	},

	updateButtonsWidth: function updateButtonsWidth() {
		var maxW = 0;
		var buttons = this.items.items;
		var l = buttons.length;

		for (var i=0; i<l; ++i)  {
			var item = buttons[i];
			var w = item.getWidth();
			if (w > maxW) {
				maxW = w;
			}
		}

		for (var i=0; i<l; ++i)  {
			var item = buttons[i];
			item.setWidth(maxW);
		}

		// to fix the width of the panel, auto width does
		// not work with IE7
		this.setWidth(maxW + 10); // 10 is the pudding
	},

	displayMode: function displayMode() {
		this.items.each(function(i) {
			i.disable();
		});
	},

	editMode: function editMode() {
		this.items.each(function(i) {
			i.enable();
		});
	},

	removeAllButtons: function removeAllButtons() {
		this.removeAll();
		this.hide();
		this._hidden = true;
	}
});