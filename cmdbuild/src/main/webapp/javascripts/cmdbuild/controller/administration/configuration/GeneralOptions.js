(function() {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'cmdbuild',

		/**
		 * @property {CMDBuild.view.administration.configuration.GeneralOptionsPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
				delegate: this
			});

			this.cmOn('onReadConfiguration', {
				configFileName: this.configFileName,
				view: this.getView()
			});
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
				case 'onGeneralOptionsAbortButtonClick':
					return this.onGeneralOptionsAbortButtonClick();

				case 'onGeneralOptionsSaveButtonClick':
					return this.onGeneralOptionsSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.configuration.GeneralOptionsPanel}
		 */
		getView: function() {
			return this.view;
		},

		onGeneralOptionsAbortButtonClick: function() {
			this.cmOn('onReadConfiguration', {
				configFileName: this.configFileName,
				view: this.getView()
			});
		},

		onGeneralOptionsSaveButtonClick: function() {
			this.cmOn('onSaveConfiguration', {
				configFileName: this.configFileName,
				view: this.getView()
			});
		},
	});

})();