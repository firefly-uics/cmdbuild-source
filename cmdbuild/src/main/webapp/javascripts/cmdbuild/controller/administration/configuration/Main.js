(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Main', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: ['CMDBuild.core.proxy.Configuration'],

		/**
		 * @property {Mixed}
		 */
		view: undefined,

		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view.delegate = this;
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onConfigurationAbortButtonClick':
					return this.onConfigurationAbortButtonClick();

				case 'onConfigurationSaveButtonClick':
					return this.onConfigurationSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onConfigurationAbortButtonClick: function() {
			this.readConfiguration();
		},

		onConfigurationSaveButtonClick: function() {
			CMDBuild.core.proxy.Configuration.save({
				scope: this,
				params: this.view.getForm().getValues(),
				success: function(result, options, decodedResult) {
					this.readConfiguration();

					CMDBuild.Msg.success();
				}
			}, this.view.configFileName);
		},

		onViewOnFront: function() {
			if (this.view.isVisible())
				this.readConfiguration();

			_CMCache.initModifyingTranslations();
		},

		readConfiguration: function() {
			CMDBuild.core.proxy.Configuration.read({
				scope: this,
				success: function(result, options, decodedResult){
					_CMCache.setActiveTranslations(decodedResult.data.enabled_languages);

					this.view.getForm().setValues(decodedResult.data);

					this.view.afterSubmit(decodedResult.data);
				}
			}, this.view.configFileName);
		}
	});

})();