(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.ThematismMainWindow", {
		extend : "CMDBuild.core.window.AbstractCustomModal",

		/**
		 * @cfg {CMDBuild.controller.management.classes.map.thematism.ThematismMainWindow}
		 */
		delegate : undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle : CMDBuild.Translation.searchFilter,

		/**
		 * @cfg {String}
		 */
		dimensionsMode : "percentage",

		/**
		 * @property {Ext.tab.Panel}
		 */
		wrapper : undefined,

		border : true,
		closeAction : "hide",
		frame : true,
		layout : "fit",

		thematismConfiguration : undefined,
		sourceFunctionsConfiguration : undefined,
		sourceFieldConfiguration : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			this.configureThematism = Ext.create(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureThematism", {
						interactionDocument : this.interactionDocument,
						parentWindow : this
					});
			this.configureSourceFunction = Ext.create(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureSourceFunction", {
						interactionDocument : this.interactionDocument,
						parentWindow : this
					});
			this.configureFieldFunction = Ext.create(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFieldFunction", {
						interactionDocument : this.interactionDocument,
						parentWindow : this
					});
			this.result = Ext.create("CMDBuild.view.management.classes.map.thematism.configurationSteps.Result", {
				interactionDocument : this.interactionDocument,
				parentWindow : this
			});
			this.wizard = this.getWizard();
			Ext.apply(this, {
				items : [ this.wizard ]
			});
			this.callParent(arguments);
		},

		listeners : {
			hide : function(panel, eOpts) {
			},
			show : function(panel, eOpts) {
				this.configureThematism.loadComponents(function() {
					this.wizard.getLayout().setActiveItem("configureThematism");
				}, this);
			}
		},
		getWizard : function() {
			var wizard = Ext.widget("panel", {
				title : "",
				border : false,
				itemId : "wizard",
				width : "100%",
				height : "100%",
				layout : "card",
				defaults : {
					border : false,
					bodyPadding : 20
				},
				items : [ this.configureThematism, this.configureSourceFunction, this.configureFieldFunction,
						this.result ]

			});
			return wizard;
		},
		getCurrentStrategy : function() {
			return this.functionConfiguration.currentStrategy;
		},
		getCurrentLayer : function() {
			return this.thematismConfiguration.sourceLayer;
		},
		getLayerName : function() {
			return this.thematismConfiguration.layerName;
		},
		close : function() {
			this.hide();
		},
		advance : function(step, configurationObject) {
			switch (step) {
			case "configureThematism":
				this.thematismConfiguration = configurationObject;
				if (this.thematismConfiguration.source === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
					this.configureSourceFunction.loadComponents(function() {
						this.wizard.getLayout().setActiveItem("configureSourceFunction");

					}, this);
				} else {
					this.configureFieldFunction.loadComponents(function() {
						this.wizard.getLayout().setActiveItem("configureFieldFunction");

					}, this);

				}
				break;
			case "configureSourceFunction":
			case "configureFieldFunction":
				this.functionConfiguration = configurationObject;
				this.result.loadComponents(function() {
					this.wizard.getLayout().setActiveItem("result");

				}, this);
				break;
			}
		},
		previous : function(step) {
			switch (step) {
			case "configureSourceFunction":
			case "configureFieldFunction":
				this.wizard.getLayout().setActiveItem("configureThematism");
				break;
			case "result":
				if (this.thematismConfiguration.source === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
					this.wizard.getLayout().setActiveItem("configureSourceFunction");
				} else {
					this.wizard.getLayout().setActiveItem("configureFieldFunction");
				}
				break;
			}
		},
		showOnMap : function() {
			this.delegate.cmfg('onShowThematism', {
				name : this.getLayerName(),
				layer : this.interactionDocument.getGeoLayerByName(this.getCurrentLayer()),
				strategy : this.getCurrentStrategy()
			});
			this.hide();
			
		}

	});
	// thematismConfiguration : undefined,
	// sourceFunctionsConfiguration : undefined,
	// sourceFieldConfiguration : undefined,

})();
