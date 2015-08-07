(function() {

	Ext.define('CMDBuild.routes.management.Classes', {
		extend: 'CMDBuild.routes.Base',

		/**
		 * @cfg {String}
		 */
		classIdentifier: undefined,

		/**
		 * @cfg {String}
		 */
		clientFilter: undefined,

		/**
		 * @cfg {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedPrintFormats: [
			CMDBuild.core.proxy.CMProxyConstants.PDF,
			CMDBuild.core.proxy.CMProxyConstants.CSV
		],

		/**
		 * Apply clientFilter to grid
		 */
		applyClientFilter: function() {
			if (!Ext.isEmpty(this.clientFilter))
				Ext.Function.createDelayed(function() {
					this.entryType.set(CMDBuild.core.proxy.CMProxyConstants.FILTER, this.clientFilter); // Inject filter in entryType object

					_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].onViewOnFront(this.entryType);
				}, 1500, this)();
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIden - class name
		 * @param {String} params.clientFilter - advanced filter object serialized
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.classIdentifier);

				CMDBuild.Runtime.StartingClassId = this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Use runtime configuration to select class

				this.applyClientFilter();
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 */
		paramsValidation: function(params) {
			this.classIdentifier = params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER];
			this.clientFilter = params[CMDBuild.core.proxy.CMProxyConstants.CLIENT_FILTER];
			this.printFormat = params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] || CMDBuild.core.proxy.CMProxyConstants.PDF;

			// Class identifier validation
			if (
				Ext.isEmpty(this.classIdentifier)
				|| !_CMCache.isEntryTypeByName(this.classIdentifier)
			) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidClassIdentifier + ' (' + this.classIdentifier + ')',
					false
				);

				return false;
			}

			// Client filter validation
			if (!Ext.isEmpty(this.clientFilter)) {
				// TODO: validate filter with server side call
			}

			// Print format validation
			if (!Ext.Array.contains(this.supportedPrintFormats, this.printFormat)) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidPrintFormat + ' (' + this.printFormat + ')',
					false
				);

				return false;
			}

			return true;
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 */
		print: function(params, path, router) {
			this.detail(params, path, router);

			Ext.Function.createDelayed(function() {
				_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].gridController.onPrintGridMenuClick(this.printFormat);
			}, 500, this)();
		}
	});

})();