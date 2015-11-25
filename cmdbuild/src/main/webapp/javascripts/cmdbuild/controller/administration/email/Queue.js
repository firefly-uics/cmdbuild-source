(function() {

	Ext.define('CMDBuild.controller.administration.email.Queue', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.email.Queue'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailQueueAbortButtonClick',
			'onEmailQueueSaveButtonClick',
			'onEmailQueueStartButtonClick',
			'onEmailQueueStopButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.email.QueuePanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.QueuePanel} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.QueuePanel', {
				delegate: this
			});
		},

		/**
		 * Reads if queue is running and setup topToolbar buttons enabled state
		 */
		isQueueRunning: function() {
			CMDBuild.core.proxy.email.Queue.isRunning({
				scope: this,
				loadmask: true,
				success: function(response, options, decodedResponse) {
					this.view.queueStartButton.setDisabled(decodedResponse.response);
					this.view.queueStopButton.setDisabled(!decodedResponse.response);
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
					loadmask: true,
					success: function(response, options, decodedResponse) {
						this.readConfiguration();

						CMDBuild.Msg.success();
					}
				});
			}
		},

		onEmailQueueStartButtonClick: function() {
			CMDBuild.core.proxy.email.Queue.start({
				scope: this,
				loadmask: true,
				success: function(response, options, decodedResponse) {
					this.isQueueRunning();
				}
			});
		},

		onEmailQueueStopButtonClick: function() {
			CMDBuild.core.proxy.email.Queue.stop({
				scope: this,
				loadmask: true,
				success: function(response, options, decodedResponse) {
					this.isQueueRunning();
				}
			});
		},

		onViewOnFront: function() {
			this.readConfiguration();
		},

		/**
		 * Reads full queue configuration and setups form
		 */
		readConfiguration: function() {
			CMDBuild.core.proxy.email.Queue.configurationRead({
				scope: this,
				loadmask: true,
				success: function(response, options, decodedResponse) {
					var configurationModel = Ext.create('CMDBuild.model.email.Queue', decodedResponse.response);

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
		 */
		toManagedUnit: function(value) {
			if (Ext.isNumber(value))
				return value * 1000;

			return 0;
		}
	});

})();