(function() {
	
	Ext.define("CMDBuild.controller.administration.lookup.CMLookupGridController", {
		constructor: function(view) {
			this.view = view;
			this.sm = this.view.getSelectionModel();
			this.subController = null;
			
			this.view.addButton.on("click", function() {
				this.sm.deselect(this.sm.getSelection());
				notifySubController.call(this, "onAddLookupClick");
			}, this);
			this.sm.on("selectionchange", this._onSelectionChanged, this);
		},

		bindSubController: function(c) {
			this.subController = c;
		},

		onLookupDisabled: function(lookup) {
			this.view.loadData(lookupIdToSelectAfterLoad = lookup);
		},

		onLookupSaved: function(lookup) {
			this.view.loadData(lookupIdToSelectAfterLoad = lookup);
		},

		_onSelectionChanged: function(sm, selection) {
			if (selection.length > 0) {
				var s = selection[0];
				notifySubController.call(this, "onSelectLookupGrid", s);
			}
		},

		onSelectLookupType: function(lookupType) {
			this.view.onSelectLookupType(lookupType);
		},

		onAddLookupTypeClick: function() {}
	});
	
	function notifySubController(event, params) {
		this.subController[event](params);
	}
})();