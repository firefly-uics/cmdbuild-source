(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'string' }, // Entry type name
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATE, type: 'boolean' }
		],

		/**
		 * Implementation of model get custom routines:
		 * - on get description if description is empty return name property
		 *
		 * @param {String} propertyName
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (propertyName) {
			switch (propertyName) {
				case CMDBuild.core.constants.Proxy.DESCRIPTION:
					return this.callParent(arguments) || this.get(CMDBuild.core.constants.Proxy.NAME) || '';

				default:
					return this.callParent(arguments);
			}
		},

		/**
		 * @returns {Array}
		 */
		getRuntimeParameters: function () {
			var configuration = this.get(CMDBuild.core.constants.Proxy.CONFIGURATION);

			return this.runtimeParameterSearch(configuration[CMDBuild.core.constants.Proxy.ATTRIBUTE] || {}, []);
		},

		/**
		 * Recursive method to find all filter runtime parameters
		 *
		 * @param {Object} configuration
		 * @param {Array} runtimeParameters
		 *
		 * @returns {Array} runtimeParameters
		 *
		 * @private
		 */
		runtimeParameterSearch: function (configuration, runtimeParameters) {
			if (Ext.isObject(configuration.simple)) {
				var conf = configuration.simple;

				if (conf.parameterType == CMDBuild.core.constants.Proxy.RUNTIME)
					runtimeParameters.push(conf);
			} else if (Ext.isArray(configuration.and) || Ext.isArray(configuration.or)) {
				var attributes = configuration.and || configuration.or;

				if (Ext.isArray(attributes) && !Ext.isEmpty(attributes))
					Ext.Array.each(attributes, function (attributeObject, i, allAttributeObjects) {
						this.runtimeParameterSearch(attributeObject, runtimeParameters);
					}, this);
			}

			return runtimeParameters;
		}


//		/**
//		 * Return a full copy of this filter
//		 *
//		 * @returns {CMDBuild.model.CMFilterModel}
//		 *
//		 * @override
//		 */
//		copy: function () {
//			var dolly = new CMDBuild.model.CMFilterModel();
//			dolly.set(CMDBuild.core.constants.Proxy.ID, this.get(CMDBuild.core.constants.Proxy.ID));
//			dolly.setName(this.getName());
//			dolly.setDescription(this.getDescription());
//			dolly.setConfiguration(Ext.apply({}, this.getConfiguration()));
//			dolly.setEntryType(this.getEntryType());
//			dolly.setApplied(this.isApplied());
//			dolly.setLocal(this.isLocal());
//			dolly.setTemplate(this.isTemplate());
//
//			dolly.commit();
//
//			if (this.dirty)
//				dolly.setDirty();
//
//			return dolly;
//		},
//
//		setName: function (name) {
//			this.set(CMDBuild.core.constants.Proxy.NAME, name);
//		},
//
//		setDescription: function (description) {
//			this.set(CMDBuild.core.constants.Proxy.DESCRIPTION, description);
//		},
//
//		getConfiguration: function () {
//			return this.get(CMDBuild.core.constants.Proxy.CONFIGURATION) || {};
//		},
//
//		/**
//		 * @param {Array} runtimeParameterFields
//		 * 	{
//		 * 		{Ext.form.field} fieldObj,
//		 * 		...
//		 * 	}
//		 */
//		getConfigurationMergedWithRuntimeAttributes: function (runtimeParameterFields) {
//			runtimeParameterFields = runtimeParameterFields || [];
//
//			var configuration = Ext.clone(this.getConfiguration());
//
//			configuration.attribute = mergeRuntimeParametersToConf(configuration.attribute, runtimeParameterFields);
//
//			return configuration;
//		},
//
//		setConfiguration: function (configuration) {
//			this.set(CMDBuild.core.constants.Proxy.CONFIGURATION, configuration);
//		},
//
//		getAttributeConfiguration: function () {
//			var c = this.getConfiguration();
//			var attributeConf = c.attribute || {};
//
//			return attributeConf;
//		},
//
//		setAttributeConfiguration: function (conf) {
//			var configuration = this.getConfiguration();
//
//			delete configuration.attribute;
//
//			if (Ext.isObject(conf) && Ext.Object.getKeys(conf).length > 0) {
//				configuration.attribute = conf;
//				this.set(CMDBuild.core.constants.Proxy.CONFIGURATION, configuration);
//			}
//		},
//
//		getCalculatedParameters: function () {
//			var calculatedParameters = [];
//			var attributeConf = this.getAttributeConfiguration();
//
//			return addCalculatedParameterToList(attributeConf, calculatedParameters);
//		},
//
//		getRelationConfiguration: function () {
//			var configuration = this.getConfiguration();
//			var relationConfiguration = configuration.relation || [];
//
//			return relationConfiguration;
//		},
//
//		setRelationConfiguration: function (conf) {
//			var configuration = this.getConfiguration();
//
//			delete configuration.relation;
//
//			if (Ext.isArray(conf) && conf.length > 0) {
//				configuration.relation = conf;
//				this.set(CMDBuild.core.constants.Proxy.CONFIGURATION, configuration);
//			}
//		},
//
//		getFunctionConfiguration: function () {
//			var c = this.getConfiguration();
//			var attributeConf = c.functions || [];
//
//			return attributeConf;
//		},
//
//		setFunctionConfiguration: function (functions) {
//			var configuration = this.getConfiguration();
//
//			if (functions.length > 0) {
//				configuration.functions = functions;
//			} else {
//				delete configuration.functions;
//			}
//
//			this.set(CMDBuild.core.constants.Proxy.CONFIGURATION, configuration);
//		},
//
//		getEntryType: function () {
//			return this.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE) || '';
//		},
//
//		setEntryType: function (entryType) {
//			this.set(CMDBuild.core.constants.Proxy.ENTRY_TYPE, entryType);
//		},
//
//		isTemplate: function () {
//			return this.get(CMDBuild.core.constants.Proxy.TEMPLATE) || false;
//		},
//
//		setTemplate: function (applied) {
//			this.set(CMDBuild.core.constants.Proxy.TEMPLATE, applied);
//		},
//
//		isApplied: function () {
//			return this.get(CMDBuild.core.constants.Proxy.APPLIED) || false;
//		},
//
//		setApplied: function (applied) {
//			this.set(CMDBuild.core.constants.Proxy.APPLIED, applied);
//		},
//
//		isLocal: function () {
//			return this.get(CMDBuild.core.constants.Proxy.LOCAL) || false;
//		},
//
//		setLocal: function (local) {
//			this.set(CMDBuild.core.constants.Proxy.LOCAL, local);
//		}
	});

