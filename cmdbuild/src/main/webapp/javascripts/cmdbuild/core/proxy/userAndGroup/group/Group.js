(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.Group', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.group.StartingClass'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.create
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		enableDisable: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.enableDisableGroup
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStartingClassStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.group.StartingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != 'Class';
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
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
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.getUiConfiguration,
				loadMask: false
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.read
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.readAll
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.update
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();