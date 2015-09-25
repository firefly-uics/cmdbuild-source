(function() {

	Ext.define('CMDBuild.routes.management.Instances', {
		extend: 'CMDBuild.routes.Base',

		/**
		 * @cfg {String}
		 */
		instanceIdentifier: undefined,

		/**
		 * @cfg {String}
		 */
		processIdentifier: undefined,

		/**
		 * @cfg {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.processIdentifier - process name
		 * @param {Number} params.instanceIdentifier - instance id
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.processIdentifier);

				if (!isNaN(parseInt(this.instanceIdentifier))) { // Single card selection
					Ext.Function.createDelayed(function() {
						_CMMainViewportController.openCard({
							Id: this.instanceIdentifier,
							IdClass: this.entryType.get(CMDBuild.core.constants.Proxy.ID),
							activateFirstTab: true
						});
					}, 500, this)();
				} else {
					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.routesInvalidInstanceIdentifier + ' (' + this.instanceIdentifier + ')',
						false
					);
				}
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 */
		paramsValidation: function(params) {
			this.instanceIdentifier = params[CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER];
			this.processIdentifier = params[CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER];
			this.clientFilterString = params[CMDBuild.core.constants.Proxy.CLIENT_FILTER];

			// Process identifier validation
			if (
				Ext.isEmpty(this.processIdentifier)
				|| !_CMCache.isEntryTypeByName(this.processIdentifier)
			) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidProcessIdentifier + ' (' + this.processIdentifier + ')',
					false
				);

				return false;
			}

			// Instance identifier validation
			if (Ext.isEmpty(this.instanceIdentifier)) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidInstanceIdentifier + ' (' + this.instanceIdentifier + ')',
					false
				);

				return false;
			}

			return true;
		}
	});

})();