(function() {

	/**
	 * @deprecated (CMDBuild.view.common.field.multiselect.Group)
	 */
	Ext.define('CMDBuild.view.common.field.CMGroupSelectionList', {
		extend: 'Ext.ux.form.MultiSelect',

		requires: ['CMDBuild.core.proxy.common.field.multiselect.Group'],

		considerAsFieldToDisable: true,

		fieldLabel: CMDBuild.Translation.enabledGroups,
		name: CMDBuild.ServiceProxy.parameter.GROUPS,
		dataFields: [
			CMDBuild.ServiceProxy.parameter.NAME,
			CMDBuild.ServiceProxy.parameter.ID,
			CMDBuild.ServiceProxy.parameter.DESCRIPTION
		],
		valueField: CMDBuild.ServiceProxy.parameter.ID,
		displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		allowBlank: true,

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.core.proxy.common.field.multiselect.Group.getStore()
			});

			this.callParent(arguments);
		},

		// The origianl multiselect set the field as readonly if disabled.
		// We don't want this behabiour.
		updateReadOnly: Ext.emptyFn,

		reset: function() {
			this.setValue([]);
		},

		selectAll: function() {
			var arrayGroups = [];

			this.store.data.each(function(item, index, totalItems) {
				arrayGroups.push(item.data.name);
			});

			this.setValue(arrayGroups);
		}
	});

})();