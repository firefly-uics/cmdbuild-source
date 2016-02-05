(function() {

	Ext.define('CMDBuild.model.group.Group', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EMAIL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_CLOUD_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.STARTING_CLASS, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEXT, type: 'string' }, // 'Name' alias (waiting server refactor)
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string', defaultValue: 'normal' } // Evaluated on model creation
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// StartingClass filter (waiting server refactor)
			if (this.get(CMDBuild.core.proxy.CMProxyConstants.STARTING_CLASS) == 0)
				this.set(CMDBuild.core.proxy.CMProxyConstants.STARTING_CLASS, null);

			// Evaluate type attribute
			if (this.get(CMDBuild.core.proxy.CMProxyConstants.IS_ADMINISTRATOR))
				this.set(
					CMDBuild.core.proxy.CMProxyConstants.TYPE,
					this.get(CMDBuild.core.proxy.CMProxyConstants.IS_CLOUD_ADMINISTRATOR) ? CMDBuild.core.proxy.CMProxyConstants.RESTRICTED_ADMIN
						: CMDBuild.core.proxy.CMProxyConstants.ADMIN
				);

			this.commit();
		}
	});

})();