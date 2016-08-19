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

						/**
						 * @returns {Void}
						 * 
						 * @override
						 */
						initComponent : function() {
							var me = this;
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
							Ext.apply(this, {
								items : [ this.controlShape, this.controlSegments, this.controlRows ],
								buttons : [ {
									text : '@@ Cancel',
									handler : function() {
										me.parentWindow.close();
									}
								}, {
									text : '@@ Previous',
									handler : function() {
										// var form = this.up('form').getForm();
										me.parentWindow.previous(me.itemId);
									}
								}, {
									text : '@@ Show',
									formBind : true, // only enabled once the
														// form is valid
									disabled : true,
									handler : function() {
										var form = this.up('form').getForm();
										var configurationObject = form.getValues();

										me.showOnMap(configurationObject);
									}
								} ],
							});
							this.callParent(arguments);
						},
						defaults : {
							anchor : "100%"
						},
						loadComponents : function(callback, callbackScope) {
							this.controlShape.loadComponents(function() {
								this.controlSegments.loadComponents(function() {
									this.controlRows.loadComponents(function() {
										callback.apply(callbackScope, []);
									}, this);
								}, this);
							}, this);
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
						showOnMap : function(configurationObject) {
							return this.parentWindow.showOnMap(configurationObject);
						}

					});
})();