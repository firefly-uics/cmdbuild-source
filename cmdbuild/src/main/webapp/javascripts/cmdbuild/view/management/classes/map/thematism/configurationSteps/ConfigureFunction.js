(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction", {
		extend : "Ext.form.Panel",
		itemId : "configureFunction",
		xtype : "form",
		layout : "anchor",

		defaults : {
			anchor : "100%"
		},
		parentWindow : undefined,
		interactionDocument : undefined,
		configurationPanel : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			this.configurationPanel = Ext.create("Ext.panel.Panel", {
				html :""
			})
			Ext.apply(this, {
				items : [this.configurationPanel].concat(this.items),
				buttons : [ {
					text : '@@ Cancel',
					handler : function() {
						me.parentWindow.close();
					}
				}, {
					text : '@@ Previous',
					handler : function() {
						var form = this.up('form').getForm();
						me.parentWindow.previous(me.itemId);
					}
				}, {
					text : '@@ Advance',
					formBind : true, // only enabled once the form is valid
					disabled : true,
					handler : function() {
						var form = this.up('form').getForm();
						me.parentWindow.advance(me.itemId, form.getValues());
					}
				} ],
			});
			this.callParent(arguments);
		},
		loadComponents : function(callback, callbackScope) {
			this.configurationPanel.update(this.getHtmlTitle());
			this.loadStrategies(function() {
				callback.apply(callbackScope, []);
			}, this);
		},
		loadStrategies : function(callback, callbackScope) {
				console.log("Abstract class: CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction");
		},
		getHtmlTitle : function(callback, callbackScope) {
			var configuration = this.parentWindow.getThematismConfiguration();
			var strHtml = "<p>@@ Thematic Layer Name : ";
			strHtml += configuration.layerName;
			strHtml += "</p>";
			strHtml += "<p>@@ Analysis Type : ";
			strHtml += this.parentWindow.getAnalysisDescription(configuration.analysis);
			strHtml += "</p>";
			strHtml += "<p>@@ Source Type : ";
			strHtml += this.parentWindow.getSourceDescription(configuration.source);
			strHtml += "</p>";
			return strHtml;
		}
	});
})();
