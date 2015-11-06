(function() {

	Ext.require(['CMDBuild.core.proxy.CMProxyConstants']);

	Ext.define('CMDBuild.model.widget.manageEmail.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATES, type: 'auto', defaultValue: [] }, // Array of CMDBuild.model.common.tabs.email.Template models
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// Apply templates model
			this.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATES, data[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES]);
		},

		/**
		 * @param {String} fieldName
		 * @param {Object} newValue
		 *
		 * @returns {String}
		 *
		 * @override
		 */
		set: function(fieldName, newValue) {
			if (!Ext.isEmpty(newValue)) {
				switch (fieldName) {
					case CMDBuild.core.proxy.CMProxyConstants.TEMPLATES: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var templatesArray = [];

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject)) {
								attributeObject[CMDBuild.core.proxy.CMProxyConstants.BCC] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES];
								attributeObject[CMDBuild.core.proxy.CMProxyConstants.BODY] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.CONTENT];
								attributeObject[CMDBuild.core.proxy.CMProxyConstants.CC] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES];
								attributeObject[CMDBuild.core.proxy.CMProxyConstants.FROM] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS];
								attributeObject[CMDBuild.core.proxy.CMProxyConstants.TO] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES];

								templatesArray.push(Ext.create('CMDBuild.model.common.tabs.email.Template', attributeObject));
							}
						}, this);

						newValue = templatesArray;
					} break;

					default: {
						if (Ext.isString(newValue))
							newValue = Ext.decode(newValue);
					}
				}
			}

			this.callParent(arguments);
		}
	});

})();