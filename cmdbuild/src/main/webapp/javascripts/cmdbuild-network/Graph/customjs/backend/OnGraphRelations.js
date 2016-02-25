(function($) {
	var OnGraphRelations = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.model = $.Cmdbuild.customvariables.model;
		this.attributes = [];
		this.data = [];
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [{
				type: "string",
				name: "domainDescription",
				description: "Domain",
				displayableInList: true
			}, {
				type: "string",
				name: "classId",
				description: "Class",
				displayableInList: false
			}, {
				type: "string",
				name: "classDescription",
				description: "Class",
				displayableInList: true
			}, {
				type: "string",
				name: "cardDescription",
				description: "Card",
				displayableInList: true
			}, {
				type: "string",
				name: "attributes",
				description: "Attributes",
				displayableInList: true
			}];
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.cyCollection2Array = function(collection) {
			var array = [];
			for (var i = 0; i < collection.length; i++) {
				var element = collection[i];
				var domainId = $.Cmdbuild.g3d.Model.getGraphData(element,
						"domainId");
				var domainDescription = $.Cmdbuild.g3d.Model.getGraphData(
						element, "label");
				var relationId = $.Cmdbuild.g3d.Model.getGraphData(element,
						"relationId");
				array.push({
					domainId: domainId,
					domainDescription: domainDescription,
					relationId: relationId
				});
			}
			return array;
		};
		this.loadData = function(param, callback, callbackScope) {
			this.data = [];
			var me = this;
			setTimeout(function() {
				var edgesCollection = $.Cmdbuild.customvariables.model
						.connectedEdges(param.cardId);
				var edges = me.cyCollection2Array(edgesCollection);
				me.getEdges(param.cardId, edges, function(response) {
					me.rows = [];
					var nRows = parseInt(param.nRows);
					var firstRow = parseInt(param.firstRow);
					for (var i = firstRow; i < firstRow + nRows
							&& i < response.length; i++) {
						me.rows.push(response[i]);
					}
					me.populateRelationAttributes(me.rows, 0,
							function() {
								callback.apply(callbackScope, (me.rows)
										? me.rows
										: []);
							}, me);
				}, me);
			}, 100);
		};
		this.getEdges = function(cardId, edges, callback, callbackScope) {
			if (edges.length == 0) {
				callback.apply(callbackScope, [this.data]);
				return;
			}
			var edge = edges[0];
			edges.splice(0, 1);
			var domainId = edge.domainId;
			var domainDescription = edge.domainDescription;
			var relationId = edge.relationId;
			var domainAttributes = $.Cmdbuild.customvariables.cacheDomains.getDomain(domainId);
			$.Cmdbuild.utilities.proxy.getRelation(domainId, relationId,
					{}, function(response) {
						this.getRelationCB(cardId, response,
								domainAttributes.domainCustomAttributes, domainDescription,
								function() {
									this.getEdges(cardId, edges, callback,
											callbackScope);
								}, this);
					}, this);
		
		};
		this.getRelationCB = function(cardId, relation, domainAttributes,
				domainDescription, callback, callbackScope) {
			var classId = (relation._sourceId != cardId)
					? relation._sourceType
					: relation._destinationType;
			var classDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(classId);
			this.data.push({
				domainId: relation._type,
				domainDescription: domainDescription,
				relationId: relation._id,
				classId: classId,
				classDescription: classDescription,
				cardDescription: (relation._sourceId != cardId)
						? relation._sourceDescription
						: relation._destinationDescription,
				domainAttributes: domainAttributes
			});
			callback.apply(callbackScope, [this.data]);
		};
		this.populateRelationAttributes = function(rows, index, callback,
				callbackScope) {
			if (index >= rows.length) {
				callback.apply(callbackScope, rows);
				return;
			}
			var row = rows[index];
			this.getRelationDetail(row, function() {
				this.populateRelationAttributes(rows, ++index, callback,
						callbackScope);
			}, this);
		};
		this.getRelationDetail = function(row, callback, callbackScope) {
			if (row.domainAttributes.length <= 0) {
				callback.apply(callbackScope, [row]);
				return;
			}
			var attributesStrings = {
				strHeaders: "",
				strValues: ""
			};
			$.Cmdbuild.utilities.proxy
					.getRelation(
							row.domainId,
							row.relationId,
							{},
							function(relationDetail) {
								this
										.getDomainAttributesValues(
												row.domainAttributes,
												0,
												attributesStrings,
												relationDetail,
												function() {
													row.attributes = "<table class='relationAttributes'>";
													row.attributes += "<tr>"
															+ attributesStrings.strHeaders
															+ "</tr>";
													row.attributes += "<tr>"
															+ attributesStrings.strValues
															+ "</tr>";
													row.attributes += "</table>";
													callback.apply(
															callbackScope,
															[row]);
												}, this);
							}, this);
		};
		this.getDomainAttributesValues = function(domainAttributes, index,
				attributesStrings, relationDetail, callback, callbackScope) {
			if (index >= domainAttributes.length) {
				callback.apply(callbackScope, domainAttributes);
				return;
			}
			var domainAttribute = domainAttributes[index];
			this.singleDomainAttributeValue(domainAttribute, attributesStrings,
					relationDetail, function() {
						this.getDomainAttributesValues(domainAttributes,
								++index, attributesStrings, relationDetail,
								callback, callbackScope);
					}, this);
		};
		this.singleDomainAttributeValue = function(domainAttribute,
				attributesStrings, relationDetail, callback, callbackScope) {
			attributesStrings.strHeaders += "<th>"
					+ domainAttribute.description + "</th>";
			if (domainAttribute.type == "lookup"
					&& relationDetail[domainAttribute._id]) {
				$.Cmdbuild.utilities.proxy.getLookupValue(domainAttribute.type,
						relationDetail[domainAttribute._id], {}, function(
								response) {
							var description = response.description;
							attributesStrings.strValues += "<td>" + description
									+ "</td>";
							callback.apply(callbackScope, []);

						}, this);
			} else {
				var description = (relationDetail[domainAttribute._id])
						? relationDetail[domainAttribute._id]
						: "";
				attributesStrings.strValues += "<td>" + description + "</td>";
				callback.apply(callbackScope, []);
			}
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.rows;
		};
		this.getMetadata = function() {
			return this.metadata;
		};

		/**
		 * Private functions
		 */
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			var metadata = this.getMetadata();
			return metadata && metadata.total
					? metadata.total
					: this.data.length;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.OnGraphRelations = OnGraphRelations;
})(jQuery);
