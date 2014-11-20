(function() {

	Ext.define('CMDBuild.routes.management.Cards', {
		extend: 'CMDBuild.routes.Base',

		/**
		 * @cfg {String}
		 */
		cardIdentifier: undefined,

		/**
		 * @cfg {Char}
		 */
		cardIdentifierSplitter: '~',

		/**
		 * @cfg {String}
		 */
		classIdentifier: undefined,

		/**
		 * @cfg {Object}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedPrintFormats: ['pdf', 'odt'],

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIdentifier - className
		 * @param {Int} params.cardIdentifier - cardId
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.classIdentifier);

				var splittedIdentifier = this.cardIdentifier.split(this.cardIdentifierSplitter);

				if (!isNaN(parseInt(this.cardIdentifier))) { // Single card selection
					Ext.Function.createDelayed(function() {
						_CMMainViewportController.openCard({
							Id: this.cardIdentifier,
							IdClass: this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID),
							activateFirstTab: true
						});
					}, 500, this)();
				} else if (
					this.cardIdentifier.indexOf(this.cardIdentifierSplitter) >= 0
					&& (splittedIdentifier.length == 2)
					&& (splittedIdentifier[0].length > 0)
					&& (splittedIdentifier[1].length > 0)
				) { // SimpleFilter
					this.simpleFilter(splittedIdentifier);
				} else {
					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.routesCardsDetailInvanlidIdentifier + ' (' + this.cardIdentifier + ')',
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
			this.cardIdentifier = params[CMDBuild.core.proxy.CMProxyConstants.CARD_IDENTIFIER];
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

			// Card identifier validation
			if (Ext.isEmpty(params[CMDBuild.core.proxy.CMProxyConstants.CARD_IDENTIFIER])) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidCardIdentifier + ' (' + this.cardIdentifier + ')',
					false
				);

				return false;
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
		 *
		 * @override
		 */
		print: function(params, path, router) {
			this.detail(params, path, router);

			Ext.Function.createDelayed(function() {
				_CMMainViewportController.panelControllers[CMDBuild.core.proxy.CMProxyConstants.CLASS].cardPanelController.onPrintCardMenuClick(
					this.printFormat
				);
			}, 1500, this)();
		},

		/**
		 * @params {Array} splittedIdentifier - ['cardParam', 'value']
		 */
		simpleFilter: function(splittedIdentifier) {
			CMDBuild.ServiceProxy.getCardList({
				scope: this,
				params: {
					className: this.classIdentifier,
					filter: '{"attribute":{"simple":{"attribute":"' + splittedIdentifier[0] + '","operator":"equal","value":["' + splittedIdentifier[1] + '"]}}}'
				},
				success: function(result, options, decodedResult) {
					if (decodedResult.results == 1) {
						Ext.Router.parse('exec/classes/' + this.classIdentifier + '/cards/' + decodedResult.rows[0]['Id']);
					} else {
						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.routesInvalidSimpleFilter,
							false
						);
					}
				}
			});
		}
	});

})();