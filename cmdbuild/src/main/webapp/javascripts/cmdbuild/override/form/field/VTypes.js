(function() {

	/**
	 * Custom VTypes:
	 * 	- alphanumextended: to validate user names (alphanum and _ - .)
	 * 	- comment: all except pipe (all excluded |)
	 * 	- commentextended: all except pipe and apostrophe (all excluded | ')
	 * 	- multimail: to validate a field with multiple email addresses separated by commas (,)
	 * 	- password: to validate password fields with confirmation capabilities
	 */
	Ext.apply(Ext.form.field.VTypes, {
		// Alpha-numeric extended (alphanumextended)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			alphanumextended: function(value) {
				return this.alphanumextendedMask.test(value);
			},

			/**
			 * @type {String}
			 */
			alphanumextendedText: 'This field should only contain letters, numbers, underscore (_), hyphen (-). dot (.), hash (#) and at (@)',

			/**
			 * @type {RegExp}
			 */
			alphanumextendedMask: /^[a-zA-Z0-9_.+#@-]+$/i,

		// Comment (comment)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			comment: function(value){
				return this.commentMask.test(value);
			},

			/**
			 * @type {String}
			 */
			commentText: CMDBuild.Translation.vtypeCommentText,

			/**
			 * @type {RegExp}
			 */
			commentMask: /^[^|]*$/i,

		// Comment extended (commentextended)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			commentextended: function(value){
				return this.commentextendedMask.test(value);
			},

			/**
			 * @type {String}
			 */
			commentextendedText: CMDBuild.Translation.vtypeCommentExtendedText,

			/**
			 * @type {RegExp}
			 */
			commentextendedMask: /^[^'|]*$/i,

		// Multiple email addresses (multimail)
			/**
			 * The function used to validated multiple email addresses on a single line
			 *
			 * @param {String} value - The email addresses separated by a comma or semicolon
			 *
			 * @returns {Boolean}
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
			multiemailMask: /[\w.\-@'"!#$%&'*+/=?^_`{|}~,]/i,

		// Password (password)
			/**
			 * @param {String} value
			 * @param {String} field
			 *
			 * @returns {Boolean}
			 */
			password: function(val, field) {
				if (
					!Ext.isEmpty(field.twinFieldId) && Ext.isString(field.twinFieldId)
					&& !Ext.isEmpty(Ext.getCmp(field.twinFieldId)) && Ext.isFunction(Ext.getCmp(field.twinFieldId).getValue)
				) {
					return val == Ext.getCmp(field.twinFieldId).getValue();
				}

				return true;
			},

			/**
			 * The error text to display when the password validation function returns false
			 *
			 * @type {String}
			 */
			passwordText: CMDBuild.Translation.passwordsDoNotMatch
	});

})();