(function () {

	Ext.define('CMDBuild.proxy.gis.Card', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.gis.Card',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.user.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CARD, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function (parameters) {
			var pageSize = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT);
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: false,
				model: 'CMDBuild.model.gis.Card',
				remoteSort: true,
				pageSize: pageSize,
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.card.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: 'results'
					},
					extraParams: this.getStoreExtraParams()
				}
			});
		},

		/**
		 * @returns {Object extraParameters}
		 */
		getStoreExtraParams: function() {
			var currentIdClass = (Ext.isEmpty(_CMCardModuleState.entryType)) ?
					undefined : _CMCardModuleState.entryType.getId();
			var extraParameters = {
				className: ""

			};

			if (currentIdClass) {
				extraParameters.className = _CMCache.getEntryTypeNameById(data.IdClass);
			}

			if (this.CQL) {
				extraParameters = Ext.apply(extraParameters, this.CQL); // RettoCompatibility
				extraParameters.filter = Ext.encode(this.CQL);
			}

			return extraParameters;
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
		}
	});

})();
