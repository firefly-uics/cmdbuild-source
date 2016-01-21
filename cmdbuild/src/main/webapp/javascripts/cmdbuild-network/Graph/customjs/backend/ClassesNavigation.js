(function($) {
	var NO_OPEN_NODE = "__NoOpenNode";
	var ClassesNavigation = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data = [];
		this.metadata = {};
		this.param = param;
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;
		var backend = this;

		/**
		 * Base functions
		 */
		this.init = function() {
			var me = this;
			if (! $.Cmdbuild.customvariables.model) {
				setTimeout(function() {
					me.init();
				
				}, 100);
				return;
			}
			this._init();
		}
		this._init = function() {
			this.model = $.Cmdbuild.customvariables.model;
			var data = this.model.getDistinctClasses(0, 10);
			this.total = data.total;
			this.data = data.rows;
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [{
				type: "string",
				name: "className",
				description: "Class",
				displayableInList: true
			}, {
				type: "integer",
				name: "qt",
				description: "Qt",
				displayableInList: true
			}];
			setTimeout(function() {
				onObjectReady();
			}, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			callback.apply(callbackScope, this.data);
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
		};
		this.getRelation4Class = function(path, className, returnData,
				callback, callbackScope) {
			var filter = {
				attribute: {
					or: [{
						simple: {
							attribute: "source",
							operator: "contain",
							value: [className]
						}
					}, {
						simple: {
							attribute: "destination",
							operator: "contain",
							value: [className]
						}
					}]
				}
			};
			var param = {
				filter: filter
			};
			this.callbackRelations = callback;
			this.callbackRelationsScope = callbackScope;
			this.domainsWithData = [];
			this.sourceRelations = className;
			this.returnData = returnData;
			$.Cmdbuild.utilities.proxy.getDomains(param, function(response) {
				this.getSingleDomains(path, response);
			}, this);
		};
		this.isTheSame = function(d, domainId, source, destination, bDirect) {
			return d._id == domainId
					&& ((bDirect && (d.source == source && d.destination == destination)) || ((!bDirect) && (d.source == destination && d.destination == source)));
		};
		this.inPathToRoot = function(path, domain, bDirect) {
			for (var i = 0; i < path.length; i++) {
				var d = path[i];
				if (this.isTheSame(d, domain._id, domain.source,
						domain.destination, bDirect)) {
					return true;
				}
			}
			return false;
		};
		this.getSingleDomains = function(path, domains) {
			if (domains.length == 0) {
				this.callbackRelations.apply(this.callbackRelationsScope, [
						this.returnData, this.domainsWithData]);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			$.Cmdbuild.utilities.proxy.getDomain(domain._id, function(response) {
				this.chargeNode(path, domains, response);
			}, this);
		};
		this.getMetadata = function() {
			return this.metadata;
		};

		this.getChildrenByClassName = function(node, className) {
			return this.model.getChildrenByClassName(node, className);
		};

		this.chargeNode = function(path, domains, response) {
			var obj = (response.source == this.sourceRelations) ? {
				className: response.destination,
				label: response.descriptionDirect,
				_id: response._id,
				source: response.source,
				destination: response.destination
			} : {
				className: response.source,
				label: response.descriptionInverse,
				_id: response._id,
				source: response.destination,
				destination: response.source
			};
			if (this.inPathToRoot(path, response,
					response.source == this.sourceRelations)) {
				obj.source = NO_OPEN_NODE;
				obj.destination = NO_OPEN_NODE;
			}
			this.domainsWithData.push(obj);
			this.getSingleDomains(path, domains);
		};
		/**
		 * Private functions
		 */
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope, [backend]);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			return this.total;
		};
		this.getNodesByClassName = function(className) {
			return this.model.getNodesByClassName(className);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.ClassesNavigation = ClassesNavigation;

})(jQuery);
