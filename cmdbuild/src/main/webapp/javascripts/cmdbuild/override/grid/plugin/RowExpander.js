(function() {

	Ext.define('CMDBuild.override.grid.plugin.RowExpander', {
		override: 'Ext.grid.plugin.RowExpander',

		collapseAll: function() {
			Ext.Object.each(this.recordsExpanded, function(key, value, myself) {
				if (value) {
					var i = this.grid.getStore().findBy(function(record, id) {
						return record.internalId == key;
					}, this);

					if (i >= 0)
						this.toggleRow(i, this.grid.getStore().getAt(i));
				}
			}, this);
		}
	});

})();
