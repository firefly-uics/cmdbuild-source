(function() {
	/**
	 * @link CMDBuild.view.common.field.filter.advanced.window.Window
	 */
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

		comboLayers : undefined,
		comboAttributes : undefined,
		dynamicTitleStep1 : undefined,
		dynamicTitleStep2 : undefined,
		dynamicTitleStep3 : undefined,
		
		analysisType : undefined,
		sourceType : undefined,
		attributeType : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			this.wizard = this.getWizard();
			Ext.apply(this, {
				items : [ this.wizard ]
			});
			this.callParent(arguments);
		},

		listeners : {
			hide : function(panel, eOpts) {
				// this.delegate.cmfg("onPanelGridAndFormFilterAdvancedFilterEditorViewHide");
			},
			show : function(panel, eOpts) {
				this.wizard.getLayout().setActiveItem("step-1");
				this.loadLayers(function() {
				}, this);
				this.loadAttributes(function() {
				}, this);
				this.resetDynamics();
				this.delegate.cmfg("onPanelGridAndFormFilterAdvancedFilterEditorViewShow");
			}
		},
		resetDynamics : function() {
			var card = this.interactionDocument.getCurrentCard();
			if (!card) {
				return;
			}
			var currentClassName = card.className;
			this.dynamicTitleStep1.changeTitle(currentClassName);
			this.dynamicTitleStep2.changeTitle(currentClassName);
			this.dynamicTitleStep3.changeTitle(currentClassName);
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
				items : [

						{
							itemId : "step-1",
							xtype : "form",
							layout : "anchor",
							defaults : {
								anchor : "100%"
							},
							items : this.getStepOneItems(),
							buttons : [ this.getAdvanceButton("@@ Next", "step-2") ]
						},
						{
							itemId : "step-2",
							xtype : "form",
							layout : "anchor",
							defaults : {
								anchor : "100%"
							},
							items : this.getStepTwoItems(),
							buttons : [ this.getAdvanceButton("@@ Previous", "step-1"),
									this.getAdvanceButton("@@ Next", "step-3") ]
						},
						{
							itemId : "step-3",
							xtype : "form",
							layout : "anchor",
							defaults : {
								anchor : "100%"
							},
							items : this.getStepThreeItems(),
								buttons : [ this.getAdvanceButton("@@ Previous", "step-2"),
									this.getShowButton("@@ Show", this) ]
						} ],
			});
			return wizard;
		},
		getStepOneItems : function() {
			this.comboLayers = this.getLayers();
			this.dynamicTitleStep1 = this.getDynamicTitle("@@ Step 1", false, false, false);
			var items = [ this.dynamicTitleStep1, this.getAnalysisType(), this.getSourceType(), this.comboLayers ];
			return items;
		},
		getStepTwoItems : function() {
			this.comboAttributes = this.getAttributes();
			this.dynamicTitleStep2 = this.getDynamicTitle("@@ Step 2 ", true, true, false);
			var items = [ this.dynamicTitleStep2, this.comboAttributes ];
			return items;
		},
		getStepThreeItems : function() {
			this.gridResults = this.getGridResults();
			this.dynamicTitleStep3 = this.getDynamicTitle("@@ Step 3 ", true, true, true);
			var items = [ this.dynamicTitleStep3, this.gridResults ];
			return items;
		},
		getDynamicTitle : function(step, withAnalysisType, withSourceType, withAttributeType) {
			var me = this;
			var item = new Ext.form.field.Display({
				fieldLabel : step,
				name : 'home_score',
				border : 2,
				style : {
					borderColor : 'blue',
					borderStyle : 'solid'
				},
				fieldStyle : 'fontWeight: bold;color: blue;',
				value : '',
				changeTitle : function(title) {
					var analysisDescription = "";
					var sourceDescription = "";
					var attributeDescription = "";
					if (withAnalysisType) {
						analysisDescription = getAnalysisDescription(me.analysisType);
						analysisDescription = "  @@ Analysis Type : " + analysisDescription;
					}
					if (withSourceType) {
						sourceDescription = getSourceDescription(me.sourceType);
						sourceDescription = "  @@ Source Type : " + sourceDescription;
					}
					if (withAttributeType) {
						attributeDescription = me.attributeType;
						attributeDescription = "  @@ Source Type : " + attributeDescription;
					}
					title = "@@ Current Class is " + title + analysisDescription + sourceDescription + attributeDescription;
					this.setValue(title);
				}
			});
			return item;

		},
		getAnalysisType : function() {
			var me = this;
			var item = Ext.create("Ext.form.RadioGroup", {
				fieldLabel : "@@ Analysis type",
				columns : 1,
				vertical : true,
				border : true,
				items : [ {
					boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.RANGES_ANALYSIS),
					name : "analysis",
					inputValue : CMDBuild.gis.constants.layers.RANGES_ANALYSIS
				}, {
					boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS),
					name : "analysis",
					inputValue : CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS,
					checked : true
				}, {
					boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.DENSITY_ANALYSIS),
					name : "analysis",
					inputValue : CMDBuild.gis.constants.layers.DENSITY_ANALYSIS
				} ],
				listeners : {
					change : function(field, newValue, oldValue) {
						me.analysisType = newValue.analysis;
						me.resetDynamics();
					}
				}

			});
			return item;
		},
		getSourceType : function() {
			var me = this;
			var item = Ext.create("Ext.form.RadioGroup", {
				fieldLabel : "@@ Source type",
				columns : 1,
				vertical : true,
				border : true,
				items : [ {
					checked: true,
					boxLabel : "@@ from table",
					name : "source",
					inputValue : CMDBuild.gis.constants.layers.TABLE_SOURCE
				}, {
					boxLabel : "@@ from function",
					name : "source",
					inputValue : CMDBuild.gis.constants.layers.FUNCTION_SOURCE,
				} ],
				listeners : {
					change : function(field, newValue, oldValue) {
						me.sourceType = newValue.source;
						me.resetDynamics();
					}
				}
			});
			return item;
		},
		loadLayers : function(callback, callbackScope) {
			var card = this.interactionDocument.getCurrentCard();
			if (!card) {
				callback.apply(callbackScope, this);
				return;
			}
			var layersStore = Ext.create('Ext.data.Store', {
				fields : [ 'name', 'type' ],
				autoLoad : false,
				data : []
			});
			var currentClassName = card.className;
			var currentCardId = card.cardId;
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i];
					var visible = me.interactionDocument.isVisible(layer, currentClassName, currentCardId);
					if (visible && !me.interactionDocument.isGeoServerLayer(layer)) {
						layersStore.add({
							"name" : layer.name,
							"type" : layer.type
						});
					}
				}
				this.comboLayers.store.loadData(layersStore.getRange(), false);
			}, this);
			callback.apply(callbackScope, this);
		},
		getLayers : function() {
			// Create the combo box, attached to the states data store
			var store = Ext.create('Ext.data.Store', {
				fields : [ 'name', 'type' ],
				data : []
			});

			var item = Ext.create('Ext.form.ComboBox', {
				fieldLabel : '@@ Choose Layer',
				store : store,
				queryMode : 'local',
				displayField : 'name',
				valueField : 'name'
			});
			return item;
		},
		loadAttributes : function(callback, callbackScope) {
			var card = this.interactionDocument.getCurrentCard();
			if (!card) {
				callback.apply(callbackScope, this);
				return;
			}
			var attributesStore = Ext.create('Ext.data.Store', {
				fields : [ 'name', 'type' ],
				autoLoad : false,
				data : []
			});
			var currentClassName = card.className;
			var type = _CMCache.getEntryTypeByName(currentClassName);
			var currentClassId = type.get("id");
			var me = this;
			_CMCache.getAttributeList(currentClassId, function(attributes) {
				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];
					attributesStore.add({
						"name" : attribute.name,
						"type" : attribute.description
					});
				}
				me.comboAttributes.store.loadData(attributesStore.getRange(), false);
			}, this);
			callback.apply(callbackScope, this);
		},
		getAttributes : function() {
			var me = this;
			var store = Ext.create('Ext.data.Store', {
				fields : [ 'name', 'type' ],
				data : []
			});

			// Create the combo box, attached to the states data store
			var item = Ext.create('Ext.form.ComboBox', {
				fieldLabel : 'Choose Attribute',
				store : store,
				queryMode : 'local',
				displayField : 'name',
				valueField : 'name',
				listeners : {
					change : function(field, newValue, oldValue) {
						me.attributeType = newValue;
						me.resetDynamics();
					}
				}
			});
			return item;
		},
		getGridResults : function() {
			Ext.create('Ext.data.Store', {
			    storeId:'simpsonsStore',
			    fields:['name', 'email', 'phone'],
			    data:{'items':[
			        { 'name': 'Lisa',  "email":"lisa@simpsons.com",  "phone":"555-111-1224"  },
			        { 'name': 'Bart',  "email":"bart@simpsons.com",  "phone":"555-222-1234" },
			        { 'name': 'Homer', "email":"homer@simpsons.com",  "phone":"555-222-1244"  },
			        { 'name': 'Marge', "email":"marge@simpsons.com", "phone":"555-222-1254"  }
			    ]},
			    proxy: {
			        type: 'memory',
			        reader: {
			            type: 'json',
			            root: 'items'
			        }
			    }
			});

			var item = Ext.create('Ext.grid.Panel', {
			    title: 'Simpsons',
			    store: Ext.data.StoreManager.lookup('simpsonsStore'),
			    columns: [
			        { text: 'Name',  dataIndex: 'name' },
			        { text: 'Email', dataIndex: 'email', flex: 1 },
			        { text: 'Phone', dataIndex: 'phone' }
			    ],
			    height: 200,
			    width: 400,
			});
			return item;
		},
		getAdvanceButton : function(text, nextTab) {
			var item = {
				text : text,
				handler : function() {
					var wizard = this.up("#wizard");
					var form = this.up("form");

					wizard.getLayout().setActiveItem(nextTab);
				}
			};
			return item;
		},
		getShowButton : function(text, me) {
			var item = {
				text : text,
				handler : function() {
					me.hide();
				}
			};
			return item;
		}
	});
	function getAnalysisDescription(analysisType) {
		switch (analysisType) {
		case CMDBuild.gis.constants.layers.RANGES_ANALYSIS:
			return "@@ Ranges";
		case CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS:
			return "@@ Puntual";
		case CMDBuild.gis.constants.layers.DENSITY_ANALYSIS:
			return "@@ Density";
		}
		return "@@ Puntual";
	}
	function getSourceDescription(sourceType) {
		switch (sourceType) {
		case CMDBuild.gis.constants.layers.TABLE_SOURCE:
			return "@@ Table";
		case CMDBuild.gis.constants.layers.FUNCTION_SOURCE:
			return "@@ Function";
		}
		return "@@ Table";
	}

})();
