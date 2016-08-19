(function() {

	Ext.define("CMDBuild.view.management.classes.map.thematism.ThematicStrategiesManager", {
		requires : [ "CMDBuild.view.management.classes.map.proxy.Functions" ],
		/**
		 * Custom client functions
		 */
		strategies : {
			strLenght : {
				description : "@@ l. description",

				/**
				 * @param {Object}
				 *            parameters
				 * 
				 * @returns {generic value}
				 */
				value : function(parameters, callback, callbackScope) {
					var length = (parameters.card.Description) ? parameters.card.Description.length : 0;
					callback.apply(callbackScope, [ length ]);
				}
			},
			strLenghtX2 : {
				description : "@@ l. description X 2",

				/**
				 * @param {Object}
				 *            parameters
				 * 
				 * @returns {generic value}
				 */
				value : function(parameters, callback, callbackScope) {
					var length = (parameters.card.Description) ? parameters.card.Description.length : 0;
					callback.apply(callbackScope, [ length * 2 ]);
				}
			},
			strLenghtX3 : {
				description : "@@ l. description X 3",

				/**
				 * @param {Object}
				 *            parameters
				 * 
				 * @returns {generic value}
				 */
				value : function(parameters, callback, callbackScope) {
					var length = (parameters.card.Description) ? parameters.card.Description.length : 0;
					callback.apply(callbackScope, [ length * 3 ]);
				}
			}
		},

		/**
		 * Custom client functions
		 * 
		 * @returns {Array}
		 */
		getFieldStrategies : function(callback, callbackScope) {
			callback.apply(callbackScope, [ this.strategies ]);
		},

		/**
		 * Custom Sql functions
		 */
		getFunctionStrategies : function(callback, callbackScope) {
			CMDBuild.view.management.classes.map.proxy.Functions.readAllFunctions({
				scope : this,
				success : function(response, options, decodedResponse) {
					var strategies = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
					this.completeStrategyByStrategy(strategies, 0, function() {
						callback.apply(callbackScope, [ strategies ]);
					});
				}
			});
		},

		/**
		 * @param {Object}
		 *            parameters
		 * 
		 * @returns {generic value}
		 */
		functionValue : function(parameters, callback, callbackScope) {
			var params = {
				parameters : Ext.encode({
					ClassName : parameters.card.className,
					CardId : parameters.card.Id
				})
			};

			CMDBuild.view.management.classes.map.proxy.Functions.readOutput({
				scope : this,
				_id : parameters.strategy._id,
				params : params,
				success : function(response, options, decodedResponse) {
					callback.apply(callbackScope, [ decodedResponse.data[0] ]);
				}
			});
		},
		/**
		 * @param {Object}
		 *            parameters
		 * 
		 * @returns {generic value}
		 */
//		functionAttributes : function(parameters, callback, callbackScope) {
//			var params = {
//				parameters : Ext.encode({
//					ClassName : parameters.card.className,
//					CardId : parameters.card.Id
//				})
//			};
//
//			CMDBuild.view.management.classes.map.proxy.Functions.readAttributes({
//				scope : this,
//				_id : parameters.strategy._id,
//				params : params,
//				success : function(response, options, decodedResponse) {
//					callback.apply(callbackScope, [ decodedResponse.data[0] ]);
//				}
//			});
//		},

		/**
		 * @param {Array}
		 *            strategies
		 * 
		 * @returns {Mixed}
		 */
		completeStrategyByStrategy : function(strategies, index, callback, callbackScope) {
			if (index >= strategies.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var strategy = strategies[index];
			strategy.value = this.functionValue;
			CMDBuild.view.management.classes.map.proxy.Functions.readAttributes({
				scope : this,
				_id : strategy._id,
				success : function(response, options, decodedResponse) {
					var attributes = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
					strategy.attributes = attributes;
					this.completeOneStrategy(strategy, function() {
						this.completeStrategyByStrategy(strategies, index + 1, function() {
							callback.apply(callbackScope, []);
						}, this);
					}, this);
				}
			});
		},

		/**
		 * @param {Object}
		 *            strategy
		 * 
		 * @returns {Void}
		 */
		completeOneStrategy : function(strategy, callback, callbackScope) {
			strategy.value = this.functionValue;
			CMDBuild.view.management.classes.map.proxy.Functions.readParameters({
				scope : this,
				_id : strategy._id,
				success : function(response, options, decodedResponse) {
					var parameters = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
					strategy.parameters = parameters;
					callback.apply(callbackScope, []);
				}
			});
		},
	});

})();
