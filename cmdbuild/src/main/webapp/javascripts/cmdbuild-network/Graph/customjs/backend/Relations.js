(function($) {
	var Relations = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.model = $.Cmdbuild.customvariables.selected;
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
		this.loadData = function(param, callback, callbackScope) {
			this.data = [];
			this.getAllDomains(param.classId, param.cardId, function(response) {
				this.rows = [];
				var nRows = parseInt(param.nRows);
				var firstRow = parseInt(param.firstRow);
				for (var i = firstRow; i < firstRow + nRows
						&& i < this.data.length; i++) {
					this.rows.push(this.data[i]);
				}
				this.populateRelationAttributes(this.rows, 0,
						function() {
							callback.apply(callbackScope, (this.rows)
									? this.rows
									: []);
						}, this);
			}, this);
		};
		this.getAllDomains = function(classId, cardId, callback, callbackScope) {
			var filter = this.getFilterForDomain(classId);
			var param = {
				filter: filter
			};
			$.Cmdbuild.utilities.proxy.getDomains(param, function(response) {
				this.getAllRelations(response, classId, parseInt(cardId),
						callback, callbackScope);
			}, this);
		};
		this.getAllRelations = function(domains, classId, cardId, callback,
				callbackScope) {
			if (domains.length == 0) {
				callback.apply(callbackScope, [this.data]);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			var domainId = domain._id;
			var domainDescription = domain.description;
			var filter = this.getFilterForRelation(cardId);
			var param = {
				filter: filter
			};
			$.Cmdbuild.utilities.proxy.getDomainAttributes(domainId, function(
					domainAttributes) {
				$.Cmdbuild.utilities.proxy
						.getRelations(domainId, param,
								function(response) {
									this.getAllRelationsCB(domains, classId,
											cardId, response, domainAttributes,
											domainDescription, callback,
											callbackScope);
								}, this);
			}, this);
		};
		this.getAllRelationsCB = function(domains, classId, cardId, relations,
				domainAttributes, domainDescription, callback, callbackScope) {
			if (relations.length <= 0) {
				this.getAllRelations(domains, classId, cardId, callback,
						callbackScope);
				return;
			}
			for (var i = 0; i < relations.length; i++) {
				var classId = (relations[i]._sourceId != cardId)
						? relations[i]._sourceType
						: relations[i]._destinationType;
				var classDescription = $.Cmdbuild.customvariables.cacheClasses.getDescription(classId);
				this.data.push({
					domainId: relations[i]._type,
					domainDescription: domainDescription,
					relationId: relations[i]._id,
					classId: classId,
					classDescription: classDescription,
					cardDescription: (relations[i]._sourceId != cardId)
							? relations[i]._sourceDescription
							: relations[i]._destinationDescription,
					domainAttributes: domainAttributes
				});
			}
			this.getAllRelations(domains, classId, cardId, callback,
					callbackScope);
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
	$.Cmdbuild.custom.backend.Relations = Relations;
})(jQuery);
