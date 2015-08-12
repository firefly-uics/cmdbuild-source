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
		 * @property {Mixed}
		 */
		attributeController: undefined,

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
		managedAttributesTypes: ['CHAR', 'DATE'
//		                         , 'TEXT'
		],

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
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
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

		buildAttributeController: function(attributeModel) {
			switch (this.attributeModelGet(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
				case 'CHAR': {
					this.attributeController = Ext.create('CMDBuild.core.fieldManager.builders.Char', { parentDelegate: this });
				} break;

				case 'DATE': {
					this.attributeController = Ext.create('CMDBuild.core.fieldManager.builders.Date', { parentDelegate: this });
				} break;

				case 'TEXT': {
					this.attributeController = Ext.create('CMDBuild.core.fieldManager.builders.Text', { parentDelegate: this });
				} break;
			}
		},

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Mixed}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			this.buildAttributeController();

			return this.attributeController.buildColumn(withEditor);
		},

		/**
		 * @param {CMDBuild.model.common.attributes.Attribute} attributeModel
		 *
		 * @returns {Mixed}
		 */
		buildField: function() {
			this.buildAttributeController();

			return this.attributeController.buildField();
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