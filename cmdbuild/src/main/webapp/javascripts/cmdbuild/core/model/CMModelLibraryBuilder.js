(function() {
	Ext.ns("CMDBuild.core.model");
	/**
	 * @class CMDBuild.core.model.CMModelLibraryBuilder
	 * 
	 * Support class to build a CMLibrary for a given CMModel type
	 */
	CMDBuild.core.model.CMModelLibraryBuilder = {};
	/**
	 * @function
	 * @static
	 * @param conf The library configuration
	 * @param conf.modelName The name of the CMModel to accept as item of this library
	 * @param conf.keyAttribute The attribute of the model to use as key for the internal map.
	 * 
	 * @example
	 * CMDBuild.core.model.CMModelLibraryBuilder.build({
	 * 	modelName: "MyModel",
	 * 	keyAttribute: "id"
	 * });
	 * 
	 * @return The definition of a new modelLibrary
	 * @link CMDBuild.core.model.CMDomainModelLibrary for example
	 */
	CMDBuild.core.model.CMModelLibraryBuilder.build = function(conf) {
		checkConfiguration(conf);
		
		var getKey = function(model) {
			return model["get" + conf.keyAttribute]();
		};
		
		var ALLOWED_MODEL = conf.modelName;
		var KEY_ATTRIBUTE = conf.keyAttribute;
		
		var Library = Ext.extend(Ext.util.Observable, {
			constructor: function() {
				this.CMEVENTS = Library.CMEVENTS;
				this.KEY_ATTRIBUTE = Library.KEY_ATTRIBUTE;
				
				this.map = {},
				this.add = function(model) {
					if (!model) {
						throw CMDBuild.core.error.model.A_MODEL_IS_REQUIRED;
					} else if (model.NAME == ALLOWED_MODEL) {
						if (typeof this.map[getKey(model)] != "undefined") {
							throw CMDBuild.core.error.model.EXISTING_KEY(getKey(model));
						}
						this.map[getKey(model)] = model;
						
						model.on(model.CMEVENTS.DESTROY, function() {
							this.remove(getKey(model));
						}, this);
						
						this.onAdd(model);
						this.fireEvent(this.CMEVENTS.ADD, model);
					} else {
						throw CMDBuild.core.error.model.ADD_WRONG_MODEL_TO_LIBRARY;
					}
				};

				this.remove = function(id) {
					if (id) {
						delete this.map[id];
						this.onRemove(id);
						this.fireEvent(this.CMEVENTS.REMOVE, id);
					} else {
						throw CMDBuild.core.error.model.REMOVE_WITHOUT_ID;
					}
				};

				this.get = function(id) {
					if (id) {
						return this.map[id];
					} else {
						throw CMDBuild.core.error.model.GET_WITHOUT_ID;
					}
				};

				this.count = function() {
					var i = 0;
					for (var k in this.map) {
						++i;
					}
					return i;
				};

				this.clear = function() {
					for (var k in this.map) {
						this.remove(k);
					}
				};

				this.hasModel = function(id) {
					return (typeof this.map[id] === "object");
				};

				this.toString = function() {
					return NS+".CMModelLibraryBuilder<" +ALLOWED_MODEL+ ">";
				};
				
				// templates to extend behavior
				this.onAdd = Ext.emptyFn;
				this.onRemove = Ext.emptyFn;
				Library.superclass.constructor.call(this, arguments);
			}
		});
		Library.ALLOWED_MODEL = ALLOWED_MODEL;
		Library.KEY_ATTRIBUTE = KEY_ATTRIBUTE;
		Library.CMEVENTS = {
			ADD: "add",
			REMOVE: "remove"
		};
		return Library;
	};
	
	function checkConfiguration(conf) {
		if (!conf) {
			throw CMDBuild.core.error.model.NO_CONFIGURATION;
		}
		if (!conf.modelName) {
			throw CMDBuild.core.error.model.NO_MODEL_NAME;
		}
		if (!Ext.isString(conf.modelName)) {
			throw CMDBuild.core.error.model.MODEL_NAME_IS_NOT_STRING;
		}
		if (!conf.keyAttribute) {
			throw CMDBuild.core.error.model.NO_KEY_ATTRIBUTE;
		}
	}
})();