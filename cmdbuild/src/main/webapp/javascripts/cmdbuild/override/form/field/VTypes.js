(function() {

	/**
	 * Custom VTypes:
	 * 	- alphanumextended: to validate user names /[a-z0-9_-.]/i
	 * 	- ipv4: ipv4 validation (CIDR support)
	 * 	- ipv6: ipv6 validation (CIDR support)
	 * 	- multimail: to validate a field with multiple email addresses separated by commas (,)
	 */
	Ext.apply(Ext.form.field.VTypes, {
		/**
		 * @param {String} value
		 *
		 * @return {Boolean}
		 */
		alphanumextended: function(value) {
			return this.alphanumextendedRegExp.test(value);
		},

		alphanumextendedRegExp: /^[a-zA-Z0-9_.+#@-]+$/,

		/**
		 * @type {RegExp}
		 */
		alphanumextendedMask: /[a-z0-9_.+#@-]/i,

		/**
		 * @type {String}
		 */
		alphanumextendedText: 'This field should only contain letters, numbers, underscore (_), hyphen (-). dot (.), hash (#) and at (@)',

		// IPv4 (ipv4)
			/**
			 * @param {String} value
			 *
			 * @return {Boolean}
			 */
			ipv4: function(value) {
				return this.ipv4RegExp.test(value);
			},

			/**
			 * @type {RegExp}
			 */
			ipv4RegExp: /^([0-9]{1,3}\.){3}[0-9]{1,3}(\/([0-9]|[1-2][0-9]|3[0-2]))?$/,

			/**
			 * @type {RegExp}
			 */
			ipv4Mask: /[0-9.\/]/i,

			/**
			 * @type {String}
			 */
			ipv4Text: CMDBuild.Translation.vtype_text.wrong_ip_address,

		// IPv6 (ipv6)
			/**
			 * @param {String} value
			 *
			 * @return {Boolean}
			 */
			ipv6: function(value) {
				return this.ipv6RegExp.test(value);
			},

			/**
			 * @type {RegExp}
			 */
			ipv6Mask: /[0-9a-fA-F:\/]/i,

			/**
			 * @type {RegExp}
			 */
			ipv6RegExp: /^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$/,

			/**
			 * @type {String}
			 */
			ipv6Text: CMDBuild.Translation.vtype_text.wrong_ip_address,

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