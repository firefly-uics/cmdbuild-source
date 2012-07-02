(function() {

	var activityRowClass = "cm_activity_row";
	var activityRowClass_selected = "cm_activity_row_selected";
	var activityRowClass_over = "cm_activity_row_over";
	var activityRowLabelClass = "cm_activity_row_label";
	var activityRowNotEditable = "cm_activity_row_not_editable";
	var ACTIVITY_SELECTION_EVENT = "cm_activity_selected";

	Ext.define("CMDBuild.view.management.common.CMMultipleActivityRowExpander", {
		extend: "Ext.ux.RowExpander",
		alias: 'plugin.activityrowexpander',
		expandOnDblClick: false,
		rowBodyTpl: "ROW EXPANDER REQUIRES THIS TO BE DEFINED",
		getRowBodyFeatureData: function(data, idx, record, orig) {
			var o = Ext.ux.RowExpander.prototype.getRowBodyFeatureData.apply(this, arguments);
			var activities = [{
				group: "Gruppo 1",
				name: "Attività 2.1"
			},{
				group: "Gruppo 2",
				name: "Attività 2.2"
			},{
				group: "Gruppo 3",
				name: "Attività 2.3",
				editable: true
			}]; // record.getActivites();

			o.rowBody = (function(activities){
				var out = "";
				for (var i=0, l=activities.length; i<l; ++i){
					var a = activities[i];
					var pClass = activityRowClass;

					if (!a.editable) {
						pClass += (" " + activityRowNotEditable);
					}
					out += Ext.String.format('<p class="{0}"> <span class="{1}">{2}:</span> {3}</p>', pClass, activityRowLabelClass, a.group, a.name);
				}

				return out;
			})(activities);

			return o;
		},

		// override
		init: function(grid) {
			this.callParent(arguments);
			grid.on("select", function() {
				selectSubRow(grid, null);
			});
		},

		onRowExpanded: function(grid, rowNode, record, nextBd) {
			grid.view.refreshSize();
			if (nextBd 
					&& record
					&& typeof record.subRows == "undefined") {

				record.subRows = [];
				var childRows = nextBd.query("p." + activityRowClass);

				for (var i=0, l=childRows.length; i<l; ++i) {
					var childRow = childRows[i];
					var rowEl = new Ext.Element(childRow);

					record.subRows.push(rowEl);
					rowEl.referredRecord = record;

					rowEl.addClsOnOver(activityRowClass_over, function test(overElement) {
						// don't add the class if is the selected row
						return !overElement.hasCls(activityRowClass_selected);
					});

					rowEl.addListener("click", function(evt, e, o) {
						selectSubRow(grid, this);
						grid.fireEvent(ACTIVITY_SELECTION_EVENT, this);
					}, rowEl);
				}
			}
		}
	});

	function selectSubRow(grid, subrow) {
		if (grid.lastSubRowSelected) {
			grid.lastSubRowSelected.removeCls(activityRowClass_selected);
		}

		grid.lastSubRowSelected = subrow;

		if (subrow) {
			subrow.removeCls(activityRowClass_over);
			subrow.addCls(activityRowClass_selected);
		}
	}

	Ext.define("CMDBuild.view.management.common.CMCardGrid", {
		extend: "Ext.grid.Panel",
		columns: [],

		extraParams: undefined, // extra params for the store
		filterCategory: undefined,
		filterSubcategory: undefined,

		forceSelectionOfFirst: false, // listen load event and select the first row
		skipSelectFirst: false,
		shouldSelectFirst: function() {
			var out = this.forceSelectionOfFirst && !this.skipSelectFirst;
			this.skipSelectFirst = false;
			return out;
		},
		skipNextSelectFirst: function() {
			this.skipSelectFirst = true;
		},

		cmStoreUrl: 'services/json/management/modcard/getcardlist',
		cmPaginate: true, // to say if build or not a paging bar, default true
		cmBasicFilter: true, // to add a basic search-field to the paging bar 
		cmAdvancedFilter: true, // to add a button to set an advanced filter
		cmAddGraphColumn: true, // to say if build or not a column to open the mystical graph window, default true
		cmAddPrintButton: true, // to add a button to set an chose the print format

		constructor: function(c) {
			Ext.apply(this, c);
			this.loadMask = false;
			this.store = this.getStoreForFields.call(this, []);

			if (this.cmPaginate) {
				buildPagingBar.call(this);
			}

			this.plugins = [{
				ptype: "activityrowexpander"
			}];

			this.callParent(arguments);
		},

		initComponent: function() {
			this.viewConfig = {
				stripeRows: true
			};

			this.callParent(arguments);
			this.on('beforeitemclick', cellclickHandler, this);
		},

		updateStoreForClassId: function(classId, o) {

			function callCbOrLoadFirstPage() {
				if (o && o.cb) {
					o.cb.call(o.scope || this);
				} else {
					this.store.loadPage(1);
				}
			}

			if (this.currentClassId == classId) {
				callCbOrLoadFirstPage.call(this);
			} else {
				this.currentClassId = classId;
				if (this.printGridMenu) {
					this.printGridMenu.setDisabled(!classId);
				}
				_CMCache.getAttributeList(classId, 
					Ext.bind(function(attributes) {
						this.setColumnsForClass(attributes);
						callCbOrLoadFirstPage.call(this);
					}, this)
				);
			}
		},

		loadPage: function(pageNumber, o) {
			o = o || {};
			scope = o.scope || this;
			cb = o.cb || Ext.emptyFn;

			// store.loadPage does not allow the definition of a callBack
			this.on("load", cb, scope, {single: true});
			this.store.loadPage(Math.floor(pageNumber));
		},

		clearFilter: clearFilter,

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
				};
			}

			this.store.load({
				scope: this,
				callback: cb
			});
		},
		
		getVisibleColumns: function() {
			var columns = this.columns;
			var visibleColumns = [];
			
			for (var i = 0, len = columns.length ; i<len ; i++) {
				var col = columns[i];
				if (!col.hidden && col.dataIndex != "Id") { // The graph column has dataIndex Id
					var columnName = col.dataIndex;
					var index = columnName.lastIndexOf("_value");
					if (index >= 0) {
						columnName = columnName.slice(0,index);
					}
					visibleColumns.push(columnName);
				}
			};

			return visibleColumns;
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
					// There was a day in which I receved the order to skip the Notes attribute.
					// Today, the boss told  me to enable the notes. So, I leave the condition
					// commented to document the that a day the notes were hidden.

					// if (attribute.name != "Notes") {
						headers.push(header);
					// }

					fields.push(header.dataIndex);
				} else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push("Description");
				}
			}

			headers = headers.concat(this.buildExtraColumns());

			if (this.cmAddGraphColumn && CMDBuild.Config.graph.enabled=="true") {
				buildGraphIconColumn.call(this, headers);
			}

			var s = this.getStoreForFields(fields);
			this.reconfigure(s, headers);

			if (this.pagingBar) {
				this.pagingBar.bindStore(s);
			}
		},
		
		// private, could be overridden
		getStoreForFields: function(fields) {
			fields.push({name: "Id", type: "int"});
			fields.push({name: "IdClass", type: "int"});
			fields.push("IdClass_value");
			var pageSize;
			try {
				pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
			} catch (e) {
				pageSize = 20;
			}

			var s = new Ext.data.Store({
				fields: fields,
				pageSize: pageSize,
				remoteSort: true,
				proxy: {
					type: "ajax",
					url: this.cmStoreUrl,
					reader: {
						root: "rows",
						type: "json",
						totalProperty: "results",
						idProperty: "Id"
					},
					extraParams: this.getStoreExtraParams()
				},
				autoLoad: false
			});

			this.mon(s, "beforeload", function() {
				this.fireEvent("beforeload", arguments);
			}, this);

			this.mon(s, "load", function(store, records) {
				this.fireEvent("load", arguments);

				if (this.shouldSelectFirst() && !this.getSelectionModel().hasSelection()
						&& records && records.length > 0) {

					try {
						this.getSelectionModel().select(0);
					} catch (e) {
						this.fireEvent("cmWrongSelection");
						CMDBuild.log.info("Not selected the first record");
						_trace();
					}
				}

			}, this);

			return s;
		},

		//private, could be overridden
		getStoreExtraParams: function() {
			var p = {
				IdClass : this.currentClassId || -1,
				FilterCategory: this.filterCategory || "",
				FilterSubcategory: this.filterSubcategory || ""
			};

			if (this.CQL) {
				Ext.apply(p, this.CQL);
			}

			return p;
		},

		//private, could be overridden
		buildExtraColumns: function() {
			return [];
		},

		getPageNumber: getPageNumber
	});

	function updateStoreAndSelectGivenPosition(idClass, position) {
		var me = this;

		this.updateStoreForClassId(idClass, {
			cb: function cbOfUpdateStoreForClassId() {
				var	pageNumber = getPageNumber(position),
					pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit),
					relativeIndex = position % pageSize;

				me.loadPage(pageNumber, {
					cb: function callBackOfLoadPage(records, operation, success) {
						try {
							me.getSelectionModel().select(relativeIndex);
						} catch (e) {
							me.fireEvent("cmWrongSelection");
							_debug("I was not able to select the record at " + relativeIndex);
							_trace();
						}
					}
				});
			}
		});
	}
	
	function buildPagingBar() {
		var items = [];

		if (this.cmBasicFilter) {
			this.gridSearchField = new CMDBuild.field.GridSearchField({grid: this});
			items.push(this.gridSearchField);
		}

		if (this.cmAdvancedFilter) {
			this.openFilterButton = new Ext.button.Button({
				scope: this,			
				iconCls: 'find',
				text: CMDBuild.Translation.management.findfilter.set_filter,
				handler: onOpenFilterButtonClick,
				disabled: true
			});

			this.clearFilterButton = new Ext.button.Button({
				scope: this,
				iconCls: 'clear_find',
				text: CMDBuild.Translation.management.findfilter.clear_filter,
				handler: function() {
					this.clearFilter();
				},
				disabled: true
			});

			items.push(this.openFilterButton, this.clearFilterButton);
		}

		if (this.cmAddPrintButton) {
			this.printGridMenu = new CMDBuild.PrintMenuButton({
				callback : function() { this.fireEvent("click"); },
				formatList: ["pdf", "csv"],
				disabled: true
			});
			items.push(this.printGridMenu);
		}

		this.pagingBar = new Ext.toolbar.Paging({
			store: this.store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none,
			items: items
		});

		this.bbar = this.pagingBar;
	}

	function buildClassColumn(headers) {
		return {
			header: CMDBuild.Translation.management.modcard.subclass,
			width: 100,
			sortable: true,
			dataIndex: 'IdClass_value'
		};
	}

	function buildGraphIconColumn(headers) {
		 var c = _CMCache.getClassById(this.currentClassId);

		 if (c && c.get("tableType") != "simpletable") {
			var graphHeader = {
				header: '&nbsp', 
				width: 30,
				tdCls: "grid-button",
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
		new CMDBuild.Management.SearchFilterWindow({
			attributeList: this.classAttributes,
			IdClass: this.currentClassId,
			grid: this,

			filterCategory: this.filterCategory,
			filterSubcategory: this.filterSubcategory
		}).show();
	}

	function clearFilter(cb, skipReload) {
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/resetcardfilter',
			params: {
				FilterCategory: this.filterCategory
			},
			scope: this,
			success: function(response) {
				if (this.clearFilterButton) {
					this.clearFilterButton.disable();
				}

				if (!skipReload) {
					if (this.pagingBar) {
						this.store.loadPage(1);
					} else {
						this.reload();
					}
				}

				if (typeof cb == "function") {
					cb();
				}
			}
		});
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		if (event.target.className == 'action-open-graph') {
			CMDBuild.Management.showGraphWindow(model.get("IdClass"), model.get("Id"));
		}
	}

	function getPageNumber(cardPosition) {
		var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit),
			pageNumber = 1;

		if (cardPosition == 0) {
			return pageNumber;
		}

		if (cardPosition) {
			pageNumber = parseInt(cardPosition) / pageSize;
		}

		return pageNumber + 1;
	}
})();