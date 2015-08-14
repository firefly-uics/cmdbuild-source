(function () {

	/**
	 * New implementation of field manager, builds fields starting from CMDBuild.model.common.attributes.Attribute model.
	 */
	Ext.define('CMDBuild.core.fieldManager.FieldManager', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.attributes.Attribute}
		 *
		 * @private
		 */
		attributeModel: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'attributeModelGet'
		],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		managedAttributesTypes: ['BOOLEAN', 'CHAR', 'DATE', 'DECIMAL', 'DOUBLE', 'INTEGER', 'TEXT'],

		// AttributeModel methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			attributeModelGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.attributeModel;

				if (!Ext.isEmpty(attributePath))
					Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName)) {
							if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
								&& Ext.isFunction(requiredAttribute.get)
							) { // Model management
								requiredAttribute = requiredAttribute.get(attributeName);
							} else if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
							) { // Simple object management
								requiredAttribute = requiredAttribute[attributeName];
							}
						}
					}, this);

				return requiredAttribute;
			},

			/**
			 * @param {CMDBuild.model.common.attributes.Attribute} attributeModel
			 */
			attributeModelSet: function(attributeModel) {
				if (
					!Ext.isEmpty(attributeModel)
					&& Ext.getClassName(attributeModel) == 'CMDBuild.model.common.attributes.Attribute'
				) {
					this.attributeModel = attributeModel;
				} else {
					_error('invalid attribute model', this);
				}
			},

		/**
		 * @returns {Mixed}
		 */
		buildAttributeController: function(attributeModel) {
			switch (this.attributeModelGet(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
				case 'BOOLEAN': return Ext.create('CMDBuild.core.fieldManager.builders.Boolean', { parentDelegate: this });
				case 'CHAR': return Ext.create('CMDBuild.core.fieldManager.builders.Char', { parentDelegate: this });
				case 'DATE': return Ext.create('CMDBuild.core.fieldManager.builders.Date', { parentDelegate: this });
				case 'DECIMAL': return Ext.create('CMDBuild.core.fieldManager.builders.Decimal', { parentDelegate: this });
				case 'DOUBLE': return Ext.create('CMDBuild.core.fieldManager.builders.Double', { parentDelegate: this });
				case 'INTEGER': return Ext.create('CMDBuild.core.fieldManager.builders.Integer', { parentDelegate: this });
				case 'TEXT': return Ext.create('CMDBuild.core.fieldManager.builders.text.Text', { parentDelegate: this });
			}
		},

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Mixed}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.buildAttributeController().buildColumn(withEditor);
		},

		/**
		 * @param {CMDBuild.model.common.attributes.Attribute} attributeModel
		 *
		 * @returns {Mixed}
		 */
		buildField: function() {
			return this.buildAttributeController().buildField();
		},

		/**
		 * @param {String} attributeType
		 *
		 * @returns {Boolean}
		 */
		isAttributeManaged: function(attributeType) {
			return Ext.Array.contains(this.managedAttributesTypes, attributeType);
		}
	});

})();