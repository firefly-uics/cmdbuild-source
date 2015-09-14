(function() {

	Ext.define('CMDBuild.model.group.Group', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DISABLED_MODULES, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.EMAIL, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.IS_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.IS_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.IS_CLOUD_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.STARTING_CLASS, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string' }, // 'Name' alias (waiting server refactor)
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'normal' } // Evaluated on model creation
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// StartingClass filter (waiting server refactor)
			if (this.get(CMDBuild.core.proxy.Constants.STARTING_CLASS) == 0)
				this.set(CMDBuild.core.proxy.Constants.STARTING_CLASS, null);

			// Evaluate type attribute
			if (this.get(CMDBuild.core.proxy.Constants.IS_ADMINISTRATOR))
				this.set(
					CMDBuild.core.proxy.Constants.TYPE,
					this.get(CMDBuild.core.proxy.Constants.IS_CLOUD_ADMINISTRATOR) ? CMDBuild.core.proxy.Constants.RESTRICTED_ADMIN
						: CMDBuild.core.proxy.Constants.ADMIN
				);

			this.commit();
		}
	});

})();