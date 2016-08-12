(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.Result", {
		extend : "Ext.form.Panel",
		itemId : "result",
		xtype : "form",
		layout : "anchor",

		defaults : {
			anchor : "100%"
		},
		parentWindow : undefined,
		interactionDocument : undefined,
		strategiesStore : undefined,
		comboStrategies : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			var store = Ext.create("Ext.data.Store", {
				fields : [ "name", "description" ],
				data : []
			});
			this.grid = Ext.create("Ext.grid.Panel", {
				title : "Results",
				store : store,
				columns : [ {
					text : "Description",
					dataIndex : "description"
				}, {
					text : "Value",
					dataIndex : "value",
					flex : 1
				}, {
					text : "Cardinality",
					dataIndex : "cardinality"
				}, {
					text : "Color",
					dataIndex : "color",
					renderer : function(value, metaData) {
						metaData.style = "background-color:" + value + ";";
						return value;
					}
				} ],
				height : "100%",
				width : "100%"
			});
			// this panel only for have scroolbars on the Grid
			var item = Ext.create('Ext.panel.Panel', {
				title : 'parent container',
				width : 800,
				height : 600,
				autoScroll : "true",
				layout : 'fit',
				items : [ this.grid ]
			});
			Ext.apply(this, {
				items : [item],
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
					text : '@@ Show',
					formBind : true, // only enabled once the form is valid
					disabled : true,
					handler : function() {
						me.parentWindow.showOnMap();
					}
				} ],
			});
			this.callParent(arguments);
		},
		loadComponents : function(callback, callbackScope) {
			this.refreshResults(this.grid, function() {
				callback.apply(callbackScope, []);
			}, this);
		},
		refreshResultsCardByCard : function(cards, cardsArray, index, callback, callbackScope) {
			if (index >= cards.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var card = this.interactionDocument.getCurrentCard();
			var currentClassName = (!card) ? "" : card.className;
			var cardId = cards[index];
			this.loadCard(cardId, currentClassName, function(response) {
				cardsArray.push(response);
				this.refreshResultsCardByCard(cards, cardsArray, index + 1, callback, callbackScope);
			}, this);
		},
		chargeCardByCard : function(groups, cardsArray, index, callback, callbackScope) {
			var currentStrategy = this.parentWindow.getCurrentStrategy();
			if (index >= cardsArray.length || !currentStrategy) {
				callback.apply(callbackScope, []);
				return;
			}
			var card = cardsArray[index];
			var params = {
				card : card,
				strategy : currentStrategy
			}
			currentStrategy.value(params, function(value) {
				if (groups[value]) {
					// can be different from the cards count?
					groups[value].count++;
					groups[value].cards.push(card);
				} else {
					groups[value] = {
						count : 1,
						cards : [ card ]
					};
				}
				this.chargeCardByCard(groups, cardsArray, index + 1, callback, callbackScope);
			}, this);
		},
		chargeStore : function(cardsStore, cardsArray, callback, callbackScope) {
			var groups = {};
			var currentStrategy = this.parentWindow.getCurrentStrategy();
			this.chargeCardByCard(groups, cardsArray, 0, function() {
				for ( var key in groups) {
					cardsStore.add({
						description : currentStrategy.description,
						value : key,
						cardinality : groups[key].count,
						cards : groups[key].cards,
						color : getColor(key)
					});
				}
				callback.apply(callbackScope, []);
			}, this);
		},
		refreshResults : function(grid, callback, callbackScope) {
			var layer = this.interactionDocument.getGeoLayerByName(this.parentWindow.getCurrentLayer());
			var cards = [];
			if (layer) {
				cards = layer.get("adapter").getCardsOnLayer();
			}
			var card = this.interactionDocument.getCurrentCard();
			var currentClassName = (!card) ? "" : card.className;
			var currentResultsStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value", "cardinality", "color" ],
				autoLoad : false,
				data : []
			});
			var cardsArray = [];
			this.refreshResultsCardByCard(cards, cardsArray, 0, function() {
				this.chargeStore(currentResultsStore, cardsArray, function() {
					grid.getStore().loadData(currentResultsStore.getRange(), false);
					callback.apply(callbackScope, this);
				}, this);
			}, this);
		},
		loadCard : function(id, className, callback, callbackScope) {
			if (!params) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CARD_ID] = id;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;
			}

			CMDBuild.proxy.Card.read({
				params : params,
				loadMask : false,
				success : function(result, options, decodedResult) {
					var data = decodedResult.card;
					callback.apply(callbackScope, [ data ]);
				}
			});
		}
	});
	function getColor(value) {
		var red = value * 10;
		var color = "rgb(" + red + ", 100, 100)";
		return color;
	}
})();
