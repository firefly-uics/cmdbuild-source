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
		functionsConfiguration : undefined,
		individualLayoutConfiguration : undefined,
		rangeLayoutConfiguration : undefined,

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
			this.configureIndividualLayout = Ext.create(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureIndividualLayout", {
						interactionDocument : this.interactionDocument,
						parentWindow : this
					});
			this.configureRangeLayout = Ext.create(
					"CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureRangeLayout", {
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
						this.configureIndividualLayout, this.configureRangeLayout, this.result ]

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
		getResultFormType : function() {
			var sourceLayer = this.thematismConfiguration.sourceLayer;
			return "result";
		},
		advanceResults : function() {
			this.interactionDocument.getLayerByName(this.configureThematism.sourceLayer, function(layer) {
				this.result.loadComponents(function() {
					var resultsForm = this.getResultFormType();
					this.wizard.getLayout().setActiveItem(resultsForm);
				}, this);

			}, this);
		},
		advanceConfigurationLayouts : function() {
			if (this.thematismConfiguration.analysis === CMDBuild.gis.constants.layers.RANGES_ANALYSIS) {
				this.configureRangeLayout.loadComponents(function() {
					this.wizard.getLayout().setActiveItem("configureRangeLayout");

				}, this);
			} else {
				this.configureIndividualLayout.loadComponents(function() {
					this.wizard.getLayout().setActiveItem("configureIndividualLayout");

				}, this);
			}
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
			case "configureFieldFunction":
			case "configureSourceFunction":
				this.functionConfiguration = configurationObject;
				this.advanceConfigurationLayouts();
				break;
			case "configureRangeLayout":
				this.rangeLayoutConfiguration = configurationObject;
				this.advanceResults();
				break;
			case "configureIndividualLayout":
				this.individualLayoutConfiguration = configurationObject;
				this.advanceResults();
				break;
			}
		},
		previous : function(step) {
			switch (step) {
			case "configureSourceFunction":
			case "configureFieldFunction":
				this.wizard.getLayout().setActiveItem("configureThematism");
				break;
			case "configureRangeLayout":
			case "configureIndividualLayout":
				if (this.thematismConfiguration.source === CMDBuild.gis.constants.layers.FUNCTION_SOURCE) {
					this.wizard.getLayout().setActiveItem("configureSourceFunction");
				} else {
					this.wizard.getLayout().setActiveItem("configureFieldFunction");
				}
				break;
			case "result":
				if (this.thematismConfiguration.analysis === CMDBuild.gis.constants.layers.RANGES_ANALYSIS) {
					this.wizard.getLayout().setActiveItem("configureRangeLayout");
				}
				else {
					this.wizard.getLayout().setActiveItem("configureIndividualLayout");
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
