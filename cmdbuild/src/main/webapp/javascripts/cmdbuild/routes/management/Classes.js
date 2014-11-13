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
		clientFilterString: undefined,

		/**
		 * @cfg {Object}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedPrintFormats: ['pdf', 'csv'],

		/**
		 * Apply clientFilter to grid
		 */
		clientFilter: function() {
			if (!Ext.isEmpty(this.clientFilterString))
				Ext.Function.createDelayed(function() {

					// Inject filter in entryType object
					this.entryType.set(
						CMDBuild.core.proxy.CMProxyConstants.FILTER,
						this.clientFilterString
					);

					_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].onViewOnFront(this.entryType);
				}, 1500, this)();
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIden - className
		 * @param {String} params.clientFilter - advanced filter object serialized
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.classIdentifier);

				CMDBuild.Runtime.StartingClassId = this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Use runtime configuration to select class

				this.clientFilter();
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 */
		paramsValidation: function(params) {
			this.classIdentifier = params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER];
			this.clientFilterString = params[CMDBuild.core.proxy.CMProxyConstants.CLIENT_FILTER];
			this.printFormat = params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] || 'pdf';

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
			if (!Ext.isEmpty(this.clientFilterString)) {
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
				_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].gridController.onPrintGridMenuClick(
					this.printFormat
				);
			}, 500, this)();
		}
	});

})();