(function() {

	Ext.define('CMDBuild.selection.CheckboxModel', {
	    extend: 'Ext.selection.CheckboxModel',
	    
		//overrid
		bind: function(store, initial) {
			this.callParent(arguments);
			this.cmInverseSelection = false;
			this.cmSelections = [];

			store.on("load", this.onStoreLoad, this);
		},
	    
		//override
	    onHeaderClick: function(headerCt, header, e) {
			if (header.isCheckerHd) {
				e.stopEvent();
				var isChecked = header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');

				this.cmInverseSelection = !isChecked;
				this.cmSelections = [];
				this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();

				if (isChecked) {
					// We have to supress the event or it will scrollTo the change
					this.deselectAll(true);
				} else {
					// We have to supress the event or it will scrollTo the change
					this.selectAll(true);
				}
			}
		},
	
		// override
	    onRowMouseDown: function(view, record, item, index, e) {
			this.callParent(arguments);

			var pageSelections = this.cmSelections[this.cmCurrentPage];	
	        var selectedRecord = pageSelections.get(record.get("Id"));

    	    if (typeof selectedRecord == "undefined") {
    	    	pageSelections.add(record);
    	    } else {
	    	    pageSelections.remove(selectedRecord);
    	    }

		},
		
		onStoreLoad: function(store, records) {
			this.cmCurrentPage = store.currentPage;

			if (typeof this.cmSelections[this.cmCurrentPage] == "undefined") {
				this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();
			}
			
			if (this.cmInverseSelection) {
				//select all the non cmSected records
				this.selectAll();

				var selections = this.cmSelections[this.cmCurrentPage];
				selections.each(function(s) {
					var r = store.find("Id", s.get("Id"));
					if (r != -1) {
						this.deselect(r);
					}
				}, this);

			} else {

				var selections = this.cmSelections[this.cmCurrentPage];
				selections.each(function(s) {
					var r = store.find("Id", s.get("Id"));
					if (r != -1) {
						this.select(r);
					}
				}, this);

			}
			
			this.toggleUiHeader(this.cmInverseSelection);
		},
		
		getCmSelections: function() {
			var out = [];

			for (var i=0, l=this.cmSelections.length; i<l; ++i) {
				var s = this.cmSelections[i];
				if (s) {
					s.each(function(item) {
						out.push(item);
					});
				}
			}
			
			return out;
		},
		
		//private
		buildMixedCollection: function() {
			return new Ext.util.MixedCollection(false, function(el){
			   return el.get("Id");
			});
		},
		
		//override
		onSelectChange: function() {
			this.callParent(arguments);
			this.toggleUiHeader(this.cmInverseSelection);
		},
		
		cmDeselectAll: function() {
			this.deselectAll(arguments);

			this.cmSelections = [];
			this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();
		}
		

	})



	Ext.define("CMDBuild.view.management.common.CMCardGrid", {
		extend: "Ext.grid.Panel",
		filterCategory: undefined,
		filterSubcategory: undefined,
		
		cmPaginate: true, // to say if build or not a paging bar, default true
		cmAddGraphColumn: true, // to say if build or not a column to open the mystical graph window, default true

		constructor: function(c) {
			Ext.apply(this, c);

			this.store = this.getStoreForFields.call(this, []);

			if (this.cmPaginate) {
				buildPagingBar.call(this);
			}

			this.callParent(arguments);
		},
		
		initComponent: function() {
			this.callParent(arguments);
			this.on('beforeitemclick', cellclickHandler, this);
		},
		
		updateStoreForClassId: function(classId) {
			this.currentClassId = classId;
			_CMCache.getAttributeList(classId, Ext.bind(this.setColumnsForClass, this));
		},
		
		// TODO 3 to 4 pagination
		reload: function(reselect) {
			var cb = Ext.emptyFn;
			
			if (reselect) {
				var s = this.getSelectionModel().getSelection();
				cb = function() {
					if (s && s.length > 0) {
						var r = this.store.findRecord("Id", s[0].get("Id"));
						if (r) {
							this.getSelectionModel().select(r);
						}
					} else {
						this.getSelectionModel().select(0);
					}
				}
			}

			this.store.load({
				scope: this,
				callback: cb
			});
		},
		
		//private, can be overridden
		setColumnsForClass: function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];
			
			if (_CMUtils.isSuperclass(this.currentClassId)) {
				headers.push(buildClassColumn());
			}

			for (var i=0; i<classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
				if (header) {
					if (attribute.name != "Notes") {
						headers.push(header);
					}
					fields.push(header.dataIndex);
				}
			}

			if (this.cmAddGraphColumn) {
				buildGraphIconColumn.call(this, headers);
			}

			var s = this.getStoreForFields.call(this, fields);
			this.reconfigure(s, headers);

			if (this.pagingBar) {
				this.pagingBar.bindStore(s);
			}

			this.store.loadPage(1);
		},
		
		// private, can be overridden
		getStoreForFields: function(fields) {
			fields.push("Id");
			fields.push("IdClass");
			fields.push("IdClass_value");
			var pageSize;
			try {
				pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
			} catch (e) {
				pageSize = 20;
			}
			
			return new Ext.data.Store({
				fields: fields,
				pageSize: pageSize,
				proxy: {
					type: "ajax",
					url: 'services/json/management/modcard/getcardlist',
					reader: {
						root: 'rows',
						type: "json",
						totalProperty: 'results'
					},
					extraParams: {
						IdClass : this.currentClassId || -1,
						FilterCategory: this.filterCategory || ""
					}
				},
				autoLoad: false
			});
		}
	});

	function buildPagingBar() {
		this.gridSearchField = new CMDBuild.field.GridSearchField({grid: this});
		
		this.openFilterButton = new Ext.button.Button({
			scope: this,			
			iconCls: 'find',
			text: CMDBuild.Translation.management.findfilter.set_filter,
			handler: onOpenFilterButtonClick
		});
		
		this.clearFilterButton = new Ext.button.Button({
			scope: this,			
			iconCls: 'clear_find',
			text: CMDBuild.Translation.management.findfilter.clear_filter,
			handler: clearFilter
		});
		
		this.pagingBar = new Ext.toolbar.Paging({
			store: this.store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none,
			items: [this.gridSearchField, this.openFilterButton, this.clearFilterButton]
		});
		
		this.bbar = this.pagingBar;
	}

	function buildClassColumn(headers) {
		return {
			header: CMDBuild.Translation.management.modcard.subclass,
			flex: 1,
			sortable: true,
			dataIndex: 'IdClass_value'
		}
	}

	function buildGraphIconColumn(headers) {
		 var c = _CMCache.getClassById(this.currentClassId);

		 if (c && c.get("tableType") != "simpletable") {
			var graphHeader = {
				header: '&nbsp', 
				width: 30, 
				fixed: true, 
				sortable: false, 
				renderer: renderGraphIcon, 
				align: 'center', 
				dataIndex: 'Id',
				menuDisabled: true,
				hideable: false
			};
			headers.push(graphHeader);
		}
	};

	function renderGraphIcon() {
		return '<img style="cursor:pointer" title="'
			+ CMDBuild.Translation.management.graph.icon_tooltip
			+'" class="action-open-graph" src="images/icons/chart_organisation.png"/>';
    }
    
    function onOpenFilterButtonClick() {
		var searchWin = new CMDBuild.Management.SearchFilterWindow({
			attributeList: this.classAttributes,
			IdClass: this.currentClassId,
			grid: this,

			filterCategory: this.filterCategory,
			filterSubcategory: this.filterSubcategory
		}).show();

	}
	
	function clearFilter() {
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/resetcardfilter',
			params: {
				FilterCategory: this.filterCategory
			},
			scope: this,
			success: function(response) {
				this.clearFilterButton.disable();
				if (this.pagingBar) {
					this.store.loadPage(1);
				} else {
					this.reload();
				}
			}
		});
	}
	
	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		if (event.target.className == 'action-open-graph') {
//			CMDBuild.Management.showGraphWindow(model.get("IdClass"), model.get("Id"));

			alert("@@ The graph");
		}
	}
})();