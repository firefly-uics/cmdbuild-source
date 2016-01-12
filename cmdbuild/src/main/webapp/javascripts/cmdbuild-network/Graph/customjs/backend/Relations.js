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
				name: "className",
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
			}, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			this.data = [];
			this.getAllDomains(param.className, param.cardId,
					function(response) {
						// this.total = data.total;
						// this.data = data.rows;
						this.rows = [];
						var nRows = parseInt(param.nRows);
						var firstRow = parseInt(param.firstRow);
						for (var i = firstRow; i < firstRow + nRows
								&& i < this.data.length; i++) {
							this.rows.push(this.data[i]);
						}
						this.populateRelationAttributes(this.rows, 0,
								function() {
									callback.apply(callbackScope, (this.rows) ? this.rows : []);
								}, this);
					}, this);
		};
		this.getAllDomains = function(className, cardId, callback,
				callbackScope) {
			var filter = this.getFilterForDomain(className);
			var param = {
				filter: filter
			};
			$.Cmdbuild.utilities.proxy.getDomains(param, function(response) {
				this.getAllRelations(response, className, parseInt(cardId),
						callback, callbackScope);
			}, this);
		};
		this.getAllRelations = function(domains, className, cardId, callback,
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
									this.getAllRelationsCB(domains, className,
											cardId, response, domainAttributes,
											domainDescription, callback,
											callbackScope);
								}, this);
			}, this);
		};
		this.getAllRelationsCB = function(domains, className, cardId,
				relations, domainAttributes, domainDescription, callback,
				callbackScope) {
			if (relations.length <= 0) {
				this.getAllRelations(domains, className, cardId, callback,
						callbackScope);
				return;
			}
			for (var i = 0; i < relations.length; i++) {
				this.data.push({
					domainId: relations[i]._type,
					domainDescription: domainDescription,
					relationId: relations[i]._id,
					className: (relations[i]._sourceId != cardId)
							? relations[i]._sourceType
							: relations[i]._destinationType,
					cardDescription: (relations[i]._sourceId != cardId)
							? relations[i]._sourceDescription
							: relations[i]._destinationDescription,
					domainAttributes: domainAttributes
				});
			}
			this.getAllRelations(domains, className, cardId, callback,
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
			if (domainAttribute.type == "lookup" && relationDetail[domainAttribute._id]) {
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
