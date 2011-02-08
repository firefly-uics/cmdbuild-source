(function() {

var FILTER = "xa:Filter";
var CLASS_ID = "xa:ClassId";
var DEFAULT_SELECTION = "xa:DefaultSelection";

CMDBuild.Management.LinkCards = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
	singleSelect: false,
    isFirstLoad: true, // flag to identify the first loading to clear the grid filter
    currentSelection: [],
    alertIfChangeDefaultSelection: false,
    
    initialize : function(extAttrDef) {
		this.outputName = extAttrDef.outputName;
		this.singleSelect = extAttrDef.SingleSelect ? true : false;
		this.noSelect = extAttrDef.NoSelect ? true : false;
		this.currentSelection = [];
		this.buildCardListGrid(extAttrDef);
		
		return {
			layout : 'fit',
			items : [ this.cardGrid ]
		};
	},

	onExtAttrShow : function(extAttr) {
		var classId = this.getVariable(CLASS_ID);
		var cqlQuery = this.getVariable(FILTER);
		this.alertIfChangeDefaultSelection = true;
		
		if (cqlQuery) {
			_debug('filter with cql: ' + cqlQuery);
			this.cardGrid.openFilterBtn.disable();
			this.resolveTemplates( {
				attributes : [ 'Filter' ],
				callback : function(out, ctx) {
					var cardReqParams = this.getTemplateResolver().buildCQLQueryParameters(cqlQuery, ctx);					
					this.initGrid(classId, cardReqParams);
				},
				scope : this
			});
		} else {
			_debug('filter whole class: ' + this.getVariable("xa:ClassName"));
			this.initGrid(classId);
		}
	},
	
	initGrid : function(classId, cardReqParams) {
		var grid = this.cardGrid;
		CMDBuild.Management.FieldManager.loadAttributes(
				classId, function(classAttrs) {
					var eventParams = {
						classId : classId,
						classAttributes : classAttrs
					};
					grid.initForClass(eventParams, cardReqParams);
				});
	},

	onSave : function() {
		if (undefined == this.outputName) {
			fn(this.identifier, true);
			return;
		}
		var out = {};
		out[this.outputName] = this.getData();
		this.react(out);
	},

	getData : function() {
		return this.currentSelection;
	},

	selectCard : function(cardId) {
		if (this.singleSelect) {
			this.currentSelection = [ cardId ];
		} else {
			if (this.currentSelection.indexOf(cardId) < 0) {
				this.currentSelection.push(cardId);
			}
		}
	},

	deselectCard : function(cardId) {
		this.currentSelection.remove(cardId);
	},

	buildCardListGrid : function(extAttrDef) {
		var outName = this.outputName;
		var _this = this;
		var sm;
		if (!this.noSelect) {
			sm = new Ext.grid.CheckboxSelectionModel({
				header : '&nbsp',
				checkOnly: true,
				singleSelect: this.singleSelect
			});
			sm.on('rowdeselect', function(sm, rowIndex, record) {
				_this.deselectCard(record.get("Id"));
			});
			sm.on('rowselect', function(sm, rowIndex, record) {
				_this.selectCard(record.get("Id"));
			});
		}
		this.cardGrid = new CMDBuild.Management.CardListGrid({
			subscribeToEvents : false,
			autoScroll : true,
			noSelect : this.noSelect,
			filterSubcategory : this.identifier,
			sm: sm,
			disableSelection: this.noSelect,
			setColumnsForClass : function(classAttributes) {
				var headers = [];
				for (var i = 0; i < classAttributes.length; i++) {
					var attribute = classAttributes[i];
					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
					if (header) {
						headers.push(header);
					}
				}
				var graphHeader = {
					header : '&nbsp',
					width : 20,
					fixed : true,
					sortable : false,
					renderer : this.renderGraphIcon,
					align : 'center',
					cellCls : 'grid-button',
					dataIndex : 'Id',
					menuDisabled : true,
					id : 'imagecolumn',
					hideable : false
				};
				headers.push(graphHeader);
				if (sm) {
					headers.push(sm);
				}
				this.setColumns(headers);
			},
			loadCards : function(position) {
				var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
				var page = position ? parseInt(position / pageSize) : 0;
				this.getStore().load({
					params : {
						start : page * pageSize,
						limit : pageSize,
						ForceResetFilter : _this.isFirstLoad
					},
					scope : this
				});
				_this.isFirstLoad = false;
			},
			onRowSelect: Ext.emptyFn,
			applySelection: function() {
				if (sm) {
					var store = this.getStore();
					sm.suspendEvents();
					for (var i=0, l=_this.currentSelection.length; i<l; ++i) {
						var cardId = _this.currentSelection[i];
						var recIndex = store.find("Id", cardId);
						if (recIndex >= 0) {
							sm.selectRow(recIndex, true);
						}
					}
					sm.resumeEvents();
				}
			},
			onRowDoubleClick: Ext.emptyFn
		});
	},
	
	onActivityStartEdit: function() {
		resolveTemplate.call(this);
	},
	
	/**
	 * @override
	 */
	isBusy: function() {
		return this.templateResolverIsBusy || CMDBuild.Management.LinkCards.superclass.isBusy.call(this);
	}
});

Ext.reg("linkCards", CMDBuild.Management.LinkCards);
	
	function resolveTemplate() {
		this.templateResolver = this.getTemplateResolver();
		resolve.call(this);
		
		function resolve() {
			this.templateResolverIsBusy = true;
			this.currentSelection = [];
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.Msg.warn(null, String.format(CMDBuild.Translation.warnings.link_cards_changed_values
						, this.extAttrDef.ButtonLabel || this.id)
						, popup=false);
				this.alertIfChangeDefaultSelection = false;
			}
			this.templateResolver.resolveTemplates( {
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
					    this.currentSelection.push(r.Id);
				    }
			    }
			    this.templateResolverIsBusy = false;
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
				this.templateResolverIsBusy = false;
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