Ext.override(Ext.selection.CheckboxModel, {
	bindComponent: function() {

		this.callOverridden(arguments);
		if (this.view) {
			this.mon(this.view.ownerCt, 'reconfigure', this.onGridReconfigure, this);
		}
	},

	onGridReconfigure: function() {
		_debug("********* reconfigure");
		var view = this.views[0],
			headerCt = view.headerCt;

		if (this.injectCheckbox !== false) {
			if (this.injectCheckbox == 'first') {
				this.injectCheckbox = 0;
			} else if (this.injectCheckbox == 'last') {
				this.injectCheckbox = headerCt.getColumnCount();
			}
			headerCt.add(this.injectCheckbox,  this.getHeaderConfig());
		}

		headerCt.on('headerclick', this.onHeaderClick, this);
	}
});