CMDBuild.Management.LinkCards = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {

	singleSelect : false,
	isFirstLoad : true, // flag to identify the first loading to clear the grid filter
	currentSelection : [],

	/**
	 * parameters: int classId [boolean singleSelect] String
	 * outputName
	 * 
	 * @param {}
	 *            extAttrDef
	 * @return {}
	 */
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
		var classId = this.getVariable("xa:ClassId");
		var cqlQuery = this.getVariable("xa:Filter");
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

	onSave : function(evtParams, fn) {
		if (undefined == this.outputName) {
			fn(this.identifier, true);
			return;
		}
		var out = {};
		out[this.outputName] = this.getData();
		this.react(out, fn);
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
	}
});
Ext.reg("linkCards", CMDBuild.Management.LinkCards);
