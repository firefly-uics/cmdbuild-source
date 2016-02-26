(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.backend) {
		$.Cmdbuild.g3d.backend = {};
	}
	var elements = {
		nodes : [ {
			data : {
				classId : '',
				id : '',
				label : '',
				color : "#ff0000",
				faveShape : 'triangle',
				position : {
					x : 0,
					y : 0,
					z : 0
				},
				rotation : {
					x : 0,
					y : 0,
					z : 0
				},
				scale : {
					x : 1,
					y : 1,
					z : 1
				}
			}
		} ],

		edges : []
	};
	var CmdbuildModel = function() {
		this.setModel = function(model) {
			this.model = model;
		};
		this.getInitModel = function(params, callback, callbackScope) {
			if (params) {
				$.Cmdbuild.g3d.proxy.getCardData(params.classId, params.cardId,
						{}, function(card) {
							elements.nodes[0].data.label = card.Description;
							elements.nodes[0].data.classId = params.classId;
							elements.nodes[0].data.id = params.cardId;
							callback.apply(callbackScope, [ elements ]);
						}, this);
			} else {
				callback.apply(callbackScope, []);
			}
		};
		this.chargeModel = function(elements, domain, relation, sourceId,
				targetId, targetDescription, targetClassName, compoundData,
				parentNode, isNew) {
			sourceId = "" + sourceId;
			targetId = "" + targetId;
			if (isNew) {
				var data = {
					classId : targetClassName,
					id : targetId,
					label : targetDescription,
					color : "#ff0000",
					faveShape : 'triangle',
					domain : (domain) ? domain._id : "--",
					position : {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : 200
					},
					compoundData : compoundData,
					previousPathNode : sourceId
				};
				var node = {
					data : data
				};

				elements.nodes.push(node);
			}
			var edgeId = sourceId + domain._id + targetId;
			var edge = {
				id : edgeId,
				source : sourceId,
				target : targetId,
				relationId : relation._id,
				domainId : domain._id,
				label : (domain) ? domain.domainDescription : "--",
				color : $.Cmdbuild.custom.configuration.edgeColor,
				strength : 90
			};
			elements.edges.push({
				data : edge
			});
			var newNode = this.model.getNode(targetId);
			return newNode;
		};
		this.getANodesBunch = function(id, domainList, callback, callbackScope) {
			var node = this.model.getNode(id);
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			this.getAllDomains(node, classId, id, domainList, callback,
					callbackScope);
		};
		this.filteredDomains = function(domainList, classId) {
			if (!domainList) {
				return null;
			}
			var domains = $.Cmdbuild.customvariables.cacheDomains
					.getDomains4Class(classId);
			var ret = [];
			for (var i = 0; i < domains.length; i++) {
				if (domains[i].active) {
					ret.push(domains[i]);
				}
			}
			return ret;
		};
		this.getAllDomains = function(node, classId, cardId, domainList,
				callback, callbackScope) {
			var elements = {
				nodes : [],
				edges : []
			};
			var configuration = $.Cmdbuild.custom.configuration;
			var filteredDomains = this.filteredDomains(
					configuration.filterClassesDomains, classId);
			if (filteredDomains) {
				this.getAllRelations(node, filteredDomains, domainList,
						classId, parseInt(cardId), elements, callback,
						callbackScope);
			} else {
				var filter = this.getFilterForDomain(classId);
				var param = {
					filter : filter
				};
				$.Cmdbuild.customvariables.cacheDomains
						.getLoadingDomains4Class(classId, function(response) {
							this.getAllRelations(node, response, domainList,
									classId, parseInt(cardId), elements,
									callback, callbackScope);
						}, this);
			}
		};
		this.pushAnOpeningChild = function(elements, domain, relation, id,
				description, classId, data, node, parentId, children) {
			var cyNode = this.model.getNode(id);
			if (cyNode.length === 0) {
				children.push(id);
			}
			this.chargeModel(elements, domain, relation, parentId, id,
					description, classId, data, node, cyNode.length === 0);
		};
		this.getAllRelations = function(node, domains, domainList, classId,
				cardId, elements, callback, callbackScope) {
			if (! domains || domains.length === 0) {
				callback.apply(callbackScope, [ elements ]);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (domainList) {
				var arDomains = domainList.filter(function(value) {
					return (value.domainId == domain._id);
				});
				if (arDomains.length <= 0) {
					this.getAllRelations(node, domains, domainList, classId,
							cardId, elements, callback, callbackScope);
					return;
				}
			}
			var domainId = domain._id;
			var children = [];
			var filter = this.getFilterForRelation(cardId);
			var param = {
				filter : filter,
				start : 0,
				limit : $.Cmdbuild.customvariables.options.clusteringThreshold
			};
			$.Cmdbuild.utilities.proxy.getRelations(domainId, param, function(
					relations, metadata) {
				if (relations.length <= 0) {
					this.getAllRelations(node, domains, domainList, classId,
							cardId, elements, callback, callbackScope);
					return;
				}
				if (this.isCompound(relations)) {
					var compoundData = {
						domainId : domainId,
						filter : filter
					};
					this.pushCompound(relations[0], compoundData,
							metadata.total, classId, elements, domain, node,
							cardId, children);
				} else {
					this.explodeChildren(elements, domain, node, classId,
							cardId, children, relations);
				}
				node.data.children = children;
				this.getAllRelations(node, domains, domainList, classId,
						cardId, elements, callback, callbackScope);
			}, this);
		};
		this.pushCompound = function(relationSample, compoundData, total,
				classId, elements, domain, node, cardId, children) {
			var rDescription = (relationSample._sourceId == cardId && relationSample._sourceType == classId) ? relationSample._type
					+ "(1)"
					: relationSample._type + "(2)";
			var description = "compound node of: " + total + " "
					+ relationSample._destinationType + " - " + rDescription;
			var id = "CN" + relationSample._type + relationSample._sourceId
					+ relationSample._destinationId;
			this.pushAnOpeningChild(elements, domain, relationSample, id,
					description, $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE,
					compoundData, node, cardId, children);
		};
		this.isCompound = function(relations) {
			var clusteringThreshold = $.Cmdbuild.customvariables.options.clusteringThreshold;
			return (relations.length >= clusteringThreshold);
		};
		this.openCompoundNode = function(id, data, domain, callback,
				callbackScope) {
			var node = this.model.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
					"previousPathNode");
			var parentNode = this.model.getNode(parentId);
			var children = [];
			var elements = {
				nodes : [],
				edges : []
			};
			var classId = $.Cmdbuild.g3d.Model.getGraphData(parentNode,
					"classId");
			this.explodeChildren(elements, domain, parentNode, classId,
					parentId, children, data);
			callback.apply(callbackScope, [ elements ]);
		};
		this.explodeChildren = function(elements, domain, node, classId,
				cardId, children, relations) {
			var destinationId;
			var destinationDescription;
			var destinationType;
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			for (var i = 0; i < relations.length; i++) {
				var relation = relations[i];
				if (filterClasses
						&& (filterClasses.indexOf(relation._destinationType) != -1 || filterClasses
								.indexOf(relation._sourceType) != -1)) {
					continue;

				}
				if (relation._sourceId == cardId
						&& relation._sourceType == classId) {
					destinationId = relation._destinationId;
					destinationDescription = relation._destinationDescription;
					destinationType = relation._destinationType;
				} else {
					destinationId = relation._sourceId;
					destinationDescription = relation._sourceDescription;
					destinationType = relation._sourceType;
				}
				this.pushAnOpeningChild(elements, domain, relation,
						destinationId, destinationDescription, destinationType,
						{}, node, cardId, children);
			}
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
		this.getFilterForRelation = function(cardId) {
			var filter = {
				attribute : {
					or : [ {
						simple : {
							attribute : "_sourceId",
							operator : "in",
							value : [ cardId ]
						}
					}, {
						simple : {
							attribute : "_destinationId",
							operator : "in",
							value : [ cardId ]
						}
					} ]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel = CmdbuildModel;

})(jQuery);