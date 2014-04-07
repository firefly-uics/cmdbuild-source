(function() {
	Ext.define("CMDBuild.view.common.field.CMGroupSelectionList", {
		extend: "Ext.ux.form.MultiSelect",
		fieldLabel : CMDBuild.Translation.administration.modreport.importJRFormStep1.enabled_groups,
		name : "groups",
		dataFields : [ 'name', 'id', 'description' ],
		valueField : 'id',
		displayField : 'description',
		allowBlank : true,
		initComponent: function() {
			if (!this.store) {
				if (_CMCache && 
						typeof _CMCache.getActiveGroupsStore == "function") {

					this.store = _CMCache.getActiveGroupsStore();
				} else {
					this.store = new Ext.data.Store({
						fields: ["fake"],
						data: []
					});
				}
			}

			this.callParent(arguments);
		},

		// the origianl multiselect set the field
		// as readonly if disabled. We don't want this
		// behabiour
		updateReadOnly: Ext.emptyFn,

		reset: function() {
			this.setValue([]);
		}
	});
})();
