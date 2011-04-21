(function() {
	Ext.ns("CMDBuild.cache");
	
	var CLASS_GROUP = "class";
	var PROCESS_GROUP = "processclass";

	var tr = CMDBuild.Translation;
	var constants = CMDBuild.Constants;
	var getTableGroup = function(table) {
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
	
	var onNewNode = function(parameters) {
		var group = this.setTable(parameters);
		this.publish("cmdb-new-"+group, parameters);
	};
	
	var onModifyNode = function(parameters) {
		var oldId = parameters.oldId;
		var table;
		
		// Looking for the oldId, because the lookupType
		// use the name as identifier and it can change.
		
		if (oldId) {
			table = this.getTableById(oldId);
			if (table) {
				this.deleteTableById(oldId);
				delete table.oldId;
				this.setTable(parameters);
			}
		} else {
			table = this.getTableById(parameters.id);
			Ext.apply(table, parameters);
		}
		
		var group = getTableGroup(table);		
		this.publish("cmdb-modify-"+group, parameters);
	};
	
	var onDeletedNode = function(parameters) {
		var table = this.getTableById(parameters.id);
		if (table) {
			var group = getTableGroup(table);
			this.publish("cmdb-deleted-"+group, table);
			delete this.tableMaps[group][parameters.id];
		} else {
			throw new Error("CMDBCache: onDeletedNode - The deleted node was not cached");
		}
	};
	
	var onNewTable = function(table) {
		var addTableInStore = function(table, store) {
			if (!store) {
				return;
			}
			var recordTemplate = Ext.data.Record.create( [ {
	            name: 'id',
	            mapping: 'id'
	        },{
	            name: 'description',
	            mapping: 'description'
	        },{
	        	name: 'name',
	            mapping: 'name'
	        }]);
			if (table.selectable) {
				var r = new recordTemplate({
					id: table.id,
					description: table.text,
					name: table.name
				});
				store.addSorted(r);
			};	
		};

		addTableInStore(table, this.classesStore);
		if (!table.superclass) {
			addTableInStore(table, this.classesStoreWithoutSuperclasses);
		}
		addTableInStore(table, this.classesAndProcessStore);
		addTableInStore(table, this.classesAndProcessStoreWithEmptyOption);
	};
	
	var onModifyTable = function(table) {
		var modifyTableInStore = function(table, store) {
			if (!store) {
				return;
			}
			var recordIndex = store.find("id", table.id); // return the matched index or -1
			if (recordIndex > -1) {
				var record = store.getAt(recordIndex);
				record.set("description", table.text);
			}
		};

		modifyTableInStore(table, this.classesStore);
		modifyTableInStore(table, this.classesStoreWithoutSuperclasses);
		modifyTableInStore(table, this.classesAndProcessStore);
		modifyTableInStore(table, this.classesAndProcessStoreWithEmptyOption);
	};
	
	var onDeletedTable = function(table) {
		var deleteTableInStore = function(table, store) {
			if (!store) {
				return;
			}
			var recordIndex = store.find("id", table.id);
			if (recordIndex > -1) {
				store.removeAt(recordIndex);
			}
		};
		
		deleteTableInStore(table, this.classesStore);
		deleteTableInStore(table, this.classesAndProcessStore);
		deleteTableInStore(table, this.classesAndProcessStoreWithEmptyOption);
	};
	
	var buildTableStore = function(cache, types, addEmptyOption, excludeSuperclasses) {		
		var classes = {};
		for (var i=0, l=types.length; i<l; ++i) {
			var tableMapKey = constants.cachedTableType[types[i]];
			Ext.apply(classes,cache.tableMaps[tableMapKey]);
		}
		var store = new Ext.data.SimpleStore({
			fields: ["id", "description", "name"],
			sortInfo: { field: "description", dir: 'ASC' },
			data: (function() {
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					if (table.selectable && !(excludeSuperclasses && table.superclass)) {
						data.push([table.id, table.text, table.name]);
					}
				}
				return data;
			})()
		});
		
		if (addEmptyOption) {
			store.insert(0, new store.recordType({ id: 0, description: "-", name:"" }));
		}
		return store;
	};
	
	var lookupTypeStore = null;
	
	CMDBuild.cache.CMCache = Ext.extend(Ext.Component, {
		initComponent : function() {
			this.toString = function() {
				return "CMCache";
			};
			
			CMDBuild.cache.CMCache.superclass.initComponent.apply(this, arguments);
			
			this.tableMaps = {}; // a type grouped map of all the tables
			this.mapOfTree = {};
			this.mapOfAttributes = {};
			this.mapOfLookupStore = {};
			this.mapOfReferenceStore = {};
			
			this.classesStore = undefined;
			this.classesAndProcessStore = undefined;
			
			
			this.subscribe("cmdb-delete-card", this.reloadReferenceStore, this);
			this.subscribe("cmdb-reload-card", this.reloadReferenceStore, this);
			
			this.subscribe("cmdb-new-class", onNewTable, this);
			this.subscribe("cmdb-modify-class", onModifyTable, this);
			this.subscribe("cmdb-deleted-class", onDeletedTable, this);
			
			this.subscribe("cmdb-new-processclass", onNewTable, this);
			this.subscribe("cmdb-modify-processclass", onModifyTable, this);
			this.subscribe("cmdb-deleted-processclass", onDeletedTable, this);
			
			this.subscribe("cmdb-modified-classattribute", function(p) {
				this.loadAttributes(p.idClass);
			}, this);
			this.subscribe("cmdb-modified-processclassattribute",  function(p) {
				this.loadAttributes(p.idClass);
			}, this);
			
			this.subscribe("cmdb-new-node", onNewNode, this);
			this.subscribe("cmdb-modify-node", onModifyNode, this);
			this.subscribe("cmdb-deleted-node", onDeletedNode, this);
		},
		
		setTables: function(tables) {
			for (var i=0, len=tables.length; i<len; ++i) {
				var table = tables[i];
				this.setTable(table);
			}
		},
		
		setTable: function(table) {
			var group = getTableGroup(table);
			table.group = group;
			
			if (!this.tableMaps[group]) {
				this.tableMaps[group] = {};
			}
			this.tableMaps[group][table.id] = table;
			
			if (table.domains) {
				for (var i=0, l=table.domains.length; i<l; ++i) {
					var rawDomain = table.domains[i];
					try {
						var domain = CMDBuild.core.model.CMDomainModel.buildFromJSON(rawDomain);
						CMDomainModelLibrary.add(domain);
					} catch (e) {
						_debug(e, "I can not add this domain", rawDomain);
					}
				}
			}
			
			return group;
		},
		
		getTablesByGroup: function(groupName) {
			return this.tableMaps[groupName];
		},

		getClassById: function(tableId) {
			var classes = this.getTablesByGroup(CLASS_GROUP);
			if (classes) {
				return classes[tableId];
			} else {
				return undefined;
			}
		},

		getTableById: function(tableId) {
			for (var group in this.tableMaps) {
				if (this.tableMaps[group][tableId]) {
					return this.tableMaps[group][tableId];
				}
			}
			return undefined;
		},
		
		deleteTableById: function(tableId) {
			for (var group in this.tableMaps) {
				if (this.tableMaps[group][tableId]) {
					delete this.tableMaps[group][tableId];
					return true;
				}
			}
			return false;
		},
		
		getTree: function(treeName, rootId, rootText, sorted) {
			return CMDBuild.TreeUtility.getTree(treeName, rootId, rootText, sorted);
		},
		
		getClassesTree: function() {
			return this.getTree(CMDBuild.Constants.cachedTableType[CLASS_GROUP]);
		},
		
		reloadReferenceStore: function(p) {
            // reload the store of the given id class and the
			// stores of his anchestores
            var classTree = this.getTree(CMDBuild.Constants.cachedTableType[CLASS_GROUP]);
            var classNode = CMDBuild.TreeUtility.searchNodeByAttribute({
                attribute: 'id',
                value: p.classId,
                root: classTree
            });
            if (classNode) {
                for (var node = classNode; node; node = node.parentNode) {
                    if (this.mapOfReferenceStore[node.attributes.id]) {
	                    CMDBuild.log.info("Reloading the store of ", node.attributes.text);
	                    this.mapOfReferenceStore[node.attributes.id].reload();
                    }
                }
            }
        }, 
		
		setTree: function(treeName, treeStructure, attributes) {
			var tree;
			if (attributes.useServerRoot && treeStructure[0]) {
				//if I want use the structure given from the server and the
				//tree has at least one node
				var rootAttributes = Ext.apply(treeStructure[0], attributes);
				tree = new Ext.tree.TreeNode(rootAttributes);
				tree.attributes.type = 'folder';
				this.appendNodes(tree, treeStructure[0].children);
			} else {
				tree = new Ext.tree.TreeNode(attributes);
				this.appendNodes(tree, treeStructure);
			}
			this.mapOfTree[treeName] = tree;
			CMDBuild.log.debug('Loaded tree', treeName, this.mapOfTree);
		},
		
		getClassesAsStore: function() {
			if (!this.classesStore) {
				this.classesStore = buildTableStore(this, [CLASS_GROUP]);
			}
			return this.classesStore;
		},
		
		getClassesAsStoreWithoutSuperclasses: function(addEmptyOption) {
			if (!this.classesStoreWithoutSuperclasses) {
				this.classesStoreWithoutSuperclasses = buildTableStore(this, [CLASS_GROUP],
						addEmptyOption, excludeSuperclasses=true);
			} 
			return this.classesStoreWithoutSuperclasses;
		},

		getClassesAndProcessAsStore: function() {
			if (!this.classesAndProcessStore) {
				this.classesAndProcessStore = buildTableStore(this, [CLASS_GROUP,PROCESS_GROUP]);
			} 
			return this.classesAndProcessStore;
		},
		
		getClassesAndProcessAsStoreWithEmptyOption: function() {
			if (!this.classesAndProcessStoreWithEmptyOption) {
				this.classesAndProcessStoreWithEmptyOption = buildTableStore(this, [CLASS_GROUP,PROCESS_GROUP], emptyOption=true);
			} 
			return this.classesAndProcessStoreWithEmptyOption;
		},
		
		getNode: function(treeName, nodeId) {
			var tree = this.getTree(treeName);
			var node = CMDBuild.TreeUtility.searchNodeByAttribute({
				attribute: 'id',
		  		value: nodeId,
		   		root: tree 
			});
			return node;
		},
		
		reloadTree: function(treeName) {
			var tree = this.mapOfTree[treeName];
			CMDBuild.TreeUtility.loadDataFromUrl({
				url: tree.attributes.url,
				params: tree.attributes.params,
				targetTreeRoot: tree
			});
		},
		
		appendNodes: function(tree, nodes) {
			//append the result of the server to the root
			//for each node append the children in the attributes map
			//to uniform the tree structure with the Ext one.
			if (tree && nodes) {			
				for (var i=0; i<nodes.length; i++) {
					var cmdbNode = nodes[i];
					var node = new Ext.tree.TreeNode(cmdbNode);
					tree.appendChild(node);
					if (!node.leaf) {
						var cmdbChildren = cmdbNode.children;
						if (cmdbChildren) {
							this.appendNodes(node, cmdbChildren);
						}
					}
				}
			}
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
		
		getLookupStore: function(type) {
			if (!this.mapOfLookupStore[type]) {
				this.mapOfLookupStore[type] = CMDBuild.ServiceProxy.getLookupFieldStore(type);
			}
			return this.mapOfLookupStore[type];
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
			var baseParams = this.buildParamsForReferenceRequest(reference);
			var isOneTime = baseParams.CQL ? true : false;
			var store =  new Ext.data.JsonStore({
				url: 'services/json/management/modcard/getcardlistshort',
				baseParams: baseParams,
		        root: "rows",
	            totalProperty: 'results',
		        fields : ['Id', 'Description'],
		        isOneTime: isOneTime,
		        autoLoad: true,
				sortInfo: {
		        	field: 'Description',
		        	direction: 'ASC' 
		        }
			});
			
	
	        store.on('beforeload', function() {
	        	store.isLoading = true;
	        });
	        
	        store.on('load', function() {
	        	store.isLoading = false;
	        });
			
			return store;
		},
		
		//private
		buildParamsForReferenceRequest: function(reference) {
			var maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
			var baseParams = { 
					limit: maxCards,
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
			var maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
			var baseParams = { 
					limit: maxCards,
					IdClass: foreignKye.fkDestination
			};
			var store =  new Ext.data.JsonStore({
				url: 'services/json/management/modcard/getcardlistshort',
				baseParams: baseParams,
		        root: "rows",
	            totalProperty: 'results',
		        fields : ['Id', 'Description'],
		        autoLoad: true,
				sortInfo: {
		        	field: 'Description',
		        	direction: 'ASC' 
		        }
			});
			
			return store;
		},
		
		
		isDescendant: function(subclassId, superclassId) {
			var isDescendant = function(tree, superclassId, subclassId) {
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
		
		getLookupTypeLeavesAsStore: function() {
			if (lookupTypeStore == null) {
				var lookupTypes = this.tableMaps[constants.cachedTableType.lookuptype];
				lookupTypeStore = new Ext.data.SimpleStore({
					fields : ["type"],
					sortInfo: { field: "type", dir: 'ASC' },
					data: [],
					fill: function() {
						var type = Ext.data.Record.create([
						    {name: 'type', mapping: 'type'}
						]);
						this.removeAll();
						for (var i in lookupTypes) {
							var table = lookupTypes[i];
							if (!table.children) {
								this.addSorted(new type({type: table.id}));
							}
						}
					}
				});
				lookupTypeStore.fill();
				this.subscribe("cmdb-new-lookuptype", lookupTypeStore.fill, lookupTypeStore);
				this.subscribe("cmdb-deleted-lookuptype", lookupTypeStore.fill, lookupTypeStore);
				this.subscribe("cmdb-modified-lookuptype", lookupTypeStore.fill, lookupTypeStore);
			}
			return lookupTypeStore;
		},
		
		syncLookupTypesParent: function(lType) {
			var lookupTypes = this.tableMaps[constants.cachedTableType.lookuptype];
			for (var i in lookupTypes) {
				var table = lookupTypes[i];
				if (table.parent == lType.oldId) {
					table.parent = lType.id;
				}
			}
		},
		getTableGroup: getTableGroup
	});

	CMDBuild.Cache = new CMDBuild.cache.CMCache();
})();