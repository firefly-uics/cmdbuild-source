(function() {

	var BASE_TITLE = CMDBuild.Translation.administration.modDashboard.title;
		TITLE_SUFFIX_SEPARATOR = " - ";

	// interface of the view
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboardInterface", {
		cmName:'dashboard',
		setTitleSuffix: Ext.emptyFn,
		getPropertiesPanel: Ext.emptyFn,
		setDelegate: Ext.emptyFn,
		toString: function() {
			return Ext.getClassName(this);
		}
	})

	// interface of the view delegate
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboardDelegate", {
		onAddButtonClick: Ext.emptyFn
	});

	// view implementation
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboard", {
		extend: "Ext.panel.Panel",

		mixins: {
			cminterface: "CMDBuild.view.administration.dashboard.CMModDashboardInterface"
		},

		constructor: function() {
			this.callParent(arguments);
			this.delegate = new CMDBuild.view.administration.dashboard.CMModDashboardDelegate();
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMModDashboardDelegate");
			this.delegate = d;
		},

		initComponent : function() {
			var me = this;
			this.addButton = new Ext.button.Button({
				text: CMDBuild.Translation.administration.modDashboard.properties.add,
				iconCls: "add",
				handler: function() {
					me.delegate.onAddButtonClick();
				}
			});

			Ext.apply(this, {
				title: BASE_TITLE,
				layout: "border",
				frame: false,
				border: true,
				items: [{
					xtype: "tabpanel",
					region: "center",
					border: false,
					frame: false,
					items: [{
						xtype: "dashboardproperties"
					}]
				}],
				tbar: [this.addButton]
			});

			this.callParent(arguments);
		},

		setTitleSuffix: function(suffix) {
			var title = BASE_TITLE;
			if (suffix) {
				title += TITLE_SUFFIX_SEPARATOR + suffix;
			}

			this.setTitle(title);
		},

		getPropertiesPanel: function() {
			var tabPanel = this.items.get(0);
			var panel;
			if (tabPanel) {
				panel = tabPanel.items.get(0);
			}

			return panel;
		}
	});

})();