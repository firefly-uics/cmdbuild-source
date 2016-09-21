(function() {
	Ext
			.define(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction",
					{
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
							this.configurationPanel = Ext.create("Ext.panel.Panel", {
								html : ""
							});
							Ext.apply(this, {
								items : [ this.configurationPanel ].concat(this.items),
								buttons : getButtons(this.parentWindow, this.itemId),
							});
							this.callParent(arguments);
						},
						loadComponents : function(callback, callbackScope) {
							this.configurationPanel.update(this.getHtmlTitle());
							this.loadStrategies(function() {
								this.init();
								callback.apply(callbackScope, []);
							}, this);
						},
						init : function() {
							var functionConfiguration = this.parentWindow.getFunctionConfiguration();
							this.parentWindow.initForm(this, functionConfiguration);
						},
						loadStrategies : function(callback, callbackScope) {
							console
									.log("Abstract class: CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction");
						},
						getHtmlTitle : function() {
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

	/**
	 * @param
	 * {CMDBuild.view.management.classes.map.thematism.ThematismMainWindow}
	 * parentWindow
	 * @param {String}
	 *            itemId
	 * 
	 * @returns {Array} extjs items
	 */
	function getButtons(parentWindow, itemId) {
		return [ {
			text : '@@ Cancel',
			handler : function() {
				parentWindow.close();
			}
		}, {
			text : '@@ Previous',
			handler : function() {
				var form = this.up('form').getForm();
				parentWindow.previous(itemId);
			}
		}, {
			text : '@@ Advance',
			formBind : true,
			disabled : true,
			handler : function() {
				var form = this.up('form').getForm();
				parentWindow.advance(itemId, form.getValues());
			}
		} ];
	}
})();
