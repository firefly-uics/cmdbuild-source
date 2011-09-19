(function() {
	var CLASS_GROUP = "class",
		PROCESS_GROUP = "processclass",
		tr = CMDBuild.Translation,
		constants = CMDBuild.Constants;

	Ext.define("CMDBuild.cache.CMCache", {
		extend: "Ext.util.Observable",
		mixins: {
			lookup: "CMDBUild.cache.CMCacheLookupFunctions",
			entryType: "CMDBUild.cache.CMCacheClassFunctions",
			groups: "CMDBUild.cache.CMCacheGroupsFunctions",
			domains: "CMDBUild.cache.CMCacheDomainFunctions",
			reports: "CMDBUild.cache.CMCacheReportFunctions"
		},

		constructor: function() {
			this._lookupTypes={};

			this.toString = function() {
				return "CMCache";
			};

			this.callParent(arguments);
			this.mapOfAttributes = {};
			this.mapOfReferenceStore = {};
		},

		getAttributeList: function(idClass, callback) {
			if (this.mapOfAttributes[idClass]) {
				var attributes = this.mapOfAttributes[idClass];
				callback(attributes);
			} else {
				this.loadAttributes(idClass, callback);
			}
		},

		loadAttributes: function(classId, callback) {
			CMDBuild.Ajax.request({
				url : 'services/json/schema/modclass/getattributelist',
				method : 'POST',
				params : {
					idClass : classId,
					active: true
				},
				scope: this,
				success: function(response, options, result) {
					attributes = result.rows;
					attributes.sort(function(a,b){return a.index - b.index;});
					this.mapOfAttributes[classId] = attributes;
					if (callback) {
						callback(attributes);
					}
				}
			});
		},
		
		getReferenceStore: function(reference) {
			var referencedIdClass = reference.referencedIdClass;
			var fieldFilter = false;
			if (reference.fieldFilter) {
				//build a non cached store with the filter active
				var oneTimeStore = this.buildReferenceStore(reference);
				//set the fieldFilter to false and save the current value
				//of the fieldFilter to allow the building of a full store
				fieldFilter = reference.fieldFilter;
				reference.fieldFilter = false;
			}
			//build a not filtered store and cache it
			if (!this.mapOfReferenceStore[referencedIdClass]) {		
				this.mapOfReferenceStore[referencedIdClass] = this.buildReferenceStore(reference);
			}
			//restore the fieldFilter
			if (fieldFilter) {
				reference.fieldFilter = fieldFilter;
			}
			return oneTimeStore || this.mapOfReferenceStore[referencedIdClass];
		},
		
		getReferenceStoreById: function(id) {
			return this.mapOfReferenceStore[id];
		},
		
		//private
		buildReferenceStore: function(reference) {
			var baseParams = this.buildParamsForReferenceRequest(reference),
				isOneTime = baseParams.CQL ? true : false,
				maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
			
			return new Ext.data.JsonStore({
				model : "CMDBuild.cache.CMReferenceStoreModel",
				isOneTime: isOneTime,
				baseParams: baseParams, //retro-compatibility,
				pageSize: maxCards,
				proxy: {
					type: 'ajax',
					url: 'services/json/management/modcard/getcardlistshort',
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: baseParams
				},
				sortInfo: {
					field: 'Description',
					direction: 'ASC' 
				},
				autoLoad : true
			});
		},
		
		//private
		buildParamsForReferenceRequest: function(reference) {
			var baseParams = {
				IdClass: reference.referencedIdClass
			};
			if (reference.fieldFilter) {
				baseParams.CQL = reference.fieldFilter;
			} else {
				baseParams.NoFilter = true;
			}
			return baseParams;
		},
	
		getForeignKeyStore: function(foreignKye) {
			var maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit),
				baseParams = { 
					limit: maxCards,
					IdClass: foreignKye.fkDestination,
					NoFilter: true
				};

			return new Ext.data.JsonStore({
				model : "CMDBuild.cache.CMReferenceStoreModel",
				baseParams: baseParams, //retro-compatibility
				proxy: {
					type: 'ajax',
					url: 'services/json/management/modcard/getcardlistshort',
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams: baseParams
				},
				sortInfo: {
					field: 'Description',
					direction: 'ASC' 
				},
				autoLoad : true
			});
		},

		isDescendant: function(subclassId, superclassId) {
			function isDescendant(tree, superclassId, subclassId) {
				if (!tree) { // don't know if this is needed
					return false;
				}

				var ids = {};
				tree.cascade(function() { ids[this.id]=this; });
				var subClass = ids[subclassId];
				var superClass = ids[superclassId];
				return superClass && subClass && subClass.isAncestor(superClass);
			};
			return superclassId == subclassId
				|| isDescendant(this.getTree(CMDBuild.Constants.treeNames.classTree), superclassId, subclassId)
				|| isDescendant(this.getTree(CMDBuild.Constants.treeNames.processTree), superclassId, subclassId);
		},

		onClassContentChanged: function(idClass) {
			reloadRelferenceStore(this.mapOfReferenceStore, idClass);
		},

		getTableGroup: getTableGroup
	});

	function getTableGroup (table) {
		//the simple table are discriminate by the tableType
		var type;
		if (table.tableType && table.tableType != "standard") {
			type = table.tableType;
		} else {
			type = table.type;
		}
		
		if (constants.cachedTableType[type]) {
			return type; 
		} else {
			throw new Error("Unsupported node type: "+type);
		}
	};

	function addAttributesToDomain(rawDomain, domain) {
		var rawAttributes = rawDomain.attributes;
		var attributeLibrary = domain.getAttributeLibrary();
		for (var i=0, l=rawAttributes.length; i<l; ++i) {
			
			var rawAttribute = rawAttributes[i];
			try {
				var attr = CMDBuild.core.model.CMAttributeModel.buildFromJson(rawAttribute);
				attributeLibrary.add(attr);
			} catch (e) {
				_debug(e);
			}
		}
	}

	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}

	function reloadRelferenceStore(stores, idClass) {
		var anchestors = _CMUtils.getAncestorsId(idClass);
		Ext.Array.each(anchestors, function(id) {
			var store = stores[id];
			if (store) {
				store.load();
			}
		});
	}

	CMDBuild.Cache = new CMDBuild.cache.CMCache();
	_CMCache = CMDBuild.Cache; //to uniform the variable names, maybe a day I'll can delete CMDBuild.Cache
})();