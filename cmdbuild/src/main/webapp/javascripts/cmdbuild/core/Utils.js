CMDBuild.Utils = (function() {
	var idCounter = 0;
	return {
		/*
		 * Used to trace a change in the type of the selection parameter between two minor ExtJS releases
		 */
		getFirstSelection: function(selection) {
			if (Ext.isArray(selection)) {
        		return selection[0];
        	} else {
				return selection;
			}
		},

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

		// FIXME: Should be getEntryTypePrivileges
		getClassPrivileges: function(classId) {
			var entryType = _CMCache.getEntryTypeById(classId)

			if (entryType) {
				privileges = {
					write: entryType.get("priv_write"),
					create: entryType.get("priv_create")
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
			var table = _CMCache.getEntryTypeById(id);
			if (table) {
				return table.data.tableType == CMDBuild.Constants.cachedTableType.simpletable
			} else {
				return false;
			}
		},

		isProcess: function(id) {
			return (!!_CMCache.getProcessById(id));
		},

		groupAttributes: function(attributes, allowNoteFiled) {
			var groups = {};
			var fieldsWithoutGroup = []; 
			for ( var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				if (!attribute) {
					continue;
				}
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

		isSuperclass: function(idClass) {
			var c =  _CMCache.getEntryTypeById(idClass);
			return c.get("superclass");
		},

		getAncestorsId: function(entryTypeId) {
			var et = _CMCache.getEntryTypeById(entryTypeId),
				out = [];

			while (et.get("parent") != "") {
				out.push(et.get("id"));
				et = _CMCache.getEntryTypeById(et.get("parent"));
			}

			return out;
		},

		getDescendantsById: function(entryTypeId) {
			var children = this.getChildrenById(entryTypeId),
				et = _CMCache.getEntryTypeById(entryTypeId),
				out = [et];

			for (var i=0; i<children.length; ++i) {
				var c = children[i],
					leaves = this.getDescendantsById(c.get("id"));
				out = out.concat(leaves);
			}

			return out;
		},

		getChildrenById: function(entryTypeId) {
			var ett = _CMCache.getEntryTypes(),
				out = [];
	
			for (var et in ett) {
				et = ett[et];
				if (et.get("parent") == entryTypeId) {
					out.push(et);
				}
			}
	
			return out;
		},

		PollingFunction: function(conf) {
			var DEFAULT_DELAY = 500,
				DEFAULT_MAX_TIMES = 60;

			this.success =  conf.success || Ext.emptyFn;
			this.failure = conf.failure || Ext.emptyFn;
			this.checkFn = conf.checkFn || function() { return true;};
			this.cbScope = conf.cbScope || this;
			this.delay = conf.delay || DEFAULT_DELAY;
			this.maxTimes = conf.maxTimes || DEFAULT_MAX_TIMES;
			this.checkFnScope = conf.checkFnScope || this.cbScope;

			this.run = function() {
				if (this.maxTimes == DEFAULT_MAX_TIMES) {
					CMDBuild.LoadMask.get().show();
				}
				if (this.maxTimes > 0) {
					if (this.checkFn.call(this.checkFnScope)) {
						_debug("End polling with success");
						CMDBuild.LoadMask.get().hide();
						this.success.call(this.cbScope);
					} else {
						this.maxTimes--;
						Ext.Function.defer(this.run, this.delay, this);
					}
				} else {
					_debug("End polling with failure");
					CMDBuild.LoadMask.get().hide();
					this.failure.call();
				}
			};
		}
	};
})();

_CMUtils = CMDBuild.Utils;

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