(function() {
	/**
	 * The model of the records must implement a getId method
	 * in order to identify the data, not the ext record
	 * Is enough to set the idProperty in the model definition
	 **/
	Ext.define('CMDBuild.selection.CMMultiPageSelectionModel', {
		extend: 'Ext.selection.CheckboxModel',

		// override
		bind: function(store, initial) {
			this.store = store;
			this.checkOnly = true; // important to prevent selection issues
			this.cmReverse = false;
			this.reset();
			this.cmCurrentPage = undefined;

			this.callParent(arguments);

			this.mon(this.store, "beforeload", function() { this._onBeforeStoreLoad.apply(this, arguments); }, this);
			this.mon(this.store, "load", function() { this._onStoreDidLoad.apply(this, arguments); }, this);
			this.mon(this, "select", function() { this._addSelection.apply(this, arguments); }, this);
			this.mon(this, "deselect", function() { this._removeSelection.apply(this, arguments); }, this);
		},

		_addSelection: function(sm, record) {
			var id = getId(record);
			if (this.cmReverse) {
				if (id && this.cmSelections.hasOwnProperty(id)) {
					delete this.cmSelections[id];
				}
			} else {
				if (id && !this.cmSelections.hasOwnProperty(id)) {
					this.cmSelections[id] = record.copy();
				}
			}
		},

		_removeSelection: function(sm, record) {
			var id = getId(record);
			if (!this.cmReverse) {
				if (!this.cmFreezedSelections && typeof id != "undefined") {
					delete this.cmSelections[id];
				}
			} else {
				if (id && !this.cmSelections.hasOwnProperty(id)) {
					this.cmSelections[id] = record.copy();
				}
			}
		},

		reset: function() {
			this.clearSelections();
			this.cmSelections = {};
			this.cmFreezedSelections = undefined;
		},

		_onBeforeStoreLoad: function() {
			this.cmFreezedSelections = Ext.clone(this.cmSelections);
		},

		// override
		hasSelection: function() {
			return this.getSelection().length > 0;
		},

		// override
		getCount: function() {
			return this.getSelection().length;
		},

		/**
		 * return the selection if the checkHeader is not
		 * checked, otherwise return the unchecked rows
		 * */
		// override
		getSelection: function() {
			var out = [];
			for (var k in this.cmSelections) {
				out.push(this.cmSelections[k]);
			}

			return out;
		},

		//override
		getHeaderConfig: function() {
			var header = this.callParent(arguments);
			header.tdCls = "grid-button";
			return header;
		},

		//override
		onHeaderClick: function(headerCt, header, e) {
			if (header.isCheckerHd) {
				e.stopEvent();
				this.cmReverse = !header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');
				this.toggleUiHeader(this.cmReverse);

				this.reset();
				this._redoSelection();
			}
		},

		// private
		_onStoreDidLoad: function(store, records) {
			this.cmCurrentPage = store.currentPage;
			if (this.cmFreezedSelections) {
				this.cmSelections = Ext.clone(this.cmFreezedSelections);
				this.cmFreezedSelections = undefined;
			}

			this._redoSelection();
		},

		_redoSelection: function() {
			var me = this,
				views = me.views;

			callOnRowDeselectForAllThePage(me, views);

			if (this.cmReverse) {
				doReverseSelection(me, views);
			} else {
				doSelection(me, views);
			}
		},

		//override
		onSelectChange: function() {
			// bypass the override of the Ext.selection.CheckboxModel that sync the selections
			// with the header status
			Ext.selection.RowModel.prototype.onSelectChange.apply(this, arguments);
		}

	});

	function getId(record) {
		var id = undefined;
		if (record && typeof record.getId == "function") {
			id = record.getId();
		}

		return id;
	}

	function callOnRowDeselectForAllThePage(me, views) {
		var viewsLn = views.length;
		var index = 0;

		me.store.each(function(recordInThePage) {
			for (var i=0; i < viewsLn; i++) {
				views[i].onRowDeselect(index, suppressEvent=true);
			}
			index++;
		});
	}

	function doSelection(me, views) {
		var recordIndex;
		var viewsLn = views.length;

		for (var currentId in me.cmSelections) {
			recordIndex = me.store.findBy(function(record) {
				if (currentId == record.getId()) {
					me.selected.add(record); // to sync with the real selection
					return true;
				}
			});

			if (recordIndex != -1) {
				for (var i=0; i < viewsLn; i++) {
					views[i].onRowSelect(recordIndex, suppressEvent=true);
				}
			}
		}
	}

	function doReverseSelection(me, views) {
		var index = 0;
		var viewsLn = views.length;

		me.store.each(function(recordInThePage) {
			if (!me.cmSelections[recordInThePage.getId()]) {
				me.selected.add(recordInThePage); // to sync with the real selection
				for (var i=0; i < viewsLn; i++) {
					views[i].onRowSelect(index, suppressEvent=true);
				}
			}
			index++;
		});
	}
})();