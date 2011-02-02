CMDBuild.Utils = (function() {
	var idCounter = 0;
	return {
		nextId: function() {
			return ++idCounter;
		}, 
		Metadata: {
			extractMetaByNS: function(meta, ns) {
				var xaVars = {};
				for (var metaItem in meta) {
					if (metaItem.indexOf(ns)==0) {
						var tmplName = metaItem.substr(ns.length);
						xaVars[tmplName] = meta[metaItem];
					}
				};
				return xaVars;
			}
		},

		Format: {
	        htmlEntityEncode : function(value) {
	            return !value ? value : String(value).replace(/&/g, "&amp;");
	        }
		},

		evalBoolean: function(v) {
			if (typeof v == "string") {
				return v === "true";
			} else {
				return !!v; //return the boolean value of the object
			}
		},
		
		getClassPrivileges: function(classId) {
			var privileges;
			var classTree = CMDBuild.Cache.getTree();
			var table = CMDBuild.Cache.getTableById(classId);
			
			if (table) {
				privileges = {
					write: table.priv_write,
					create: table.priv_create
				};
			} else {
				privileges = {
					write: false,
					create: false
				};
			}
			
			return privileges;
		},
		
		isEmpty: function(o) {
			if (o) {
				for (var i in o) {
					return false;
				}
			}
			return true;
		},
		
		isSimpleTable: function(id) {
			var table = CMDBuild.Cache.getTableById(id);
			if (table) {
				var tGroup = CMDBuild.Cache.getTableGroup(table);
				return tGroup == CMDBuild.Constants.cachedTableType.simpletable;
			} else {
				return false;
			}
		},
		
		/**
		 * for each element call the passed fn,
		 * with scope the element 
		 **/
		foreach: function(array, fn, params) {
			if (array) {
				for (var i=0, l=array.length; i<l; ++i) {
					var element = array[i];
					fn.call(element,params);
				}
			}
		},
		
		groupAttributes: function(attributes, allowNoteFiled) {
			var groups = {};
			var fieldsWithoutGroup = [];
			for ( var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				if (!allowNoteFiled && attribute.name == "Notes") {
					continue;
				} else {
					var attrGroup = attribute.group;
					if (attrGroup) {
						if (!groups[attrGroup]) {
							groups[attrGroup] = [];
						}
						groups[attrGroup].push(attribute);
					} else {
						fieldsWithoutGroup.push(attribute);
					}
				}
			}

			if (fieldsWithoutGroup.length > 0) {
				groups[CMDBuild.Translation.management.modcard.other_fields] = fieldsWithoutGroup;
			}

			return groups;
		}
	};
})();

CMDBuild.extend = function(subClass, superClass) {
	var ob = function() {};
	ob.prototype = superClass.prototype;
	subClass.prototype = new ob();
	subClass.prototype.constructor = subClass;
	subClass.superclass = superClass.prototype;
	if(superClass.prototype.constructor == Object.prototype.constructor) {
		superClass.prototype.constructor = superClass;
	}
};
