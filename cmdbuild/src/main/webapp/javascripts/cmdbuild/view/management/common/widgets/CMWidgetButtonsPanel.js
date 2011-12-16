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

		me.add(new Ext.Button({
			text: widget.btnLabel || widget.label
				|| CMDBuild.Translation.management.modworkflow[widget.labelId],
			widgetDefinition: widget,
			disabled: true,
			handler: function() {
				me.fireEvent(me.CMEVENTS.widgetButtonClick, widget);
			},
			margins:'0 0 5 0'
		}));

		me.updateButtonsWidth();
	},

	updateButtonsWidth: function updateButtonsWidth() {
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