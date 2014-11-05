(function() {

	Ext.define('CMDBuild.routes.management.RoutesManagerCards', {
		extend: 'CMDBuild.routes.RoutesManagerBase',

		/**
		 * @cfg {Object}
		 */
		entryType: undefined,

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIdentifier - className
		 * @param {Int} params.cardIdentifier - cardId
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (
				!Ext.isEmpty(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER])
				&& !Ext.isEmpty(params[CMDBuild.core.proxy.CMProxyConstants.CARD_IDENTIFIER])
				&& _CMCache.isEntryTypeByName(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER])
			) {
				this.entryType = _CMCache.getEntryTypeByName(params[CMDBuild.core.proxy.CMProxyConstants.CLASS_IDENTIFIER]);

				Ext.Function.createDelayed(function() {
					_CMMainViewportController.openCard({
						Id: params[CMDBuild.core.proxy.CMProxyConstants.CARD_IDENTIFIER],
						IdClass: this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID),
						activateFirstTab: true
					});
				}, 500, this)();
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesCardsDetailInvanlidIdentifier + ' (' + params[CMDBuild.core.proxy.CMProxyConstants.CARD_IDENTIFIER] + ')',
					false
				);
			}
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @override
		 */
		print: function(params, path, router) {
			params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] = params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] || 'pdf';

			var supportedFormats = ['pdf', 'odt'];

			if (Ext.Array.contains(supportedFormats, params[CMDBuild.core.proxy.CMProxyConstants.FORMAT])) {
				this.detail(params, path, router);

				Ext.Function.createDelayed(function() {
					_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].cardPanelController.onPrintCardMenuClick(
						params[CMDBuild.core.proxy.CMProxyConstants.FORMAT]
					);
				}, 1000, this)();
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