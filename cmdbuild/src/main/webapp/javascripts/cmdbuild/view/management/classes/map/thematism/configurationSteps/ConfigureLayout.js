(function() {
	Ext
			.define(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureLayout",
					{
						extend : "Ext.form.Panel",
						itemId : "configureLayout",
						xtype : "form",
						layout : "anchor",
						requires : [
								'CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureShape',
								'CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureSegments',
								'CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureRows' ],

						parentWindow : undefined,
						interactionDocument : undefined,
						controlShape : undefined,
						controlSegments : undefined,
						controlRows : undefined,
						configurationPanel : undefined,

						defaults : {
							anchor : "100%"
						},
						getLayoutConfiguration : function() {
							return this.parentWindow.getLayoutConfiguration();
						},
						getCurrentLayer : function() {
							return this.parentWindow.getCurrentLayer();
						},
						getCurrentSourceType : function() {
							return this.parentWindow.getCurrentSourceType();
						},
						getCurrentLayerType : function() {
							return this.parentWindow.getCurrentLayerType();
						},
						getCurrentStrategy : function() {
							return this.parentWindow.getCurrentStrategy();
						},
						getCurrentAnalysisType : function() {
							return this.parentWindow.getCurrentAnalysisType();
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
							var functionConfiguration = this.parentWindow.getFunctionConfiguration();
							if (configuration.source === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
								strHtml += "<p>@@ Function Name : ";
								strHtml += functionConfiguration.currentStrategy.description;
								strHtml += "</p>";

							} else {
								strHtml += "<p>@@ Function Name : ";
								strHtml += functionConfiguration.currentStrategy.description;
								strHtml += "</p>";
								strHtml += "<p>@@ Field : ";
								strHtml += "/* TODO! */";
								strHtml += "</p>";

							}
							return strHtml;
						},

						getColorsTable : function() {
							var rows = this.controlRows.getColorsTable();
							return rows;
						},

						/**
						 * @param
						 * {CMDBuild.view.management.classes.map.thematism.ThematismMainWindow}
						 * parentWindow
						 * @param {String}
						 *            itemId
						 * 
						 * @returns {Array} extjs items
						 */
						getButtons : function() {
							var itemId = this.itemId;
							var parentWindow = this.parentWindow;
							var me = this;
							return [ {
								text : '@@ Cancel',
								handler : function() {
									parentWindow.close();
								}
							}, {
								text : '@@ Previous',
								handler : function() {
									parentWindow.previous(itemId);
								}
							}, {
								text : '@@ Show',
								formBind : true,
								disabled : true,
								handler : function() {
									var form = this.up('form').getForm();
									var configurationObject = form.getValues();
									configurationObject.colorsTable = me.getColorsTable();
									parentWindow.showOnMap(configurationObject);
								}
							} ];
						},

						init : function() {
							var layoutConfiguration = this.getLayoutConfiguration();
							this.parentWindow.initForm(this, layoutConfiguration);
						},

						/**
						 * @returns {Void}
						 * 
						 * @override
						 */
						initComponent : function() {
							var me = this;
							this.configurationPanel = Ext.create("Ext.panel.Panel", {
								html : ""
							});
							this.initControls();
							Ext.apply(this, {
								items : [ this.configurationPanel, this.controlShape, this.controlSegments,
										this.controlRows ],
								buttons : this.getButtons(),
							});
							this.callParent(arguments);
						},
						initForm : function(form, layoutConfiguration) {
							this.parentWindow.initForm(form, layoutConfiguration);
						},
						initControls : function() {
							this.controlShape = Ext
									.create(
											"CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureShape",
											{
												interactionDocument : this.interactionDocument,
												parentWindow : this
											});
							this.controlSegments = Ext
									.create(
											"CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureSegments",
											{
												interactionDocument : this.interactionDocument,
												parentWindow : this
											});
							this.controlRows = Ext
									.create(
											"CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureRows",
											{
												interactionDocument : this.interactionDocument,
												parentWindow : this
											});

						},
						loadComponents : function(callback, callbackScope) {
							this.configurationPanel.update(this.getHtmlTitle());
							this.controlShape.loadComponents(function() {
								this.controlSegments.loadComponents(function() {
									this.controlRows.loadComponents(function() {
										callback.apply(callbackScope, []);
									}, this);
								}, this);
							}, this);
						}
					});

})();