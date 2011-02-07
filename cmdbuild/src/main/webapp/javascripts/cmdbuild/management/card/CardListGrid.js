(function() {
	function buildGraphIconColumn(headers) {
		var cachedTable = CMDBuild.Cache.getTableById(this.currentClassId);
		if (cachedTable && 
				cachedTable.tableType != "simpletable") {
			var graphHeader = {	
				header: '&nbsp', 
				width: 30, 
				fixed: true, 
				sortable: false, 
				renderer: this.renderGraphIcon, 
				align: 'center', 
				dataIndex: 'Id',
				menuDisabled: true,
				id: 'imagecolumn',
				hideable: false
			};
			headers.push(graphHeader);
		}
	};
	
	function buildClassColumn(headers) {
		var classColumn = {
			header: CMDBuild.Translation.management.modcard.subclass,
			width: 40,
			fixed: false,
			sortable: true,
			dataIndex: 'IdClass_value'
		};
		headers.push(classColumn);	
	}
/**
 * This is the Grid Panel that contains the card list of the selected class
 * 
 * @class CMDBuild.Management.CardListGrid
 * @extends CMDBuild.Grid
 */
CMDBuild.Management.CardListGrid = Ext.extend(CMDBuild.Grid, {
	baseUrl : 'services/json/management/modcard/getcardlist',
	filtering : false,
	viewConfig: { forceFit:true },
	remoteSort: true,
	subscribeToEvents : true,
	silent: false, // flag to ignore the events
	eventtype: 'card',
	eventmastertype: 'class',
	initComponent : function() {
		var listAttributes = {};
		var className = '';
		
		this.openFilterBtn = new Ext.Button({
			scope: this,			
			iconCls: 'find',
			text: CMDBuild.Translation.management.findfilter.set_filter,			
			handler: this.openFilter
		});
		
		this.clearFilterBtn = new Ext.Button({
			scope: this,			
			iconCls: 'clear_find',
			text: CMDBuild.Translation.management.findfilter.clear_filter,			
			handler: this.clearFilter
		});
		
		this.gridsearchfield = new Ext.app.GridSearchField({grid: Ext.getCmp(this.id)});

		this.printMenu = new CMDBuild.PrintMenuButton({
			callback: this.print,
			formatList: ['pdf', 'csv'],
			scope: this
		});
		
		this.pagingTools =  [
		   this.openFilterBtn,		    		   
		   this.clearFilterBtn,
		   this.gridsearchfield,
		   this.printMenu
		];
		this.currentClassId = -1;
		
		CMDBuild.Management.CardListGrid.superclass.initComponent.apply(this, arguments);		
		
		function cellclickHandler(grid, rowIndex, colIndex, event) {
			if (event.target.className == 'action-open-graph') {
				var jsonRow = grid.getStore().getAt(rowIndex).json;
				CMDBuild.Management.showGraphWindow(jsonRow.IdClass, jsonRow.Id);
			}
		}
		this.on('cellclick', cellclickHandler);

		this.getSelectionModel().on('rowselect', this.onRowSelect , this);
		
		if (this.subscribeToEvents === true) {
            this.subscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
            this.subscribe('cmdb-reload-' + this.eventtype, this.reloadCard , this);
            this.subscribe('cmdb-new-' + this.eventtype, this.onNewCard , this);
		}
	},

	initForClass : function(eventParams, baseParamsOverride) {
		if (!eventParams) {
			return;
		}
		
		if (this.silent) {
			// clear the selection to reload
			// when back to front
			this.currentClassId = undefined;
			return;
		}
		
		this.selectedRow = undefined;
		if (this.currentClassId != eventParams.classId) {
			this.currentClassId = eventParams.classId;
			this.setColumnsForClass(eventParams.classAttributes, eventParams.superClass);	
			this.setListAttributes(eventParams.classAttributes);
			this.setClassName(eventParams.className);
			this.getStore().baseParams.IdClass = eventParams.classId;
			this.getStore().baseParams.FilterCategory = this.eventmastertype; 
			if(this.filterSubcategory) {
				this.getStore().baseParams.FilterSubcategory = this.filterSubcategory;
			}
			this.clearFilterBtn.disable();
			this.gridsearchfield.setValue("");
		}
		if (baseParamsOverride) {
			Ext.apply(this.getStore().baseParams, baseParamsOverride);
		}		
		this.loadPageForCardId(eventParams.cardId);		
	},

	reloadCard : function(eventParams) {
		if (eventParams && eventParams.classId &&
				((this.currentClassId < 0) || !CMDBuild.Cache.isDescendant(eventParams.classId, this.currentClassId))) {
			return; // skip reloads if specified a class id that is not the current one or a descendant
		}
		if (eventParams && eventParams.cardId) {
			this.loadPageForCardId(eventParams.cardId);
		} else {
			this.reloadCurrentCard();
		}
	},

	reloadCurrentCard : function() {
		this.getStore().reload({
				callback: function() {
					var row = this.selectedRow;
					this.applySelection(row);
				},
				scope: this
			});
	},

	onNewCard: function() {
		this.getSelectionModel().clearSelections();
	},

	setColumnsForClass : function(classAttributes, superClass) {
		var headers = [];
		if (superClass) {
			buildClassColumn.call(this, headers);
		}
		
		for (var i=0; i<classAttributes.length; i++) {
			var attribute = classAttributes[i];
			var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
			if (header) {
				headers.push(header);
			}
		}
		
		buildGraphIconColumn.call(this, headers);
		this.setColumns(headers);
	},

	renderGraphIcon : function() {
		return '<img style="cursor:pointer" title="'
			+ CMDBuild.Translation.management.graph.icon_tooltip
			+'" class="action-open-graph" src="images/icons/chart_organisation.png"/>';
    },

	setListAttributes : function(classAttributes) {
		this.listAttributes = classAttributes;
	},
	
	setClassName :function(name) {
		this.className = name;
	},
	
	getListAttributes : function() {
		return this.listAttributes;
	},

	getClassName : function() {
		return this.className;
	},

	loadPageForCardId : function(cardId) {
		if (cardId) {
			var params = this.defineParamsToLoadPageForCardId(cardId);
			params['FilterCategory'] = this.eventmastertype;
			CMDBuild.Ajax.request({
				url: 'services/json/management/modcard/getcardposition',
		   		params: params,
				scope: this,
				success:function(response, options, resText) {
					this.loadCards(resText.position);
				},
				failure: function(response, options, decoded) {
					this.loadCards(1);
					if (decoded && decoded.reason == 'CARD_NOTFOUND') {
						CMDBuild.Msg.info(undefined, CMDBuild.Translation.info.card_not_found);
					}
					return false;
				}
			});
		} else {
			this.loadCards(1);
		}
	},
	
	defineParamsToLoadPageForCardId: function(cardId) {
		var params = {
				IdClass: this.currentClassId,
	   			Id: cardId
		};
		if (this.store.lastOptions && this.store.lastOptions.params.dir) {
			var sortDirection = this.store.lastOptions.params.dir;
			var sortValue = this.store.lastOptions.params.sort;
			params['dir'] = sortDirection;
			params['sort'] = sortValue;
		} 
		return params;
	},

	loadCards : function(position) {
		var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
		if (position)
			var page = parseInt((position -1) / pageSize);
		else
			var page = 0;
		
		this.getStore().load({
			params: {
				start: page*pageSize,
				limit: pageSize
			},
			dontSelectFirst: true, // TODO see CMDBuild.Grid load handler
			callback: function(records, options, success) {
				if (records.length > 0 && position > 0) {
					var row = (position -1) % pageSize;
					this.selectedRow = row;
					this.applySelection(row);
					this.publish('cmdb-cardsloaded-' + this.eventtype);
				} else {
					CMDBuild.log.info('cmdb-empty-' + this.eventtype);
					this.publish('cmdb-empty-' + this.eventtype);
				}
			},
			scope: this
		});
	},

    onRowSelect: function(sm, rown, rec) {
		this.selectedRow = rown;
		var recToSend = rec.json;
		this.currentCardId = rec.json.Id;
		if (this.currentClassId != rec.json.IdClass) {
			var success = (function(response, options, decoded) {
				// no one thought it was important to save the current
				// ActivityName AKA Code (!!!) in the CMDBuild card
				delete decoded.card.Code;
				Ext.apply(recToSend, decoded.card);
				this.publishLoadEvent(recToSend);
			}).createDelegate(this);
			CMDBuild.ServiceProxy.getCard(rec.json.IdClass, rec.json.Id, success);
		} else {
			this.publishLoadEvent(recToSend);
		}
	},
	
	publishLoadEvent: function(record) {
		this.publish('cmdb-load-' + this.eventtype, {
			record: new Ext.data.Record(record),
			publisher: this
		});
	},
	
	print: function(type) {
		CMDBuild.LoadMask.get().show();
		var columns = this.getVisibleColumns();
		CMDBuild.Ajax.request({
			url: 'services/json/management/modreport/printcurrentview',
			scope: this,
			params: {
				FilterCategory: this.eventmastertype,
				IdClass: this.currentClassId,
				type: type,
				columns: Ext.util.JSON.encode(columns)
			},
			success: function(response) {
				CMDBuild.LoadMask.get().hide();
				var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
				if (!popup) {
					CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
				}
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
	      	}
		});
	},
	
	//private
	getVisibleColumns: function() {
		var columns = this.colModel.config;
		var visibleColumns = [];
		
		for (var i = 0, len = columns.length ; i<len ; i++) {
			var col = columns[i];
			if (typeof col.hidden != "undefined" && !col.hidden) {
				var columnName = col.dataIndex;
				var index = columnName.lastIndexOf("_value");
				if (index >= 0) {
					columnName = columnName.slice(0,index);
				}
				visibleColumns.push(columnName);
			}
		};
		
		CMDBuild.log.debug(visibleColumns);
		return visibleColumns;
	},	
	/*
	 * Filter functions
	 * 
	 *  TODO move to a separate file
	 */
	openFilter: function() {
		var listAttributeForFilter = this.getListAttributes();
		var classNameForFilter = this.getClassName();
		searchWin = new CMDBuild.Management.SearchFilterWindow({
			attributeList:listAttributeForFilter,
			className: classNameForFilter,
			IdClass: this.currentClassId,
			grid: this,
			filterCategory: this.eventmastertype,
			filterSubcategory: this.filterSubcategory
		});
		searchWin.show();
	},

	clearFilter: function(){
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/resetcardfilter',
			params: {
				FilterCategory: this.eventmastertype
			},
			scope: this,
			success: function(response) {
				this.clearFilterBtn.disable();
				this.reload();
				if (this.pagingBar) 
					this.pagingBar.changePage(1); 
			}
		});
	},
	/*
	 * check the CMDBuild state to
	 * now if mine last class is the
	 * global one.
	 * */
	onFront: function() {
		this.silent = false;
		var lastClass = CMDBuild.State.getLastClassSelectedId();
		if (lastClass && this.currentClassId != lastClass) {
			this.initForClass(CMDBuild.State.getLastClassSelected());
		} else {
			var lastCard = CMDBuild.State.getLastCardSelectedId();
			if (lastCard && this.currentCardId != lastCard) {
				this.loadPageForCardId(lastCard);
			}
		}
	},
	
	onBack: function() {
		this.silent = true;
	}
});
Ext.reg('cardlistgrid', CMDBuild.Management.CardListGrid);
CMDBuild.Management.openCard = function(jsonRow, tabToOpen) {	
	this.publish("cmdb-opencard", {
		table: Ext.apply({},CMDBuild.Cache.getTableById(jsonRow.ClassId)),
		cardId: jsonRow.CardId,
		tabToOpen: tabToOpen
	});	
};

})();