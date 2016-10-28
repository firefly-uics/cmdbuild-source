(function () {

	Ext.require(['CMDBuild.core.constants.Global']);

	/**
	 * New class to replace CMDBuild.Utils
	 */
	Ext.define('CMDBuild.core.Utils', {

		singleton: true,

		/**
		 * Decode variable as boolean ('true', 'on', 'false', 'off') case unsensitive
		 *
		 * @param {Mixed} variable
		 *
		 * @returns {Boolean}
		 */
		decodeAsBoolean: function (variable) {
			if (!Ext.isEmpty(variable)) {
				switch (Ext.typeOf(variable)) {
					case 'boolean':
						return variable;

					case 'number':
						return variable != 0;

					case 'string':
						return variable.toLowerCase() == 'true' || variable.toLowerCase() == 'on';
				}
			}

			return false;
		},

		/**
		 * Clones a ExtJs store
		 *
		 * @param {Ext.data.Store} sourceStore
		 *
		 * @returns {Ext.data.Store} clonedStore
		 */
		deepCloneStore: function (sourceStore) {
			var clonedStore = Ext.create('Ext.data.Store', { model: sourceStore.model });

			sourceStore.each(function (record) {
				var newRecordData = Ext.clone(record.copy().data);
				var model = new sourceStore.model(newRecordData, newRecordData.id);

				clonedStore.add(model);
			}, this);

			return clonedStore;
		},

		/**
		 * @param {Object} meta
		 * @param {String} ns
		 *
		 * @returns {Object} xaVars
		 *
		 * @deprecated
		 */
		extractMetadataByNamespace: function (meta, ns) {
			_deprecated('extractMetadataByNamespace', this);

			var xaVars = {};

			for (var metaItem in meta) {
				if (metaItem.indexOf(ns)==0) {
					var tmplName = metaItem.substr(ns.length);

					xaVars[tmplName] = meta[metaItem];
				}
			};

			return xaVars;
		},

		/**
		 * @param {Object} wrapper
		 * @param {Object} target
		 * @param {Array} methods
		 *
		 * @returns {Array} out
		 */
		forwardMethods: function (wrapper, target, methods) {
			_deprecated('forwardMethods', this);

			if (!Ext.isArray(methods))
				methods = [methods];

			for (var i = 0, l = methods.length; i < l; ++i) {
				var m = methods[i];

				if (typeof m == 'string' && typeof target[m] == 'function') {
					var fn = function () {
						return target[arguments.callee.$name].apply(target, arguments);
					};

					fn.$name = m;
					wrapper[m] = fn;
				}
			}
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel or Number} entryTypeId
		 *
		 * @returns {Array} out
		 *
		 * @deprecated CMDBuild.core.Utils.getEntryTypeAncestorsId
		 */
		getAncestorsId: function (entryTypeId) {
			_deprecated('getAncestorsId', this);

			var et = null;
			var out = [];

			if (Ext.getClassName(entryTypeId) == 'CMDBuild.cache.CMEntryTypeModel') {
				et = entryTypeId;
			} else {
				et = _CMCache.getEntryTypeById(entryTypeId);
			}

			if (et) {
				out.push(et.get('id'));

				while (!Ext.isEmpty(et) && et.get('parent') != '') {
					et = _CMCache.getEntryTypeById(et.get('parent'));

					if (!Ext.isEmpty(et))
						out.push(et.get('id'));
				}
			}

			return out;
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * @returns {Array} out
		 *
		 * @deprecated
		 *
		 * FIXME: parseInt will be useless when model will be refactored
		 */
		getEntryTypeAncestorsId: function (entryType) {
			_deprecated('getEntryTypeAncestorsId', this);

			var out = [];

			if (!Ext.Object.isEmpty(entryType)) {
				out.push(parseInt(entryType.get(CMDBuild.core.constants.Proxy.ID)));

				while (!Ext.isEmpty(entryType) && !Ext.isEmpty(entryType.get(CMDBuild.core.constants.Proxy.PARENT))) {
					entryType = _CMCache.getEntryTypeById(entryType.get(CMDBuild.core.constants.Proxy.PARENT));

					if (!Ext.isEmpty(entryType))
						out.push(parseInt(entryType.get(CMDBuild.core.constants.Proxy.ID)));
				}
			}

			return out;
		},

		/**
		 * @param {Ext.data.Model} et
		 *
		 * @returns {Object}
		 *
		 * @deprecated
		 */
		getEntryTypePrivileges: function (et) {
			var privileges = {
				write: false,
				create: false,
				crudDisabled: {}
			};

			if (Ext.isObject(et) && !Ext.Object.isEmpty(et)) {
				var strUiCrud = et.get('ui_card_edit_mode'),
					objUiCrud = Ext.decode(strUiCrud);

				privileges = {
					write: et.get('priv_write'),
					create: et.isProcess() ? et.isStartable() : et.get('priv_create'),
					crudDisabled: objUiCrud
				};
			}

			return privileges;
		},

		/**
		 * @param {Ext.data.Model} card
		 *
		 * @returns {Object}
		 *
		 * @deprecated
		 */
		getEntryTypePrivilegesByCard: function (card) {
			_deprecated('getEntryTypePrivilegesByCard', this);

			var privileges = {
				write: false,
				create: false,
				crudDisabled: {}
			};

			if (Ext.isObject(card) && !Ext.Object.isEmpty(card))
				privileges = CMDBuild.core.Utils.getEntryTypePrivileges(
					_CMCache.getEntryTypeById(
						card.get('IdClass')
					)
				);

			return privileges;
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Object}
		 *
		 * @deprecated
		 */
		getEntryTypePrivilegesById: function (id) {
			_deprecated('getEntryTypePrivilegesById', this);

			return CMDBuild.core.Utils.getEntryTypePrivileges(
				_CMCache.getEntryTypeById(id)
			);
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Object}
		 *
		 * @deprecated
		 */
		getEntryTypePrivilegesByName: function (name) {
			_deprecated('getEntryTypePrivilegesByName', this);

			return CMDBuild.core.Utils.getEntryTypePrivileges(
				_CMCache.getEntryTypeByName(name || '')
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
		 * @returns {String}
		 */
		getExtJsVersion: function (format) {
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
		 * @param {String} cardPosition
		 *
		 * @returns {Number} pageNumber
		 */
		getPageNumber: function (cardPosition) {
			if (cardPosition == 0)
				return 1;

			if (!Ext.isEmpty(cardPosition))
				return Math.floor((parseInt(cardPosition) / CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT)) + 1);

			return 2;
		},

		/**
		 * @param {Array} attributes
		 * @param {Array} attributesNamesToFilter
		 *
		 * @returns {Object} groups
		 */
		groupAttributesObjects: function (attributes, attributesNamesToFilter) {
			attributesNamesToFilter = Ext.isArray(attributesNamesToFilter) ? attributesNamesToFilter : [];
			attributesNamesToFilter.push('Notes');

			var groups = {},
				withoutGroup = [];

			Ext.Array.forEach(attributes, function (attribute, i, allAttributes) {
				if (
					!Ext.isEmpty(attribute)
					&& !Ext.Array.contains(attributesNamesToFilter, attribute[CMDBuild.core.constants.Proxy.NAME])
				) {
					if (Ext.isEmpty(attribute[CMDBuild.core.constants.Proxy.GROUP])) {
						withoutGroup.push(attribute);
					} else {
						if (Ext.isEmpty(groups[attribute[CMDBuild.core.constants.Proxy.GROUP]]))
							groups[attribute[CMDBuild.core.constants.Proxy.GROUP]] = [];

						groups[attribute[CMDBuild.core.constants.Proxy.GROUP]].push(attribute);
					}
				}
			}, this);

			if (!Ext.isEmpty(withoutGroup))
				groups[CMDBuild.Translation.management.modcard.other_fields] = withoutGroup;

			return groups;
		},

		/**
		 * Returns if string contains HTML tags
		 *
		 * @param {String} inputString
		 *
		 * @returns {Boolean}
		 */
		hasHtmlTags: function (inputString) {
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
		isJsonString: function (string) {
			if (Ext.isString(string) && !Ext.isEmpty(string))
				return !(/[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/.test(string.replace(/"(\\.|[^"\\])*"/g, ''))) && eval('(' + string + ')')

			return false;
		},

		/**
		 * @param {Object} object
		 *
		 * @returns {Boolean}
		 */
		isObjectEmpty: function (object) {
			if (Ext.isObject(object)) {
				var isEmpty = true;

				Ext.Object.each(object, function (key, value, myself) {
					if (Ext.isObject(value)) {
						if (!Ext.Object.isEmpty(value)) {
							isEmpty = false;

							return false;
						}
					} else {
						if (!Ext.isEmpty(value)) {
							isEmpty = false;

							return false;
						}
					}
				}, this);

				return isEmpty;
			}

			return false;
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Boolean}
		 *
		 * @deprecated
		 */
		isSimpleTable: function (id) {
			_deprecated('isSimpleTable', this);

			var table = _CMCache.getEntryTypeById(id);

			if (table) {
				return table.data.tableType == CMDBuild.core.constants.Global.getTableTypeSimpleTable();
			} else {
				return false;
			}
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Boolean}
		 *
		 * @deprecated
		 */
		isSuperclass: function (id) {
			_deprecated('isSimpleTable', this);

			var c = _CMCache.getEntryTypeById(id);

			if (c) {
				return c.get('superclass');
			} else {
				// TODO maybe is not the right thing to do...
				return false;
			}
		},

		/**
		 * Custom function to order an array of objects or models
		 *
		 * @param {Array} array
		 * @param {String} attributeToSort - (Default) description
		 * @param {String} direction - (Default) ASC
		 * @param {Boolean} caseSensitive - (Default) true
		 *
		 * @returns {Array}
		 */
		objectArraySort: function (array, attributeToSort, direction, caseSensitive) {
			attributeToSort = Ext.isString(attributeToSort) ? attributeToSort : CMDBuild.core.constants.Proxy.DESCRIPTION;
			direction = Ext.isString(direction) ? direction : 'ASC'; // ASC or DESC
			caseSensitive = Ext.isBoolean(caseSensitive) ? caseSensitive : false;

			if (Ext.isArray(array)) {
				return Ext.Array.sort(array, function (item1, item2) {
					var attribute1 = undefined;
					var attribute2 = undefined;

					if (Ext.isFunction(item1.get) && Ext.isFunction(item2.get)) {
						attribute1 = (!caseSensitive && Ext.isFunction(item1.get(attributeToSort).toLowerCase)) ? item1.get(attributeToSort).toLowerCase() : item1.get(attributeToSort);
						attribute2 = (!caseSensitive && Ext.isFunction(item2.get(attributeToSort).toLowerCase)) ? item2.get(attributeToSort).toLowerCase() : item2.get(attributeToSort);
					} else if (!Ext.isEmpty(item1[attributeToSort]) && !Ext.isEmpty(item2[attributeToSort])) {
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

			return array;
		},

		/**
		 * @param {String} label
		 *
		 * @returns {String}
		 */
		prependMandatoryLabel: function (label) {
			if (!Ext.isEmpty(label) && Ext.isString(label))
				return CMDBuild.core.constants.Global.getMandatoryLabelFlag() + label;

			return label;
		},

		/**
		 * Capitalize first string's char
		 *
		 * @param {String} string
		 *
		 * @returns {String} string
		 */
		toTitleCase: function (string) {
			if (Ext.isString(string) && !Ext.isEmpty(string))
				string = string.charAt(0).toUpperCase() + string.slice(1);

			return string;
		}
	});

	/**
	 * @deprecated
	 */
	CMDBuild.extend = function (subClass, superClass) {
		var ob = function () {};

		ob.prototype = superClass.prototype;
		subClass.prototype = new ob();
		subClass.prototype.constructor = subClass;
		subClass.superclass = superClass.prototype;

		if(superClass.prototype.constructor == Object.prototype.constructor)
			superClass.prototype.constructor = superClass;
	};

	/**
	 * @deprecated
	 */
	CMDBuild.isMixedWith = function (obj, mixinName) {
		var m = obj.mixins || {};

		for (var key in m) {
			var mixinObj = m[key];

			if (Ext.getClassName(mixinObj) == mixinName)
				return true;
		}

		return false;
	};

	/**
	 * @deprecated
	 */
	CMDBuild.instanceOf = function (obj, className) {
		while (obj) {
			if (Ext.getClassName(obj) == className)
				return true;

			obj = obj.superclass;
		}

		return false;
	};

	/**
	 * @deprecated
	 */
	CMDBuild.checkInterface = function (obj, interfaceName) {
		return CMDBuild.isMixedWith(obj, interfaceName) || CMDBuild.instanceOf(obj, interfaceName);
	};

	/**
	 * @deprecated
	 */
	CMDBuild.validateInterface = function (obj, interfaceName) {
		CMDBuild.IS_NOT_CONFORM_TO_INTERFACE = 'The object {0} must implement the interface: {1}';

		if (!CMDBuild.checkInterface(obj, interfaceName))
			throw Ext.String.format(CMDBuild.IS_NOT_CONFORM_TO_INTERFACE, obj.toString(), interfaceName);
	};

})();
