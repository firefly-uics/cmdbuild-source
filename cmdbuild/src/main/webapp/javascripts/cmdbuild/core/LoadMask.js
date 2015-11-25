(function() {

	/**
	 * Global LoadMask management class
	 */
	Ext.define('CMDBuild.core.LoadMask', {

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
		build: function(message) {
			message = Ext.isString(message) ? message : CMDBuild.Translation.pleaseWait;

			if (Ext.isEmpty(CMDBuild.core.LoadMask.instance))
				CMDBuild.core.LoadMask.instance = Ext.create('Ext.LoadMask', {
					msg: message,
					target: Ext.getBody()
				});

			return CMDBuild.core.LoadMask.instance;
		},

		hide: function() {
			CMDBuild.core.LoadMask.build().hide();
		},

		/**
		 * @param {String} message
		 */
		show: function(message) {
			CMDBuild.core.LoadMask.build(message).show();
		}
	});

})();