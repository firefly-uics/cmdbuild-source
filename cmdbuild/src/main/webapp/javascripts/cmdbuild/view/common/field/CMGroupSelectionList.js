(function() {
	Ext.define("CMGroupModelForList", {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'id', type: 'int'},
			{name: 'description', type: 'string'}
		]
	});

	Ext.define("CMDBuild.view.common.field.CMGroupSelectionList", {
		extend: "Ext.ux.form.MultiSelect",
		fieldLabel : CMDBuild.Translation.administration.modreport.importJRFormStep1.enabled_groups,
		name : "groups",
		dataFields : [ 'id', 'description' ],
		valueField : 'id',
		displayField : 'description',
		allowBlank : true,
		initComponent: function() {
			if (!this.store) {
				this.store  = new Ext.data.Store( {
					model: "CMGroupModelForList",
					proxy : {
						type : "ajax",
						url : 'services/json/management/modreport/getgroups',
						reader : {
							type : "json",
							root : "rows"
						}
					},
					autoLoad : true
				});
			}

			this.callParent(arguments);
		},

		// the origianl multiselect set the field
		// as readonly if disabled. We don't want this
		// behabiour
		updateReadOnly: Ext.emptyFn
	});
})();
