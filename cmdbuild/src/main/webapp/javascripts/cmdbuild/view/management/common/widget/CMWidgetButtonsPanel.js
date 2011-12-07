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

	updateWidgets: function(widgets) {
		var me = this;
		this.removeAll();

		if (widgets.length > 0) {
			this.show();
			Ext.each(widgets, function(item) {
				me.add(new Ext.Button({
					text: item.btnLabel || CMDBuild.Translation.management.modworkflow[item.labelId],
					widgetDefinition: item,
					disabled: true,
					handler: function() {
						me.fireEvent(me.CMEVENTS.widgetButtonClick, item);
					},
					margins:'0 0 5 0'
				}));
			});
			me.updateButtonsWidth();
		} else {
			this.hide();
		}
	},

	updateButtonsWidth: function () {
		var maxW = 0;
		this.items.each(function(item) {
			var w = item.getWidth();
			if (w > maxW) {
				maxW = w;
			}
		});

		this.items.each(function(item) {
			item.setWidth(maxW);
		});
		// to fix the width of the panel, auto width does
		// not work with IE7
		this.setWidth(maxW + 10); // 10 is the pudding
	},

	displayMode: function() {
		this.items.each(function(i) {
			i.disable();
		});
	},

	editMode: function() {
		this.items.each(function(i) {
			i.enable();
		});
	}
});