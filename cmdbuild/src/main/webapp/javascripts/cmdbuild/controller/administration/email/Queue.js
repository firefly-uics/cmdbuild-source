(function() {

	Ext.define('CMDBuild.controller.administration.email.Queue', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.email.Queue'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailQueueAbortButtonClick',
			'onEmailQueueSaveButtonClick',
			'onEmailQueueShow',
			'onEmailQueueStartButtonClick',
			'onEmailQueueStopButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.email.QueueView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.QueueView} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.QueueView', { delegate: this });
		},

		/**
		 * Reads if queue is running and setup topToolbar buttons enabled state
		 *
		 * @private
		 */
		isQueueRunning: function() {
			CMDBuild.core.proxy.email.Queue.isRunning({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					this.view.queueStartButton.setDisabled(decodedResponse);
					this.view.queueStopButton.setDisabled(!decodedResponse);
				}
			});
		},

		onEmailQueueAbortButtonClick: function() {
			this.readConfiguration();
		},

		onEmailQueueSaveButtonClick: function() {
			if (this.validate(this.view)) { // Validate before save
				var params = {};
				params[CMDBuild.core.constants.Proxy.TIME] = this.toManagedUnit(this.view.cycleIntervalField.getValue());

				CMDBuild.core.proxy.email.Queue.configurationSave({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.readConfiguration();

						CMDBuild.core.Message.success();
					}
				});
			}
		},

		onEmailQueueShow: function() {
			this.readConfiguration();
		},

		onEmailQueueStartButtonClick: function() {
			CMDBuild.core.proxy.email.Queue.start({
				scope: this,
				success: function(response, options, decodedResponse) {
					this.isQueueRunning();
				}
			});
		},

		onEmailQueueStopButtonClick: function() {
			CMDBuild.core.proxy.email.Queue.stop({
				scope: this,
				success: function(response, options, decodedResponse) {
					this.isQueueRunning();
				}
			});
		},

		/**
		 * Reads full queue configuration and setups form
		 *
		 * @private
		 */
		readConfiguration: function() {
			CMDBuild.core.proxy.email.Queue.configurationRead({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					var configurationModel = Ext.create('CMDBuild.model.email.Queue', decodedResponse);

					this.view.cycleIntervalField.setValue(this.toDisplayedUnit(configurationModel.get(CMDBuild.core.constants.Proxy.TIME)));
				},
				callback: function(options, success, response) {
					this.isQueueRunning();
				}
			});
		},

		/**
		 * Converts milliseconds to displayed unit of measure (seconds)
		 *
		 * @param {Number} value
		 *
		 * @return {Number}
		 *
		 * @private
		 */
		toDisplayedUnit: function(value) {
			if (Ext.isNumber(value))
				return value / 1000;

			return 0;
		},

		/**
		 * Converts seconds to managed unit of measure (milliseconds)
		 *
		 * @param {Number} value
		 *
		 * @return {Number}
		 *
		 * @private
		 */
		toManagedUnit: function(value) {
			if (Ext.isNumber(value))
				return value * 1000;

			return 0;
		}
	});

})();