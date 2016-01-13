(function() {

	/**
	 * New class to replace CMDBuild.Utils
	 */
	Ext.define('CMDBuild.core.Utils', {

		singleton: true,

		/**
		 * Clones a ExtJs store
		 *
		 * @param {Ext.data.Store} sourceStore
		 *
		 * @return {Ext.data.Store} clonedStore
		 */
		deepCloneStore: function(sourceStore) {
			var clonedStore = Ext.create('Ext.data.Store', {
				model: sourceStore.model
			});

			sourceStore.each(function(record) {
				var newRecordData = Ext.clone(record.copy().data);
				var model = new sourceStore.model(newRecordData, newRecordData.id);

				clonedStore.add(model);
			}, this);

			return clonedStore;
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryTypeId
		 *
		 * @return {Array} out
		 *
		 * TODO: parseInt will be useless when model will be refactored
		 */
		getEntryTypeAncestorsId: function(entryType) {
			var out = [];

			if (!Ext.Object.isEmpty(entryType)) {
				out.push(parseInt(entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID)));

				while (!Ext.isEmpty(entryType.get(CMDBuild.core.proxy.CMProxyConstants.PARENT))) {
					entryType = _CMCache.getEntryTypeById(entryType.get(CMDBuild.core.proxy.CMProxyConstants.PARENT));

					out.push(parseInt(entryType.get(CMDBuild.core.proxy.CMProxyConstants.ID)));
				}
			}

			return out;
		},

		/**
		 * @param {String} className
		 *
		 * @returns {Object}
		 */
		getEntryTypePrivilegesByName: function(className) {
			return _CMUtils.getEntryTypePrivileges(
				_CMCache.getEntryTypeByName(className || '')
			);
		},

		/**
		 * Returns string with custom formatted ExtJs version
		 *
		 * @param {Object} format
		 * 		Ex. {
		 * 			{String} separator,
		 * 			{Boolean} major,
		 * 			{Boolean} minor,
		 * 			{Boolean} patch,
		 * 			{Boolean} release
		 * 		}
		 *
		 * @return {String}
		 */
		getExtJsVersion: function(format) {
			format = format || {};
			format.separator = format.separator || '.';
			format.major = format.major || true;
			format.minor = format.minor || true;
			format.patch = format.patch || true;
			format.release = format.release || false;

			var extjsVersion = Ext.getVersion('extjs');
			var outputArray = [];

			if (format.major)
				outputArray.push(extjsVersion.getMajor());

			if (format.minor)
				outputArray.push(extjsVersion.getMinor());

			if (format.patch)
				outputArray.push(extjsVersion.getPatch());

			if (format.release)
				outputArray.push(extjsVersion.getRelease());

			return outputArray.join(format.separator);
		},

		/**
		 * Default pageSize value is set as 20
		 *
		 * @return {Int} pageSize
		 */
		getPageSize: function() {
			var pageSize;

			try {
				pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
			} catch (e) {
				pageSize = 20;
			}

			return pageSize;
		},

		/**
		 * Returns if string contains HTML tags
		 *
		 * @param {String} inputString
		 *
		 * @return {Boolean}
		 */
		hasHtmlTags: function(inputString) {
			if (typeof inputString == 'string')
				return /<[a-z][\s\S]*>/i.test(inputString);

			return false;
		},

		/**
		 * Evaluates is a string is JSON formatted or not.
		 *
		 * @param {String} string
		 *
		 * @returns {Boolean}
		 */
		isJsonString: function(string) {
			if (Ext.isString(string))
				return !(/[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/.test(string.replace(/"(\\.|[^"\\])*"/g, ''))) && eval('(' + string + ')')

			return false;
		},

		/**
		 * Custom function to order an array of objects or models
		 *
		 * @param {Array} array
		 * @param {String} attributeToSort - (Default) description
		 * @param {String} direction - (Default) ASC
		 * @param {Boolean} caseSensitive - (Default) true
		 */
		objectArraySort: function(array, attributeToSort, direction, caseSensitive) {
			attributeToSort = Ext.isString(attributeToSort) ? attributeToSort : CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;
			direction = Ext.isString(direction) ? direction : 'ASC'; // ASC or DESC
			caseSensitive = Ext.isBoolean(caseSensitive) ? caseSensitive : false;

			if (Ext.isArray(array)) {
				Ext.Array.sort(array, function(item1, item2) {
					var attribute1 = undefined;
					var attribute2 = undefined;

					if (Ext.isFunction(item1.get) && Ext.isFunction(item2.get)) {
						attribute1 = (!caseSensitive && Ext.isFunction(item1.get(attributeToSort).toLowerCase)) ? item1.get(attributeToSort).toLowerCase() : item1.get(attributeToSort);
						attribute2 = (!caseSensitive && Ext.isFunction(item2.get(attributeToSort).toLowerCase)) ? item2.get(attributeToSort).toLowerCase() : item2.get(attributeToSort);
					} else if (item1.hasOwnProperty(attributeToSort) && item2.hasOwnProperty(attributeToSort)) {
						attribute1 = (!caseSensitive && Ext.isFunction(item1[attributeToSort].toLowerCase)) ? item1[attributeToSort].toLowerCase() : item1[attributeToSort];
						attribute2 = (!caseSensitive && Ext.isFunction(item2[attributeToSort].toLowerCase)) ? item2[attributeToSort].toLowerCase() : item2[attributeToSort];
					}

					switch (direction) {
						case 'DESC': {
							if (attribute1 > attribute2)
								return -1;

							if (attribute1 < attribute2)
								return 1;

							return 0;
						} break;

						case 'ASC':
						default: {
							if (attribute1 < attribute2)
								return -1;

							if (attribute1 > attribute2)
								return 1;

							return 0;
						}
					}
				});
			}
		},

		/**
		 * Capitalize first string's char
		 *
		 * @param {String} string
		 *
		 * @return {String} string
		 */
		toTitleCase: function(string) {
			if (typeof string == 'string')
				string = string.charAt(0).toUpperCase() + string.slice(1);

			return string;
		}
	});

	CMDBuild.Utils = (function() {
		var idCounter = 0;

		return {
			mergeCardsData: function(cardData1, cardData2) {
				var out = {};

				for (var prop in cardData1)
					out[prop] = cardData1[prop];

				for (var prop in cardData2) {
					if (out[prop]) {
						if (typeof out[prop] == "object") {
							out[prop] = CMDBuild.Utils.mergeCardsData(cardData1[prop], cardData2[prop]);
						} else {
							continue;
						}
					} else {
						out[prop] = cardData2[prop];
					}
				}

				return out;
			},

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

			lockCard: {
				isEnabled: function() {
					var enabled = CMDBuild.Config.cmdbuild.lockcardenabled;

					return _CMUtils.evalBoolean(enabled);
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
				var entryType = _CMCache.getEntryTypeById(classId);

				return _CMUtils.getEntryTypePrivileges(entryType);
			},

			getEntryTypePrivileges: function(et) {
				var privileges = {
					write: false,
					create: false,
					crudDisabled: {}
				};

				if (et) {
					var strUiCrud = et.get("ui_card_edit_mode");
					var objUiCrud = Ext.JSON.decode(strUiCrud);
					privileges = {
						write: et.get("priv_write"),
						create: et.isProcess() ? et.isStartable() : et.get("priv_create"),
						crudDisabled: objUiCrud
					};
				}

				return privileges;
			},

			getEntryTypePrivilegesByCard: function(card) {
				var privileges = {
					write: false,
					create: false,
					crudDisabled: {}
				};

				if (card) {
					var entryTypeId = card.get("IdClass");
					var entryType = _CMCache.getEntryTypeById(entryTypeId);

					privileges = _CMUtils.getEntryTypePrivileges(entryType);
				}

				return privileges;
			},

			isSimpleTable: function(id) {
				var table = _CMCache.getEntryTypeById(id);

				if (table) {
					return table.data.tableType == CMDBuild.Constants.cachedTableType.simpletable;
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

				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];

					if (!attribute)
						continue;

					if (!allowNoteFiled && attribute.name == "Notes") {
						continue;
					} else {
						var attrGroup = attribute.group;
						if (attrGroup) {
							if (!groups[attrGroup])
								groups[attrGroup] = [];

							groups[attrGroup].push(attribute);
						} else {
							fieldsWithoutGroup.push(attribute);
						}
					}
				}

				if (fieldsWithoutGroup.length > 0)
					groups[CMDBuild.Translation.management.modcard.other_fields] = fieldsWithoutGroup;

				return groups;
			},

			/**
			 * for each element call the passed fn,
			 * with scope the element
			 **/
			foreach: function(array, fn, params) {
				if (array) {
					for (var i = 0, l = array.length; i < l; ++i) {
						var element = array[i];
						fn.call(element,params);
					}
				}
			},

			/**
			 *
			 * @param {array} array an array in which search something
			 * @param {function} fn a function that is called one time for each
			 * element in the array. The function must return true if the
			 * item is the searched
			 *
			 * @returns an object of the array if the passed function return true, or null
			 */
			arraySearchByFunction: function(array, fn) {
				if (!Ext.isArray(array) || !Ext.isFunction(fn))
					return null;

				for (var i = 0, l = array.length; i < l; ++i) {
					var el = array[i];

					if (fn(el))
						return el;
				}

				return null;
			},

			isSuperclass: function(idClass) {
				var c =  _CMCache.getEntryTypeById(idClass);

				if (c) {
					return c.get("superclass");
				} else {
					// TODO maybe is not the right thing to do...
					return false;
				}
			},

			getAncestorsId: function(entryTypeId) {
				var et = null;
				var out = [];

				if (Ext.getClassName(entryTypeId) == "CMDBuild.cache.CMEntryTypeModel") {
					et = entryTypeId;
				} else {
					et = _CMCache.getEntryTypeById(entryTypeId);
				}

				if (et) {
					out.push(et.get("id"));

					while (et.get("parent") != "") {
						et = _CMCache.getEntryTypeById(et.get("parent"));
						out.push(et.get("id"));
					}
				}

				return out;
			},

			getDescendantsById: function(entryTypeId) {
				var children = this.getChildrenById(entryTypeId);
				var et = _CMCache.getEntryTypeById(entryTypeId);
				var out = [et];

				for (var i = 0; i < children.length; ++i) {
					var c = children[i];
					var leaves = this.getDescendantsById(c.get("id"));

					out = out.concat(leaves);
				}

				return out;
			},

			forwardMethods: function (wrapper, target, methods) {
				if (!Ext.isArray(methods))
					methods = [methods];

				for (var i = 0, l = methods.length; i < l; ++i) {
					var m = methods[i];

					if (typeof m == "string" && typeof target[m] == "function") {
						var fn = function() {
							return target[arguments.callee.$name].apply(target, arguments);
						};

						fn.$name = m;
						wrapper[m] = fn;
					}
				}
			},

			getChildrenById: function(entryTypeId) {
				var ett = _CMCache.getEntryTypes();
				var out = [];

				for (var et in ett) {
					et = ett[et];

					if (et.get("parent") == entryTypeId)
						out.push(et);
				}

				return out;
			},

			grid: {
				getPageSize: function getPageSize() {
					var pageSize;

					try {
						pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
					} catch (e) {
						pageSize = 20;
					}

					return pageSize;
				},

				getPageNumber: function getPageNumber(cardPosition) {
					var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
					var pageNumber = 1;

					if (cardPosition == 0)
						return pageNumber;

					if (cardPosition)
						pageNumber = parseInt(cardPosition) / pageSize;

					return pageNumber + 1;
				}
			},

			PollingFunction: function(conf) {
				var DEFAULT_DELAY = 500;
				var DEFAULT_MAX_TIMES = 60;

				this.success =  conf.success || Ext.emptyFn;
				this.failure = conf.failure || Ext.emptyFn;
				this.checkFn = conf.checkFn || function() { return true; };
				this.cbScope = conf.cbScope || this;
				this.delay = conf.delay || DEFAULT_DELAY;
				this.maxTimes = conf.maxTimes || DEFAULT_MAX_TIMES;
				this.checkFnScope = conf.checkFnScope || this.cbScope;

				this.run = function() {
					if (this.maxTimes == DEFAULT_MAX_TIMES)
						CMDBuild.LoadMask.get().show();

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

	Ext.define("CMDBuild.Utils.CMRequestBarrier", {
		constructor: function(cb) {
			var me = this;

			this.dangling = 1;

			this.cb = function () {
				me.dangling--;

				if (me.dangling == 0)
					cb();
			};
		},

		getCallback: function() {
			this.dangling++;
			return this.cb;
		},

		start: function() {
			this.cb();
		}
	});

	CMDBuild.extend = function(subClass, superClass) {
		var ob = function() {};

		ob.prototype = superClass.prototype;
		subClass.prototype = new ob();
		subClass.prototype.constructor = subClass;
		subClass.superclass = superClass.prototype;

		if(superClass.prototype.constructor == Object.prototype.constructor)
			superClass.prototype.constructor = superClass;
	};

	CMDBuild.isMixedWith = function(obj, mixinName) {
		var m = obj.mixins || {};

		for (var key in m) {
			var mixinObj = m[key];

			if (Ext.getClassName(mixinObj) == mixinName)
				return true;
		}

		return false;
	};

	CMDBuild.instanceOf = function(obj, className) {
		while (obj) {
			if (Ext.getClassName(obj) == className)
				return true;

			obj = obj.superclass;
		}

		return false;
	};

	CMDBuild.checkInterface = function(obj, interfaceName) {
		return CMDBuild.isMixedWith(obj, interfaceName) || CMDBuild.instanceOf(obj, interfaceName);
	};

	CMDBuild.validateInterface = function(obj, interfaceName) {
		CMDBuild.IS_NOT_CONFORM_TO_INTERFACE = "The object {0} must implement the interface: {1}";

		if (!CMDBuild.checkInterface(obj, interfaceName))
			throw Ext.String.format(CMDBuild.IS_NOT_CONFORM_TO_INTERFACE, obj.toString(), interfaceName);
	};

})();