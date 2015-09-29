(function() {

	Ext.define('CMDBuild.controller.management.dataView.DataView', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'dataViewSelectedGet',
			'dataViewSelectedIsEmpty',
			'dataViewSelectedSet',
			'onDataViewViewSelected -> sectionController'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Object}
		 */
		sectionController: undefined,

		/**
		 * @property {CMDBuild.model.dataView.SqlView}
		 *
		 * @private
		 */
		selectedView: undefined,

		/**
		 * @cfg {CMDBuild.view.management.dataView.DataViewView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.isEmpty(node)) {
				this.dataViewSelectedSet({ value: node.getData() });

				this.view.removeAll(true);

				switch(this.dataViewSelectedGet(CMDBuild.core.proxy.CMProxyConstants.ID)) { // FIXME: implement node identifier
					case 'dataViewSql':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.management.dataView.Sql', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(this.dataViewSelectedGet(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				this.cmfg('onDataViewViewSelected');

				this.callParent(arguments);
			}
		},

		// SelectedView property methods
			/**
			 * @param {String} parameterName
			 *
			 * @returns {Boolean}
			 */
			dataViewSelectedIsEmpty: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return Ext.isEmpty(this.selectedView.get(parameterName));

				return Ext.isEmpty(this.selectedView);
			},

			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.group.Group} or Mixed
			 */
			dataViewSelectedGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedView.get(parameterName);

				return this.selectedView;
			},

			/**
			 * @param {Object} parameters
			 * @param {Object} parameters.value
			 * @param {String} parameters.propertyName
			 */
			dataViewSelectedSet: function(parameters) {
				if (!Ext.isEmpty(parameters)) {
					var value = parameters.value;
					var propertyName = parameters.propertyName;

					// Create model if not existing
					if (Ext.isEmpty(this.selectedView))
						this.selectedView = Ext.create('CMDBuild.model.dataView.SqlView');

					// Single property management
					if (!Ext.isEmpty(propertyName) && Ext.isString(propertyName)) {
						return this.selectedView.set(propertyName, value);
					} else if (!Ext.isEmpty(value) && Ext.isObject(value)) { // Full object management
						if (Ext.getClassName(value) == 'CMDBuild.model.dataView.SqlView') {
							this.selectedView = value;
						} else {
							this.selectedView = Ext.create('CMDBuild.model.dataView.SqlView', value);
						}
					}
				}
			}
	});

})();