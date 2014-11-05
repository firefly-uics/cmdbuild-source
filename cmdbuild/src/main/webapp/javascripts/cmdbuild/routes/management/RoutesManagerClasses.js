(function() {

	Ext.define('CMDBuild.routes.management.RoutesManagerClasses', {
		extend: 'CMDBuild.routes.RoutesManagerBase',

		/**
		 * @cfg {Object}
		 */
		entryType: undefined,

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIden - className
		 * @param {String} params.clientFilter - advanced filter object serialized
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (
				!Ext.isEmpty(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER])
				&& _CMCache.isEntryTypeByName(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER])
			) {
				this.entryType = _CMCache.getEntryTypeByName(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER]);

				// Use runtime configuration to select class
				CMDBuild.Runtime.StartingClassId = this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID);
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesClassDetailInvanlidIdentifier + ' (' + params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER] + ')',
					false
				);
			}
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 */
		print: function(params, path, router) {
			params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] = params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] || 'pdf';

			var supportedFormats = ['pdf', 'csv'];

			if (Ext.Array.contains(supportedFormats, params[CMDBuild.core.proxy.CMProxyConstants.FORMAT])) {
				this.detail(params, path, router);

				Ext.Function.createDelayed(function() {
					_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].gridController.onPrintGridMenuClick(
						params[CMDBuild.core.proxy.CMProxyConstants.FORMAT]
					);
				}, 500, this)();
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesPrintInvalidFormat + ' (' + params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] + ')',
					false
				);
			}
		}
	});

})();