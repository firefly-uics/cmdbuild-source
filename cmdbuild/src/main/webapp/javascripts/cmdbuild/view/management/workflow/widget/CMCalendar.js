(function() {
	Ext.override(Extensible.calendar.view.AbstractCalendar, {
		onClick : function(e, t) {
// 				I don't want to return if is read only
//				if (this.readOnly === true) {
//					return true;
//				}

				if (this.dropZone) {
					this.dropZone.clearShims();
				}
				if (this.menuActive === true) {
					// ignore the first click if a context menu is active (let it
					// close)
				this.menuActive = false;
				return true;
			}
			var el = e.getTarget(this.eventSelector, 5);
			if (el) {
				var id = this.getEventIdFromEl(el), rec = this.getEventRecord(id);

// 				I want only the event, not the event editor
				if (this.fireEvent('eventclick', this, rec, el) !== false) {
//					this.showEventEditor(rec, el);
				}
				return true;
			}
		}
	});

	Ext.define("CMDBuild.view.management.workflow.widgets.CMCalendar", {
		extend: "Ext.panel.Panel",
		withButtons: true,
		cmEventsData: [],
		constructor: function(c) {

			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;

			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back
			});

			this.eventStore = new Extensible.calendar.data.MemoryEventStore({
				data : this.cmEventsData
			});

			this.calendar = new Extensible.calendar.CalendarPanel({
				eventStore: this.eventStore,
				title: 'Basic Calendar',
				region: "center",
				frame: false,
				border: false,
				cls: "cmborderbottom",

				showTodayText: true,
				readOnly: true,
				showNavToday: false
			});

			this.mon(this.calendar, "eventclick", function(panel, rec, el) {
				this.fireEvent("eventclick", panel, rec, el);
			}, this);

			this.mon(this.calendar, "viewchange", function(calendar, view, info) {
				this.fireEvent("viewchange", info);
			}, this);

			Ext.apply(this, {
				frame: false,
				border: false,
				items: [this.calendar],
				buttonAlign: "center",
				buttons: [this.backToActivityButton],
				layout: "border",
				cls: "x-panel-body-default-framed"
			});

			this.callParent([this.widgetConf]); // to apply the conf to the panel
		},

		cmActivate: function() {
			this.mon(this.ownerCt, "cmactive", function() {
				this.ownerCt.bringToFront(this);
			}, this, {single: true});

			this.ownerCt.cmActivate();
		},

		addEvent: function(event) {
			this.eventStore.add(event)
		},

		clearStore: function() {
			this.eventStore.removeAll();
		},

		getWievBounds: function() {
			var info;
			if (this.calendar.layout && this.calendar.layout.getActiveItem) {
				var view = this.calendar.layout.getActiveItem();
				if (view) {
					if (view.getViewBounds) {
						var vb = view.getViewBounds();
						info = {
							activeDate: view.getStartDate(),
							viewStart: vb.start,
							viewEnd: vb.end
						};
					};
				}
			}
			return info;
		}
	});
})();