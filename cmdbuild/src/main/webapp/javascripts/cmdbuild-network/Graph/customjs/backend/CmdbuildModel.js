(function($) {
	var cache = {
		domains: {}
	};
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.backend) {
		$.Cmdbuild.g3d.backend = {};
	}
	var elements = {
		nodes: [{
			data: {
				className: '',
				id: '',
				label: '',
				color: "#ff0000",
				faveShape: 'triangle',
				position: {
					x: 0,
					y: 0,
					z: 0
				},
				rotation: {
					x: 0,
					y: 0,
					z: 0
				},
				scale: {
					x: 1,
					y: 1,
					z: 1
				}
			}
		}],

		edges: []
	};
	var CmdbuildModel = function() {
		this.setModel = function(model) {
			this.model = model;
		};
		this.getInitModel = function(params, callback, callbackScope) {
			if (params) {
				$.Cmdbuild.utilities.proxy.getCardData(params.classId,
						params.cardId, {}, function(card) {
							elements.nodes[0].data.label = card.Description;
							elements.nodes[0].data.className = params.classId;
							elements.nodes[0].data.id = params.cardId;
							callback.apply(callbackScope, [elements]);
						}, this);
			} else {
				callback.apply(callbackScope, []);
			}
		};
		this.chargeModel = function(elements, domain, sourceId, targetId,
				targetDescription, targetClassName, compoundData, parentNode,
				isNew) {
			sourceId = "" + sourceId;
			targetId = "" + targetId;
			if (isNew) {
				var data = {
					className: targetClassName,
					id: targetId,
					label: targetDescription,
					color: "#ff0000",
					faveShape: 'triangle',
					position: {
						x: Math.random() * 1000 - 500,
						y: Math.random() * 600 - 300,
						z: 200
					// Math.random() * 800 - 400
					},
					compoundData: compoundData,
					previousPathNode: sourceId
				};
				var node = {
					data: data
				};

				elements.nodes.push(node);
			}
			var edge = {
				source: sourceId,
				target: targetId,
				label: (domain) ? domain.description : "--",
				color: $.Cmdbuild.custom.configuration.edgeColor,
				strength: 90
			};
			elements.edges.push({
				data: edge
			});
			var newNode = this.model.getNode(targetId);
			return newNode;
		};
		this.getANodesBunch = function(id, domainList, callback, callbackScope) {
			var node = this.model.getNode(id);
			var className = $.Cmdbuild.g3d.Model
					.getGraphData(node, "className");
			this.getAllDomains(node, className, id, domainList, callback,
					callbackScope);
			return elements;
		};
		this.getAllDomains = function(node, className, cardId, domainList,
				callback, callbackScope) {
			var elements = {
				nodes: [],
				edges: []
			};
			var configuration = $.Cmdbuild.custom.configuration;
			if (configuration.filterClassesDomains && configuration.filterClassesDomains[className]) {
				this.getAllRelations(node,
						configuration.filterClassesDomains[className],
						domainList, className, parseInt(cardId), elements,
						callback, callbackScope);
			} else if (cache.domains[className] && false) {
				this.getAllRelations(node, cache.domains[className],
						domainList, className, parseInt(cardId), elements,
						callback, callbackScope);

			} else {
				var filter = this.getFilterForDomain(className);
				var param = {
					filter: filter
				};
				$.Cmdbuild.utilities.proxy.getDomains(param,
						function(response) {
							cache.domains[className] = response.slice();
							this.getAllRelations(node, response, domainList,
									className, parseInt(cardId), elements,
									callback, callbackScope);
						}, this);
			}
		};
		this.pushAnOpeningChild = function(elements, domain, id, description,
				className, data, node, parentId, children) {
			var cyNode = this.model.getNode(id);
			if (cyNode.length == 0) {
				children.push(id);
			}
			this.chargeModel(elements, domain, parentId, id, description,
					className, data, node, cyNode.length == 0);
		};
		this.getAllRelations = function(node, domains, domainList, className,
				cardId, elements, callback, callbackScope) {
			if (domains.length == 0) {
				callback.apply(callbackScope, [elements]);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (domainList != null) {
				var arDomains = domainList.filter(function(value) {
					return (value.domainId == domain._id);
				});
				if (arDomains.length <= 0) {
					this.getAllRelations(node, domains, domainList, className,
							cardId, elements, callback, callbackScope);
					return;
				}
			}
			var domainId = domain._id;
			var children = [];
			var filter = this.getFilterForRelation(cardId);
			var param = {
				filter: filter
			};
			$.Cmdbuild.utilities.proxy
					.getRelations(
							domainId,
							param,
							function(response) {
								if (response.length <= 0) {
									this.getAllRelations(node, domains,
											domainList, className, cardId,
											elements, callback, callbackScope);
									return;
								}
								var relation = response[0];
								var configuration = $.Cmdbuild.custom.configuration;
								if (configuration.filterClasses
										&& (configuration.filterClasses
												.indexOf(relation._destinationType) != -1 || configuration.filterClasses
												.indexOf(relation._sourceType) != -1)) {
									this.getAllRelations(node, domains,
											domainList, className, cardId,
											elements, callback, callbackScope);
									return;

								}
								var clusteringThreshold = $.Cmdbuild.customvariables.options["clusteringThreshold"];
								if (response.length > clusteringThreshold) {
									var relation = response[0];
									var rDescription = (relation._sourceId == cardId && relation._sourceType == className)
											? relation._type + "(1)"
											: relation._type + "(2)";
									var description = "compound node of: "
											+ response.length + " "
											+ relation._destinationType + " - "
											+ rDescription;
									this
											.pushAnOpeningChild(
													elements,
													domain,
													relation._destinationId,
													description,
													$.Cmdbuild.g3d.constants.GUICOMPOUNDNODE,
													response, node, cardId,
													children);
								} else {
									this.explodeChildren(elements, domain,
											node, className, cardId, children,
											response);
								}
								// $.Cmdbuild.g3d.Model.setGraphData(node,
								// "children", children);
								node.data.children = children;
								this.getAllRelations(node, domains, domainList,
										className, cardId, elements, callback,
										callbackScope);
							}, this);
		};
		this.openChildren = function(id, data, callback, callbackScope) {
			var node = this.model.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
					"previousPathNode");
			var parentNode = this.model.getNode(parentId);
			var children = [];
			var elements = {
				nodes: [],
				edges: []
			};
			var className = $.Cmdbuild.g3d.Model.getGraphData(parentNode,
					"className");
			this.explodeChildren(elements, null, parentNode, className,
					parentId, children, data);
			callback.apply(callbackScope, [elements]);
		};
		this.explodeChildren = function(elements, domain, node, className,
				cardId, children, response) {
			var destinationId = undefined;
			var destinationDescription = undefined;
			var destinationType = undefined;
			for (var i = 0; i < response.length; i++) {
				var relation = response[i];
				if (relation._sourceId == cardId
						&& relation._sourceType == className) {
					destinationId = relation._destinationId;
					destinationDescription = relation._destinationDescription;
					destinationType = relation._destinationType;
				} else {
					destinationId = relation._sourceId;
					destinationDescription = relation._sourceDescription;
					destinationType = relation._sourceType;
				}
				this.pushAnOpeningChild(elements, domain, destinationId,
						destinationDescription, destinationType, {}, node,
						cardId, children);
			}
		};
		this.getFilterForDomain = function(className) {
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
			return filter;
		};
		this.getFilterForRelation = function(cardId) {
			var filter = {
				attribute: {
					or: [{
						simple: {
							attribute: "_sourceId",
							operator: "in",
							value: [cardId]
						}
					}, {
						simple: {
							attribute: "_destinationId",
							operator: "in",
							value: [cardId]
						}
					}]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel = CmdbuildModel;

})(jQuery);