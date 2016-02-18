(function($) {
	var cache = function() {
		$.Cmdbuild.customvariables.cacheDomains = new cacheDomains();
		$.Cmdbuild.customvariables.cacheClasses = new cacheClasses();
		$.Cmdbuild.customvariables.cacheImages = new cacheImages();
	};
	var cacheImages = function() {
		this.data = {};
		this.getBase64Image = function(img) {
		       canvas = document.createElement('canvas');
		        canvas.width = img.width;
		        canvas.height = img.height;
		        // Get '2d' context and draw the image.
		        ctx = canvas.getContext("2d");
		        ctx.drawImage(img, 0, 0);
		        // Get canvas data URL
		        try{
		            data = canvas.toDataURL();
		            return data;
		        }catch(e){
		            error(e);
		        }
		};
		this.pushClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, []);
				return;
			}
			var token = $.Cmdbuild.authentication.getAuthenticationToken();
			var url =  $.Cmdbuild.SpriteArchive.class2Sprite(classId);
		    var img = $('<img/>');
		    var me = this;
		    img[0].onload = function() {
		    	me.data[classId] = me.getBase64Image(img[0]);
				callback.apply(callbackScope, []);
		    };
		    img.attr('src', url);
		};
		this.pushClass_DEFINITIVE_FUNCTION = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, []);
				return;
			}
			var token = $.Cmdbuild.authentication.getAuthenticationToken();
			var url = $.Cmdbuild.global.getApiUrl()
					+ "classes/InternalEmployee/cards/6083/attachments/b2JqZWN0LXJvdGF0ZS0yLnBuZw/object-rotate-2.png?CMDBuild-Authorization="
					+ token;
		    var img = $('<img/>');
		    var me = this;
		    img[0].onload = function() {
		    	me.data[classId] = me.getBase64Image(img[0]);
				callback.apply(callbackScope, []);
		    };
		    img.attr('src', url);
		};
		this.pushElementsRecursive = function(nodes, index, callback, callbackScope) {
			if (index >= nodes.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var node = nodes[index];
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(node.data.classId, function() {
				this.pushClass(node.data.classId, function() {
					this.pushElementsRecursive(nodes, index + 1, callback, callbackScope);				
				}, this);
			}, this);
		};
		this.pushElements = function(elements, callback, callbackScope) {
			this.pushElementsRecursive(elements.nodes, 0, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
		this.getImage = function(classId) {
			return this.data[classId];
		};
	};
	var cacheClasses = function() {
		this.data = {};
		this.getLoadingClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, [this.data[classId]]);
			} else {
				$.Cmdbuild.utilities.proxy.getClass(classId, function(classAttributes) {
					this.data[classId] = classAttributes;
					callback.apply(callbackScope, [classAttributes]);
				}, this);
			}
		};
		this.getClass = function(classId) {
			return this.data[classId];
		};
		this.getDescription = function(classId) {
			return this.data[classId].description;
		};
		this.getClasses = function() {
			var classes = [];
			for (var key in this.data) {
				classes.push({
					_id: key,
					description: this.data[key].description
				});
			}
			return classes;
		};
	};
	var cacheDomains = function() {
		this.data = [];
		this.pushClass = function(classId, callback, callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(classId, function() {
				this.getAllDomains(classId, function() {
					callback.apply(callbackScope, []);
				}, this);
			}, this);
		};
		this.getAllDomains = function(classId, callback, callbackScope) {
			var filter = this.getFilterForDomain(classId);
			var param = {
				filter: filter
			};
			$.Cmdbuild.utilities.proxy.getDomains(param, function(response) {
				this.getAllDomainsRecursive(response, callback, callbackScope);
			}, this);
		};
		this.getAllDomainsRecursive = function(domains, callback, callbackScope) {
			if (domains.length == 0) {
				callback.apply(callbackScope, []);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (this.getDomainIndex(domain._id) > -1) {
				this.getAllDomainsRecursive(domains, callback,
						callbackScope);
			} else {
				$.Cmdbuild.utilities.proxy.getDomain(domain._id, function(
						domainAttributes) {
					this.loadExtremeClasses(domainAttributes.source, domainAttributes.destination, function() {
						this.pushDomain(domainAttributes);
						this.getAllDomainsRecursive(domains, callback,
								callbackScope);
					}, this);
				}, this);
			}
		};
		this.loadExtremeClasses = function(sourceId, destinationId, callback, callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(sourceId, function() {
				$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(destinationId, function() {
					callback.apply(callbackScope, []);
				}, this);
			}, this);
		};
		this.pushDomain = function(domainAttributes) {
			var destinationDescription = $.Cmdbuild.customvariables.cacheClasses.getDescription(domainAttributes.destination);
			var sourceDescription = $.Cmdbuild.customvariables.cacheClasses.getDescription(domainAttributes.source);
			this.data.push({
				_id: domainAttributes._id,
				active: true,
				domainDescription: domainAttributes.description,
				destinationId: domainAttributes.destination,
				sourceId: domainAttributes.source,
				destinationDescription: destinationDescription,
				sourceDescription: sourceDescription
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
				var classAttributes = $.Cmdbuild.customvariables.cacheClasses.getClass(classId);
				if (! (classAttributes && classAttributes.parent)) {
					return allDomains;
				}
				classId = classAttributes.parent;
			} while (true);
		};
		this._getDomains4Class = function(classId) {
			var domains = [];
			for (var i = 0; i < this.data.length; i++) {
				if (this.data[i].sourceId === classId || this.data[i].destinationId === classId) {
					domains.push(this.data[i]);
				}
			}
			return domains;
		};
		this.getData = function() {
			return this.data;
		};
		this.getLoadingDomains4Class = function(classId, callback, callbackScope) {
			this.pushClass(classId, function() {
				callback.apply(callbackScope, [this.getDomains4Class(classId)]);
			}, this);
		};
		this.getFilterForDomain = function(classId) {
			var filter = {
				attribute: {
					or: [{
						simple: {
							attribute: "source",
							operator: "contain",
							value: [classId]
						}
					}, {
						simple: {
							attribute: "destination",
							operator: "contain",
							value: [classId]
						}
					}]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.cache = cache;
})(jQuery);