(function() {
	var FILTER = "xa:Filter";
	var CLASS_ID = "xa:ClassId";
	var DEFAULT_SELECTION = "xa:DefaultSelection";
	var TABLE_VIEW_NAME = "table";
	var MAP_VIEW_NAME = "map";
	var STARTING_VIEW = TABLE_VIEW_NAME;
	
	CMDBuild.Management.LinkCardsController = function(view) {
		this.singleSelect = view.singleSelect;
		this.currentView = STARTING_VIEW;
		this.alertIfChangeDefaultSelection = false,
		this.view = view;
		this.extAttrDef = view.extAttrDef;
		
		this.model = new CMDBuild.Management.LinkCardsModel({
			singleSelect: this.singleSelect
		});
		
		this.mapController = {};
		this.gridController = new CMDBuild.Management.LinkCards.LinkCardsCardGridController(this.view.cardGrid, this.model);
		
		this.templateResolver = new CMDBuild.Management.TemplateResolver({
			clientForm: view.clientForm,
			xaVars: view.extAttrDef,
			serverVars: view.activity
		});
		addViewEventListeners.call(this, view);
	};
	
	CMDBuild.Management.LinkCardsController.prototype = {
		getData: function() {
			return this.model.getSelections();
		},
		
		onActivityStartEdit: function() {
			resolveTemplate.call(this);
		},
		
		buildCardListMapController: function(map) {
			if (map != null) {
				this.mapController = new CMDBuild.Management.LinkCardsMapController(view=map, ownerController=this, this.model);
			}
		},
		
		isSelected: function(cardId) {
			return this.model.isSelected(cardId);
		},
		
		selectCard: function(cardId) {
			this.model.select(cardId);
		},

		deselectCard: function(cardId) {
			this.model.deselect(cardId);
		}
	};
	
	function addViewEventListeners(view) {
		view.on("CM_show", function(extAttr) {
			this.alertIfChangeDefaultSelection = true;
			var classId = this.templateResolver.getVariable(CLASS_ID);
			var cqlQuery = this.templateResolver.getVariable(FILTER);
			
			if (cqlQuery) {
				this.view.cardGrid.openFilterBtn.disable();
				this.templateResolver.resolveTemplates({
					attributes: [ FILTER ],
					callback: function(out, ctx) {
						var cardReqParams = this.getTemplateResolver().buildCQLQueryParameters(cqlQuery, ctx);					
						this.initGrid(classId, cardReqParams);
					},
					scope: this.view
				});
			} else {
				this.view.initGrid(classId);
			}			
		}, this);
		
		view.on("CM_save", function() {
			if (undefined != view.outputName) {
				var out = {};
				out[view.outputName] = this.getData.call(this);
				view.react(out);
			}
		}, this);
		
		view.on("CM_toggle_map", function() {
			var v = this.view;
			if (v.cardGrid.isVisible()) {
				v.getLayout().setActiveItem(v.mapPanel.id);
				v.mapButton.setIconClass("table");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);				
			} else {
				v.getLayout().setActiveItem(v.cardGrid.id);
				v.mapButton.setIconClass("map");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
				this.gridController.loadPageForLastSelection(this.mapController.getLastSelection());		
			}
		}, this);		
	};

	function resolveTemplate() {
		resolve.call(this);
		
		function resolve() {
			this.view.templateResolverIsBusy = true;
			this.model.reset();
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.Msg.warn(null, String.format(CMDBuild.Translation.warnings.link_cards_changed_values
						, this.view.extAttrDef.ButtonLabel || this.view.id)
						, popup=false);
				this.alertIfChangeDefaultSelection = false;
			}
			this.templateResolver.resolveTemplates({
				attributes: [ 'DefaultSelection' ],
				callback: onTemplateResolved,
				scope: this
			});
		}
		
		function onTemplateResolved(out, ctx) {
			function callback(request, options, response) {
			    var resp = Ext.util.JSON.decode(response.responseText);
			    
			    if (resp.rows) {
				    for ( var i = 0, l = resp.rows.length; i < l; i++) {
					    var r = resp.rows[i];
					    this.model.select(r.Id);
				    }
			    }
			    
			    if (this.view.cardGrid.isVisible()) {
			    	// if the grid is shown while the template-resolver is
			    	// working, the selection can be inconsistently
			    	this.view.cardGrid.reload();
			    }
			    
			    this.view.templateResolverIsBusy = false;
		    }
			
			// do the request only if there are a default selection
			var defaultSelection = this.templateResolver.buildCQLQueryParameters(out.DefaultSelection, ctx);
			if (defaultSelection) {
				CMDBuild.ServiceProxy.getCardList({
					params: defaultSelection,
					callback: callback.createDelegate(this)
				});
				
				addListenerToDeps.call(this);
			} else {
				this.view.templateResolverIsBusy = false;
			}
		}
		
		function addListenerToDeps() {
			var ld = this.templateResolver.getLocalDepsAsField();
			for (var i in ld) {
				//before the blur if the value is changed
				if (ld[i]) {
					ld[i].on('change', resolveTemplate, this, {single: true});
				}
			}
		}			
	}
})();