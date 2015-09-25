(function() {

	Ext.define('CMDBuild.core.proxy.group.Group', {

		requires: [
			'CMDBuild.core.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.group.StartingClass',
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.create
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		enableDisable: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.enableDisableGroup,
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		getStartingClassStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.group.StartingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: 'classes'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getTypeStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[ CMDBuild.core.constants.Proxy.NORMAL, CMDBuild.Translation.normal ],
					[ CMDBuild.core.constants.Proxy.RESTRICTED_ADMIN, CMDBuild.Translation.limitedAdministrator ],
					[ CMDBuild.core.constants.Proxy.ADMIN, CMDBuild.Translation.administrator ]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getUIConfiguration: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.getUiConfiguration,
				loadMask : false
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.read
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.readAll,
				loadMask: false
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.update,
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();