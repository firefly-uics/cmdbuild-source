(function() {

	var activityRowClass = "cm_activity_row";
	var activityRowClass_selected = "cm_activity_row_selected";
	var activityRowClass_over = "cm_activity_row_over";
	var activityRowLabelClass = "cm_activity_row_label";
	var activityRowNotEditable = "cm_activity_row_not_editable";
	var singleActivityRowClass = "cm_single_activity";

	Ext.define("CMDBuild.view.management.common.CMMultipleActivityRowExpander", {
		extend: "Ext.ux.RowExpander",
		alias: 'plugin.activityrowexpander',
		expandOnDblClick: false,
		rowBodyTpl: "ROW EXPANDER REQUIRES THIS TO BE DEFINED",

		// override
		getRowBodyFeatureData: function(data, idx, record, orig) {
			var o = Ext.ux.RowExpander.prototype.getRowBodyFeatureData.apply(this, arguments);
			var activities = record.getActivityInfoList();

			o.rowBody = (function(activities) {
				var out = "";
				// if have a single activity we don't need
				// to have the subrows
				if (activities.length <= 1) {
					out = '<p class="' + singleActivityRowClass + '"></p>';
				} else {
					for (var i=0, l=activities.length; i<l; ++i) {
						var a = activities[i];
						var pClass = activityRowClass;

						if (!a.writable) {
							pClass += (" " + activityRowNotEditable);
						}

						out += Ext.String.format('<p id={0} class="{1}"> <span class="{2}">{3}: </span>{4}</p>', a.id, pClass, activityRowLabelClass, a.performerName, a.description);
					}
				}
				return out;
			})(activities);

			return o;
		},

		// override
		init: function(grid) {
			this.callParent(arguments);

			grid.mon(grid, "select", function() {
				selectSubRow(grid, null);
			});

			// CMDBuild
			// patch to allow the listen the grid reconfigure
			grid.mon(grid, 'reconfigure', this.onReconfigure, this);
		},

		onReconfigure : function(grid, store, columns) {
			if (columns) {
				grid.headerCt.insert(0, this.getHeaderConfig());
			}

			selectSubRow(grid, null);

			// to have not the expanded rows after a reconfiguration
			// of the whole grid
			this.recordsExpanded = {};
		},

		// override
		getHeaderConfig: function() {
			var config = this.callParent(arguments);
			var realRenderer = config.renderer || Ext.emptyFn;

			// show the expander only if have more than one activity
			config.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				var out = "";
				if (record.getActivityInfoList().length > 1) {
					out = realRenderer(value, metadata);
				}
				return out;
			};

			config.width = 18;

			return config;
		},

		/*
		 * override
		 * Add the calls to onRowExpanded and onRowCollapsed
		 * to extend the behaviour after row toggle
		 * 
		 * Add the forceExpand parameter
		 */
		toggleRow: function(rowIdx, forceExpand) {
			var rowNode = this.view.getNode(rowIdx);
			var row = Ext.get(rowNode);

			if (!row) {
				return;
			} else {
				var isEmpty = Ext.query("p[class=" + singleActivityRowClass + "]", row.dom).length > 0;
				if (isEmpty) {
					return;
				}
			}

			var nextBd = Ext.get(row).down(this.rowBodyTrSelector);
			var record = this.view.getRecord(rowNode);
			var grid = this.getCmp();

			if (row.hasCls(this.rowCollapsedCls)) {
				// expand
				row.removeCls(this.rowCollapsedCls);
				nextBd.removeCls(this.rowBodyHiddenCls);
				this.recordsExpanded[record.internalId] = record;
				this.view.fireEvent('expandbody', rowNode, record, nextBd.dom);

				this.onRowExpanded(grid, rowNode, record, nextBd); // CMDBuild, see below
			} else if (!forceExpand) {
				// collapse
				row.addCls(this.rowCollapsedCls);
				nextBd.addCls(this.rowBodyHiddenCls);
				this.recordsExpanded[record.internalId] = false;
				this.view.fireEvent('collapsebody', rowNode, record, nextBd.dom);

				this.onRowCollapsed(grid, rowNode, record, nextBd); // CMDBuild, see below
			}
		},

		onRowExpanded: function(grid, rowNode, record, nextBd) {
			grid.view.refreshSize();
			if (nextBd && record) {
				// listen some event on the activity rows
				var activityRows = nextBd.query("p." + activityRowClass) || [];
				for (var i=0, l=activityRows.length, rowDom = null, rowEl=null; i<l; ++i) {
					rowDom = activityRows[i];
					var rowEl = Ext.get(rowDom.id);

					rowEl.addClsOnOver(activityRowClass_over, function test(overElement) {
						// don't add the class if is the selected row
						return !overElement.hasCls(activityRowClass_selected);
					});

					rowEl.addListener("click", function(evt, e, o) {
						if (!isDoubleClick(evt, grid)) {
							selectSubRow(grid, this);
							grid.onActivitySelected(this.id);
						}
					}, rowEl);
				}
			}
		},

		onRowCollapsed: function(grid, rowNode, record, nextBd) {
			if (nextBd && record) {
				var activityRows = nextBd.query("p." + activityRowClass) || [];
				for (var i=0, l=activityRows.length, sr = null; i<l; ++i) {
					sr = activityRows[i];
					var el = Ext.get(sr.id);
					if (el) {
						el.clearListeners();
					}
				}
			}
		},

		selectSubRow: selectSubRow
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

	var TIME_LIMIT = 500;
	function isDoubleClick(evt, me) {
		var out = false;
		var timeStamp = evt.browserEvent.timeStamp;
		if (me.lastSubRowClickTime) {
			var delta = timeStamp - me.lastSubRowClickTime;
			out = delta < TIME_LIMIT;
		}
		me.lastSubRowClickTime = timeStamp;
		return out;
	}
})();