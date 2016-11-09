(function () {

	Ext.define('CMDBuild.proxy.common.field.comboBox.Reference', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.comboBox.reference.StoreRecord',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.core.fieldManager.FieldManager'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 * @param {Object} parameters.extraParams
		 * @param {Object} parameters.scope
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.extraParams = Ext.isObject(parameters.extraParams) ? parameters.extraParams : {};
			parameters.extraParams[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(['Description']);
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this

			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.comboBox.reference.StoreRecord',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.card.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: parameters.extraParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				],
				listeners: {
					scope: parameters.scope,
					beforeload: function (store, operation, eOpts) {
						return (
							Ext.isFunction(this.cmfg)
							&& !this.cmfg('fieldManagerObjectHasTemplates', store.getProxy().extraParams) // Stop call if parameter object has templates
						);
					}
				}
			});
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readCard: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.card.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readClassByName: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.classes.readByName });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters);
		}
	});

})();
