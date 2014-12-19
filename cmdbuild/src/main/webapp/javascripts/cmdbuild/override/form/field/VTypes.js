(function() {

	/**
	 * To add multimail as a VType to validate a field with multiple email addresses separated by commas (,)
	 */
	Ext.apply(Ext.form.field.VTypes, {
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
		 * @type String
		 */
		multiemailText: 'This field should be an e-mail address, or a list of email addresses separated by commas (,) in the format "user@domain.com,test@test.com"',

		/**
		 * The keystroke filter mask to be applied on multi email input
		 *
		 * @type RegExp
		 */
		multiemailMask: /[\w.\-@'"!#$%&'*+/=?^_`{|}~,]/i
	});

})();
