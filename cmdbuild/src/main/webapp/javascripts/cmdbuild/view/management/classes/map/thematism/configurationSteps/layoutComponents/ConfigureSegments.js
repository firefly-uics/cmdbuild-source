(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureSegments",
			{
				extend : "Ext.panel.Panel",
				itemId : "configureSegments",
				xtype : "form",
				layout : "anchor",

				parentWindow : undefined,
				interactionDocument : undefined,

				/**
				 * @returns {Void}
				 * 
				 * @override
				 */
				initComponent : function() {
					var me = this;
					Ext.apply(this, {
						items : [ {
							xtype : "numberfield",
							fieldLabel : CMDBuild.Translation.thematicSegments,
							name : 'segmentsConfiguration',
							allowBlank : false,
							maxValue : 10,
							minValue : 1,
							listeners : {
								change : function(field, newValue, oldValue, eOpts) {
									var layoutConfiguration = me.getLayoutConfiguration();
									layoutConfiguration.segmentsConfiguration = newValue;
									me.parentWindow.refreshGridColors();
								}
							}

						} ]
					});
					this.callParent(arguments);
				},
				defaults : {
					anchor : "100%"
				},
				getLayoutConfiguration : function() {
					return this.parentWindow.getLayoutConfiguration();
				},
				init : function() {
					var layoutConfiguration = this.getLayoutConfiguration();
					var analysisType = this.parentWindow.getCurrentAnalysisType();
					var isPuntual = (analysisType === CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS);
					if (isPuntual) {
						this.items.getAt(0).hide();
					} else {
						this.items.getAt(0).show();
					}
					if (!layoutConfiguration.segmentsConfiguration) {
						layoutConfiguration.segmentsConfiguration = CMDBuild.gis.constants.DEFAULT_SEGMENTS;
					}
					this.parentWindow.initForm(this, layoutConfiguration);
				},
				loadComponents : function(callback, callbackScope) {
					this.init();
					callback.apply(callbackScope, []);
				}
			});
})();
