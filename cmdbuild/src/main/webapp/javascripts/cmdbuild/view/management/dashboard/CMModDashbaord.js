(function() {

	Ext.override(Ext.app.PortalPanel, {
		/**
		 * override to fix a bug: crash if no configured columns
		 */
		beforeLayout : function() {
			var items = this.layout.getLayoutItems(), len = items.length, i = 0, item;

			for (; i < len; i++) {
				item = items[i];
				// item.columnWidth = 1 / len;
				item.removeCls( [ 'x-portal-column-first', 'x-portal-column-last' ]);
			}

			// CMDBUild patch
			if (len > 0) {
				items[0].addCls('x-portal-column-first');
				items[len - 1].addCls('x-portal-column-last');
			}
			// end CMDBUild patch

			return this.callParent(arguments);
		}
	});

	Ext.define("CMDBuild.view.management.dashboard.CMModDashboard", {

		extend: "Ext.panel.Panel",
		cmName: "dashboard",

		initComponent: function() {
			this.layout = "card";
			this.items = [{xtype: "panel"}];
			this.renderdDashboards = {};
			this.dashbaord = null;
			this.callParent(arguments);
		},

		buildDashboardColumns: function(dashboard) {
			if (dashboard) {

				updateTitle(this, dashboard.get("description"));

				if (this.renderdDashboards[dashboard.getId()]) {
					this.getLayout().setActiveItem(this.renderdDashboards[dashboard.getId()]);
				} else {
					
					var columnsConf = dashboard.getColumns();
					var columns = [];
					var me = this;
	
					this.dashbaord = dashboard;
	
					for (var i=0, l=columnsConf.length, conf; i<l; ++i) {
						conf = columnsConf[i];
	
						columns.push(new CMDBuild.view.management.dashboard.CMDashboardColumn({
							columnWidth : conf.width,
							charts: conf.charts,
							items: [],
							split: true,
							listeners : {
								render: function(column) {
									if (me.delegate) {
										me.delegate.onColumnRender(column);
									}
								}
							}
						}));
					}

					var newDashboard = new Ext.app.PortalPanel({
						items: columns
					});

					this.renderdDashboards[dashboard.getId()] = newDashboard;
					this.add(newDashboard);
					this.getLayout().setActiveItem(newDashboard);
				}
			}
		},

		setDelegate: function(d) {
			this.delegate = d;
		}
	});

	Ext.define("CMDBuild.view.management.dashboard.CMDashboardColumn", {
		extend: "Ext.app.PortalColumn",
		addChart: function(chartConf, store) {
			if (chartConf.isActive()) {
				var c = new CMDBuild.view.management.dashboard.CMChartPortlet({
					chartConfiguration: chartConf,
					store: store
				});

				this.add(c);
				return c;
			}

			return null;
		}
	});

	function updateTitle(me, dashboardName) {
		var title =  CMDBuild.Translation.administration.modDashboard.title;
		var titleSeparator = " - ";

		if (dashboardName) {
			title += titleSeparator + dashboardName;
		}

		me.setTitle(title);
	}
})();