//	function addRuntimeParameterToList(attributeConf, runtimeParameters) {
//		if (Ext.isObject(attributeConf.simple)) {
//			var conf = attributeConf.simple;
//
//			if (conf.parameterType == "runtime")
//				runtimeParameters.push(conf);
//
//		} else if (Ext.isArray(attributeConf.and) || Ext.isArray(attributeConf.or)) {
//			var attributes = attributeConf.and || attributeConf.or;
//
//
//			for (var i = 0; i < attributes.length; ++i)
//				addRuntimeParameterToList(attributes[i], runtimeParameters);
//		}
//
//		return runtimeParameters;
//	}
//
//	function addCalculatedParameterToList(attributeConf, calculatedParameters) {
//		if (Ext.isObject(attributeConf.simple)) {
//			var conf = attributeConf.simple;
//
//			if (conf.parameterType == "calculated")
//				calculatedParameters.push(conf);
//		} else if (Ext.isArray(attributeConf.and) || Ext.isArray(attributeConf.or)) {
//			var attributes = attributeConf.and || attributeConf.or;
//
//
//			for (var i = 0; i < attributes.length; ++i)
//				addCalculatedParameterToList(attributes[i], calculatedParameters);
//		}
//
//		return calculatedParameters;
//	}
//
//	var calculatedValuesMapping = {};
//	calculatedValuesMapping["@MY_USER"] = function () {
//		return CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USER_ID);
//	};
//
//	calculatedValuesMapping["@MY_GROUP"] = function () {
//		return CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_ID);
//	};
//
//	/**
//	 * @param {Onject} attributeConfiguration - filterObject
//	 * @param {Array} runtimeParameterFields
//	 * 	{
//	 * 		{Ext.form.field} fieldObj,
//	 * 		...
//	 * 	}
//	 */
//	function mergeRuntimeParametersToConf(attributeConfiguration, runtimeParameterFields) {
//		var attributeConf = Ext.clone(attributeConfiguration);
//
//		if (attributeConf) {
//			if (Ext.isObject(attributeConf.simple)) {
//				var conf = attributeConf.simple;
//
//				if (conf.parameterType == "runtime") {
//					var fieldIndex = undefined;
//
//					// Find field index
//					for(var i = 0; i < runtimeParameterFields.length; i++)
//						if (runtimeParameterFields[i].name == conf.attribute)
//							fieldIndex = i;
//
//					if (!Ext.isEmpty(fieldIndex)) {
//						var field = runtimeParameterFields[fieldIndex];
//						var value = [field.getValue()];
//
//						if (field._cmSecondField)
//							value.push(field._cmSecondField.getValue());
//
//						conf.value = value;
//					}
//				} else if (conf.parameterType == "calculated") {
//					var value = conf.value[0];
//
//					if (typeof calculatedValuesMapping[value] == "function")
//						conf.value = [calculatedValuesMapping[value]()];
//				}
//
//			} else if (Ext.isArray(attributeConf.and) || Ext.isArray(attributeConf.or)) {
//				var attributes = attributeConf.and || attributeConf.or;
//
//
//				for (var i = 0; i < attributes.length; ++i)
//					attributes[i] = mergeRuntimeParametersToConf(attributes[i], runtimeParameterFields);
//			}
//
//			return attributeConf;
//		}
//
//		return;
//	}

})();
