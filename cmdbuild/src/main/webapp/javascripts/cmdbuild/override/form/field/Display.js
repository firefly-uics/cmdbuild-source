(function() {

	Ext.define('CMDBuild.override.form.field.Display', {
		override: 'Ext.form.field.Display',

		requires: ['CMDBuild.core.Utils'],

		/**
		 * Avoids Display fields to strip \n on contents
		 *
		 * @param {Mixed} rawValue
		 * @param {Mixed} fieldObject
		 *
		 * @return {String}
		 */
		renderer: function(rawValue, fieldObject) {
_debug('rawValue', rawValue);
_debug('fieldObject', fieldObject);
			if (!CMDBuild.core.Utils.hasHtmlTags(rawValue))
				return rawValue.replace(/(\r\n|\n|\r)/gm, '<br />');

			return rawValue;
		},
	});

	/**
	 * Old things
	 */
	Ext.form.field.Display.override({
		setValue : function(value) {
			// for the attributes like lookup and reference
			// that has as value an object like {id:"", description:""}
			if (value != null
					&& typeof value == "object") {

				value = value.description;
			}

			this.callOverridden([value]);
			this._addTargetToLinks();
		},

		_addTargetToLinks: function() {
			var ct = this.getContentTarget();
			if (ct) {
				var links = Ext.DomQuery.select("a", ct.dom);
				if (links) {
					for (var i=0, l=links.length; i<l; ++i) {
						links[i].target = "_blank";
					}
				}
			}
		}
	});

})();