(function() {

	Ext.define('CMDBuild.model.configuration.userInterface.UserInterface', {
		extend: 'Ext.data.Model',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLOUD_ADMIN, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FULL_SCREEN_MODE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.HIDE_SIDE_PANEL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_WIDGET_ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_CARD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_PROCESS, type: 'boolean' }
		],

		/**
		 * @param {Object} data
		 *
		 * TODO: waiting for server refactor (properties rename)
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// DisabledCardTabs to model conversion
			this.toModel(CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS, 'CMDBuild.model.configuration.userInterface.DisabledCardTabs');

			// DisabledModules to model conversion
			this.toModel(CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES, 'CMDBuild.model.configuration.userInterface.DisabledModules');

			// DisabledProcessTabs to model conversion
			this.toModel(CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS, 'CMDBuild.model.configuration.userInterface.DisabledProcessTabs');
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledCardTab: function(name) {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_CARD_TABS).get(name);
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledModule: function(name) {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_MODULES).get(name);
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledProcessTab: function(name) {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.DISABLED_PROCESS_TABS).get(name);
		},

		/**
		 * @param {String} propertyName
		 * @param {String} modelName
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		toModel: function(propertyName, modelName) {
			if (Ext.isArray(this.get(propertyName))) {
				var modelObject = {};

				Ext.Array.forEach(this.get(propertyName), function(property, i, allProperties) {
					modelObject[property] = true;
				}, this);

				return this.set(propertyName, Ext.create(modelName, modelObject));
			}

			return this.set(propertyName, Ext.create(modelName, this.get(propertyName)));
		}
	});

})();