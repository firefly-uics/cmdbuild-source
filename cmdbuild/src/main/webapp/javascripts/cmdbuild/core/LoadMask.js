(function () {

	/**
	 * Global LoadMask management class
	 *
	 * NOTE: gives priority to Splash visualization, to avoid ugly masks overviews
	 */
	Ext.define('CMDBuild.core.LoadMask', {

		requires: ['CMDBuild.core.interfaces.service.Splash'],

		singleton: true,

		/**
		 * @property {Ext.LoadMask}
		 *
		 * @private
		 */
		instance: undefined,

		/**
		 * @param {String} message
		 *
		 * @returns {Ext.LoadMask}
		 */
		build: function (message) {
			message = Ext.isString(message) ? message : CMDBuild.Translation.pleaseWait;

			if (Ext.isEmpty(CMDBuild.core.LoadMask.instance))
				CMDBuild.core.LoadMask.instance = Ext.create('Ext.LoadMask', {
					msg: message,
					target: Ext.getBody()
				});

			return CMDBuild.core.LoadMask.instance;
		},

		/**
		 * @returns {Void}
		 */
		hide: function () {
			CMDBuild.core.LoadMask.build().hide();
		},

		/**
		 * @param {String} message
		 *
		 * @returns {Void}
		 */
		show: function (message) {
			if (
				Ext.isEmpty(CMDBuild.core.interfaces.service.Splash)
				|| !Ext.isFunction(CMDBuild.core.interfaces.service.Splash.build)
				|| CMDBuild.core.interfaces.service.Splash.build().isHidden()
			) {
				CMDBuild.core.LoadMask.build(message).show();
			}
		}
	});

})();
