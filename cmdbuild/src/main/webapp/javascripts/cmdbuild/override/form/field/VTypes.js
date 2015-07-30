(function() {

	/**
	 * Custom VTypes:
	 * 		- multimail: to validate a field with multiple email addresses separated by commas (,)
	 * 		- alphanumextended: to validate user names /[a-z0-9_-.]/i
	 */
	Ext.apply(Ext.form.field.VTypes, {
		/**
		 * @param {String} value
		 *
		 * @return {Boolean}
		 */
		alphanumextended: function(value) {
			var username = /^[a-zA-Z0-9_.+#@-]+$/;

			return username.test(value);
		},

		/**
		 * @type {String}
		 */
		alphanumextendedText: 'This field should only contain letters, numbers, underscore (_), hyphen (-). dot (.), hash (#) and at (@)',

		/**
		 * @type {RegExp}
		 */
		alphanumextendedMask: /[a-z0-9_.+#@-]/i,

		/**
		 * The function used to validated multiple email addresses on a single line
		 *
		 * @param {String} value - The email addresses separated by a comma or semicolon
		 *
		 * @return {Boolean}
		 */
		multiemail: function(value) {
			var array = value.split(',');
			var valid = true;

			Ext.Array.each(array, function(value) {
				if (!this.email(value)) {
					valid = false;

					return false;
				}
			}, this);

			return valid;
		},

		/**
		 * The error text to display when the multi email validation function returns false
		 *
		 * @type {String}
		 */
		multiemailText: 'This field should be an e-mail address, or a list of email addresses separated by commas (,) in the format "user@domain.com,test@test.com"',

		/**
		 * The keystroke filter mask to be applied on multi email input
		 *
		 * @type {RegExp}
		 */
		multiemailMask: /[\w.\-@'"!#$%&'*+/=?^_`{|}~,]/i
	});

})();
