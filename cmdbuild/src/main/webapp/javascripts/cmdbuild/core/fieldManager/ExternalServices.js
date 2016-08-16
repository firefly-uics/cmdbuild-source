(function () {

	/**
	 * External service functions for field manager
	 */
	Ext.define('CMDBuild.core.fieldManager.ExternalServices', {

		/**
		 * Filters empty components
		 *
		 * @param {Object} target
		 * @param {Mixed or Array} components
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		add: function (target, components) {
			components = Ext.isArray(components) ? components : [components];

			if (
				Ext.isObject(target)
				&& Ext.isFunction(target.add)
			) {
				components = Ext.Array.filter(components, function (item, i, array) {
					return !Ext.isEmpty(item) && !Ext.Object.isEmpty(item);
				}, this);

				if (!Ext.isEmpty(components))
					target.add(components);
			} else {
				_error('add(): target not supported object', this, target);
			}
		},

		/**
		 * Filters empty elements
		 *
		 * @param {Array} target
		 * @param {Mixed or Array} elements
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		push: function (target, elements) {
			elements = Ext.isArray(elements) ? elements : [elements];

			if (Ext.isArray(target)) {
				elements = Ext.Array.filter(elements, function (item, i, array) {
					return !Ext.isEmpty(item) && !Ext.Object.isEmpty(item);
				}, this);

				if (!Ext.isEmpty(elements))
					target = Ext.Array.push(target, elements);
			} else {
				_error('push(): target in not array', this, target);
			}
		}
	});

})();
