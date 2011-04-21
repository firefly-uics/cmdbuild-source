(function() {
	/**
	 * @namespace CMDBuild.core.model
	 */
	Ext.ns("CMDBuild.core.model");
	/**
	 * @class CMModelBuilder build a
	 * data model using the passed structure
	 */
	CMDBuild.core.model.CMModelBuilder = {};
	/**
	 * @function
	 * @static
	 * @param conf The model configuration
	 * @param conf.name The name to use to identify the new model definition
	 * @param conf.structure A map of object that describe the attribute of the Model.
	 * 
	 * @example
	 * CMDBuild.core.model.CMModelBuilder.build({
	 * 	name: "MyModel",
	 * 	structure: {
	 * 		myFirstAttr: {},
	 * 		mySecondAttr: {}
	 * 	}
	 * });
	 * 
	 * @return The definition of a new model
	 * @link CMDBuild.core.model.CMDomainModel
	 */
	CMDBuild.core.model.CMModelBuilder.build = function(conf) {
		checkConfiguration(conf);
		var Model = Ext.extend(Ext.util.Observable, {
			constructor: function(instanceData) {
				checkInstanceData(instanceData, conf.structure);

				this.REC_TEMPLATE = buildRecordTemplate(conf.structure);
				this.NAME = Model.NAME;
				this.STRUCTURE = Model.STRUCTURE;
				this.CMEVENTS = Model.CMEVENTS;
				
				var record = new this.REC_TEMPLATE(instanceData);

				this.getRecord = function() {
					return record;
				};

				this.get = function(name) {
					return record.get(name);
				};
				
				this.set = function(name, value) {
					if (this.STRUCTURE[name]) {
						var old = record.get(name);
						record.set(name, value);
						record.commit();
						this.fireEvent(this.CMEVENTS.CHANGED, {
							attribute: name,
							oldValue: old,
							newValue: value
						});
					} else {
						throw CMDBuild.core.error.model.SET_UNDEFINED_ATTR;
					}
				};
				
				this.update = function(newModel) {
					if (newModel && typeof newModel == "object") {
						if (this.NAME == newModel.NAME) {
							for (var attr in this.STRUCTURE) {
								var myVal = this.get(attr);
								var newVal = newModel["get"+attr]();
								if (myVal != newVal) {
									this.set(attr, newVal);
								}
							}
						} else {
							throw CMDBuild.core.error.model.WRONG_MODEL_TYPE
						}
					} else {
						throw CMDBuild.core.error.model.WRONG_UPDATE_PARAMETER
					}
				};

				this.destroy = function() {
					this.fireEvent(this.CMEVENTS.DESTROY);
				};

				this.toString = function() {
					return Model.NAME;
				};
				
				for (var attr in this.STRUCTURE) {
					buildSetter.call(this, attr);
					buildGetter.call(this, attr);
				}

				Model.superclass.constructor.call(this, arguments);
			}
		});
		
		Model.NAME = conf.name;
		Model.STRUCTURE = conf.structure;
		Model.CMEVENTS = {
			CHANGED: "changed",
			DESTROY: "destroy"
		};
		return Model;
	};

	function checkConfiguration(conf) {
		if (!conf) {
			throw CMDBuild.core.error.model.NO_CONFIGURATION;
		}
		if (!conf.name) {
			throw CMDBuild.core.error.model.NO_CONFIGURATION_NAME;
		}
		if (!conf.structure) {
			throw CMDBuild.core.error.model.NO_CONFIGURATION_STRUCTURE;
		}
		if (!Ext.isObject(conf.structure)) {
			throw CMDBuild.core.error.model.CONFIGURATION_STRUCTURE_IS_NOT_OBJECT;
		}
	}
	
	function buildRecordTemplate(structure) {
		var attributes = [];
		for (var attr in structure) {
			// add the name of the attribute in the structure, to refer to it in the
			// external object
			structure[attr]["name"] = attr;  
			attributes.push(attr);
		}
		return  Ext.data.Record.create(attributes);
	};
	
	function checkInstanceData(instanceData, structure) {
		for (var attributeName in structure) {
			var attr = structure[attributeName];
			if (attr.required && instanceData[attributeName] === undefined) {
				throw CMDBuild.core.error.model.REQUIRED_ATTRIBUTE(attributeName);
			}
		}
	}
	
	function buildSetter(name) {
		var fnName = "set" + name;
		this[fnName] = function(value) {
			this.set(name, value);
		};
	}
	
	function buildGetter(name) {
		var fnName = "get" + name;
		this[fnName] = function() {
			return this.get(name);
		};
	}
})();