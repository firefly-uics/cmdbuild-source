(function() {

	Ext.define('CMDBuild.model.group.userInterface.UserInterface', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLOUD_ADMIN, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FULL_SCREEN_MODE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.HIDE_SIDE_PANEL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_WIDGET_ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_CARD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_PROCESS, type: 'boolean' }
		],

		/**
		 * @param {Object} data
		 *
		 * TODO: waiting for server refactor
		 */
		constructor: function(data) {
			var modelObject = {};

			this.callParent(arguments);

			// DisabledCardTabs to model conversion
			if (Ext.isArray(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS])) {
				Ext.Array.forEach(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS], function(property, i, allProperties) {
					modelObject[property] = true;
				}, this);

				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS, Ext.create('CMDBuild.model.group.userInterface.DisabledCardTabs', modelObject));
			} else {
				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS, Ext.create('CMDBuild.model.group.userInterface.DisabledCardTabs', data));
			}

			// DisabledModules to model conversion
			if (Ext.isArray(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES])) {
				modelObject = {};

				Ext.Array.forEach(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES], function(property, i, allProperties) {
					modelObject[property] = true;
				}, this);

				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, Ext.create('CMDBuild.model.group.userInterface.DisabledModules', modelObject));
			} else {
				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, Ext.create('CMDBuild.model.group.userInterface.DisabledModules', data));
			}

			// DisabledProcessTabs to model conversion
			if (Ext.isArray(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS])) {
				modelObject = {};

				Ext.Array.forEach(data[CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS], function(property, i, allProperties) {
					modelObject[property] = true;
				}, this);

				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS, Ext.create('CMDBuild.model.group.userInterface.DisabledProcessTabs', modelObject));
			} else {
				this.set(CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS, Ext.create('CMDBuild.model.group.userInterface.DisabledProcessTabs', data));
			}
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		getData: function() {
			var arrayBuffer = [];
			var returnedObject = this.callParent(arguments);

			// DisabledCardTabs to array conversion
			Ext.Object.each(this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS).getData(), function(key, value, myself) {
				if (value)
					arrayBuffer.push(key);
			}, this);

			returnedObject[CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS] = arrayBuffer;

			// DisabledModules to array conversion
			arrayBuffer = [];

			Ext.Object.each(this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES).getData(), function(key, value, myself) {
				if (value)
					arrayBuffer.push(key);
			}, this);

			returnedObject[CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES] = arrayBuffer;

			// DisabledProcessTabs to array conversion
			arrayBuffer = [];

			Ext.Object.each(this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS).getData(), function(key, value, myself) {
				if (value)
					arrayBuffer.push(key);
			}, this);

			returnedObject[CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS] = arrayBuffer;

			delete returnedObject[CMDBuild.core.proxy.CMProxyConstants.ID];

			return returnedObject;
		}
	});

})();