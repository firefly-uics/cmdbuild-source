(function($) {
	var cache = function(callback, callbackScope) {
		$.Cmdbuild.customvariables.cacheProcess = new cacheProcesses();
		$.Cmdbuild.customvariables.cacheDomains = new cacheDomains();
		$.Cmdbuild.customvariables.cacheClasses = new cacheClasses();
		$.Cmdbuild.customvariables.cacheImages = new cacheImages();
		$.Cmdbuild.customvariables.cacheTrees = new cacheTrees();

		// load icons data
		$.Cmdbuild.customvariables.cacheImages.loadData(callback, callbackScope);
	};
	var cacheTrees = function() {
		this.data = {};
		this.currentNavigationTree = null;
		this.getFilterEqual = function(classId) {
			var filter = {
				"attribute" : {
					"simple" : {
						"attribute" : "targetClass",
						"operator" : "equal",
						"value" : [ classId ]
					}
				}
			};
			return filter;
		};
		this.getFilterContain = function(classId) {
			var filter = {
				"attribute" : {
					"simple" : {
						"attribute" : "targetClass",
						"operator" : "contain",
						"value" : [ classId ]
					}
				}
			};
			return filter;
		};
		this.pushTreeForClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, []);
				return;
			}
			var filterEqual = this.getFilterEqual(classId);
			var filterContain = this.getFilterContain(classId);
			$.Cmdbuild.g3d.proxy.getDomainTrees({
				filter : filterEqual
			}, function(treesWithClassLikeRoot) {
					$.Cmdbuild.g3d.proxy.getDomainTrees({
						filter : filterContain
					}, function(treesAboutClass) {
							this.data[classId] = this.merge(
									treesWithClassLikeRoot, treesAboutClass);
							callback.apply(callbackScope, []);
					}, this);
			}, this);
		};
		this.merge = function(ar1, ar2) {
			function isJustHere(ar, value) {
				for (var i = 0; i < ar.length; i++) {
					if (value === ar[i]._id) {
						return true;
					}
				}
				return false;
			}
			for (var i = 0; i < ar2.length; i++) {
				if (!isJustHere(ar1, ar2[i]._id)) {
					ar1.push(ar2[i]);
				}
				return ar1;
			}
		};
		this.getTreesFromClass = function(classId) {
			if (!classId) {
				return [];
			}
			if (this.data[classId] && this.data[classId].length > 0) {
				return this.data[classId];
			}
			var classType = $.Cmdbuild.customvariables.cacheClasses
					.getClass(classId);
			if (classType && classType.parent) {
				return this.getTreesFromClass(classType.parent);
			}
			return [];
		};
		this.setCurrentNavigationTree = function(navigationTree, callback, callbackScope) {
			if (navigationTree) {
				$.Cmdbuild.g3d.proxy.getDomainTree(navigationTree, function(
						tree) {
					this.currentNavigationTree = tree;
					callback.apply(callbackScope, []);
				}, this);
			}
		};
		this.getCurrentNavigationTree = function() {
			return this.currentNavigationTree;
		};
	};
	var cacheProcesses = function() {
		this.data = {};
		$.Cmdbuild.utilities.proxy.getProcesses(function(processes) {
			for (var i = 0; i < processes.length; i++) {
				this.data[processes[i]._id] = true;
			}
		}, this);
		this.isProcess = function(processId) {
			return (this.data[processId]) ? true : false;
		};
	};
	var cacheImages = function() {
		this.data = [];
		this.loadData = function(callback, callbackScope) {
			var me = this;
			$.Cmdbuild.utilities.proxy.getIcons({}, function(data, metadata) {
				me.data = data;
				callback.apply(callbackScope);
			});
		};
		this.getBaseImages = function(type) {
			var base_url = $.Cmdbuild.global.getAppConfigUrl() + $.Cmdbuild.g3d.constants.SPRITES_PATH;
			switch (type) {
				case "default" :
					return base_url + "default.png";
					break;
				case "selected" :
					return base_url + "selected.png";
					break;
				case "current" :
					return base_url + "current.png";
					break;
				default:
					return "";
			}
		};
		this.getImage = function(classId) {
			var imgs = $.grep(this.data, function(item) {
				return item.details.id === classId;
			});
			var url;
			if (imgs && imgs.length) {
				url = $.Cmdbuild.utilities.proxy.getURIForIconDownload(imgs[0]._id);
			} else {
				url = $.Cmdbuild.customvariables.cacheImages.getBaseImages("default");
			}
			return url;
		};
	};
	var cacheClasses = function() {
		this.data = {};
		this.getLoadingClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, [ this.data[classId] ]);
			} else if (classId === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				callback.apply(callbackScope,
						[ [ $.Cmdbuild.g3d.constants.COMPOUND_ATTRIBUTES ] ]);
			} else {
				$.Cmdbuild.g3d.proxy.getClass(classId,
						function(classAttributes) {
							this.data[classId] = classAttributes;
							$.Cmdbuild.customvariables.cacheTrees
									.pushTreeForClass(classId, function() {
										callback.apply(callbackScope,
												[ classAttributes ]);
									}, this);
						}, this);
			}
		};
		this.getClass = function(classId) {
			return this.data[classId];
		};
		this.getDescription = function(classId) {
			return (this.data[classId] && this.data[classId].description) ? this.data[classId].description
					: "";
		};
		this.getClasses = function() {
			var classes = [];
			for ( var key in this.data) {
				classes.push({
					_id : key,
					description : this.data[key].description
				});
			}
			return classes;
		};
		this.pushClassesRecursive = function(nodes, index, callback,
				callbackScope) {
			if (index >= nodes.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var node = nodes[index];
			this.getLoadingClass(node.data.classId, function() {
//				$.Cmdbuild.customvariables.cacheImages.pushClass(
//						node.data.classId, function() {
							this.pushClassesRecursive(nodes, index + 1,
									callback, callbackScope);
//						}, this);
			}, this);
		};
		this.pushClasses = function(elements, callback, callbackScope) {
			this.pushClassesRecursive(elements.nodes, 0, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
	};
	var cacheDomains = function() {
		this.data = [];
		this.pushClass = function(classId, callback, callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(classId,
					function() {
						this.getAllDomains(classId, function() {
							callback.apply(callbackScope, []);
						}, this);
					}, this);
		};
		this.getAllDomains = function(classId, callback, callbackScope) {
			var filter = this.getFilterForDomain(classId);
			var param = {
				filter : filter
			};
			$.Cmdbuild.utilities.proxy.getDomains(param, function(response) {
				this.getAllDomainsRecursive(response, callback, callbackScope);
			}, this);
		};
		this.getAllDomainsRecursive = function(domains, callback, callbackScope) {
			if (domains.length === 0) {
				callback.apply(callbackScope, []);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (this.getDomainIndex(domain._id) > -1) {
				this.getAllDomainsRecursive(domains, callback, callbackScope);
			} else {
				$.Cmdbuild.utilities.proxy.getDomain(domain._id, function(
						domainAttributes) {
					$.Cmdbuild.utilities.proxy.getDomainAttributes(domain._id,
							function(domainCustomAttributes) {
								this.loadExtremeClasses(
										domainAttributes.source,
										domainAttributes.destination,
										function() {
											this.pushDomain(domainAttributes,
													domainCustomAttributes);
											this.getAllDomainsRecursive(
													domains, callback,
													callbackScope);
										}, this);
							}, this);
				}, this);
			}
		};
		this.loadExtremeClasses = function(sourceId, destinationId, callback,
				callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(sourceId,
					function() {
						$.Cmdbuild.customvariables.cacheClasses
								.getLoadingClass(destinationId, function() {
									callback.apply(callbackScope, []);
								}, this);
					}, this);
		};
		this.pushDomain = function(domainAttributes, domainCustomAttributes) {
			var destinationDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(domainAttributes.destination);
			var sourceDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(domainAttributes.source);
			this.data.push({
				_id : domainAttributes._id,
				active : true,
				domainDescription : domainAttributes.description,
				destinationId : domainAttributes.destination,
				sourceId : domainAttributes.source,
				destinationDescription : destinationDescription,
				sourceDescription : sourceDescription,
				domainCustomAttributes : domainCustomAttributes,
				descriptionDirect : domainAttributes.descriptionDirect,
				descriptionInverse : domainAttributes.descriptionInverse
			});
		};
		this.getDomainIndex = function(domainId) {
			for (var i = 0; i < this.data.length; i++) {
				if (this.data[i]._id === domainId) {
					return i;
				}
			}
			return -1;
		};
		this.getDomain = function(domainId) {
			var index = this.getDomainIndex(domainId);
			return (index === -1) ? null : this.data[index];
		};
		this.getDescription = function(domainId) {
			var index = this.getDomainIndex(domainId);
			return (index === -1) ? "" : this.data[index].domainDescription;
		};
		this.setActive = function(domainId, active) {
			var domainIndex = this.getDomainIndex(domainId);
			if (domainIndex === -1) {
				console.log("ERROR ! cacheDomains setActive " + domainId);
			} else {
				this.data[domainIndex].active = active;
			}
		};
		this.getDomains4Class = function(classId) {
			var allDomains = [];
			do {
				var domains = this._getDomains4Class(classId);
				if (domains.length > 0) {
					allDomains = allDomains.concat(domains);
				}
				var classAttributes = $.Cmdbuild.customvariables.cacheClasses
						.getClass(classId);
				if (!(classAttributes && classAttributes.parent)) {
					return allDomains;
				}
				classId = classAttributes.parent;
			} while (true);
		};
		this._getDomains4Class = function(classId) {
			var domains = [];
			for (var i = 0; i < this.data.length; i++) {
				if (this.data[i].sourceId === classId
						|| this.data[i].destinationId === classId) {
					domains.push(this.data[i]);
				}
			}
			return domains;
		};
		this.getData = function() {
			return this.data;
		};
		this.getLoadingDomains4Class = function(classId, callback,
				callbackScope) {
			this.pushClass(classId, function() {
				callback.apply(callbackScope,
						[ this.getDomains4Class(classId) ]);
			}, this);
		};
		this.getFilterForDomain = function(classId) {
			var filter = {
				attribute : {
					or : [ {
						simple : {
							attribute : "source",
							operator : "contain",
							value : [ classId ]
						}
					}, {
						simple : {
							attribute : "destination",
							operator : "contain",
							value : [ classId ]
						}
					} ]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.cache = cache;
})(jQuery);