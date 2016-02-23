(function($) {
	var GraphCard = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.classId;
		this.data = [];
		this.metadata = {};

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			if (!this.type) {
				this.attributes = [];
				return;
			}
			if (this.type == $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				this.attributes = this.compoundAttributes();
				var node = $.Cmdbuild.customvariables.model
						.getNode(this.param.cardId);
				this.data = this.compoundData(node);
				setTimeout(function() {
					onObjectReady();
				}, 100);
			} else {
				$.Cmdbuild.utilities.proxy.getClassAttributes(this.type,
						this.loadAttributesCallback, this);
			}
		};
		this.getWidgets = function() {
			return [];
		};
		this.compoundAttributes = function() {
			var attributes = [{
				type: "string",
				name: "type",
				description: "Type",
				displayableInList: true
			}, {
				type: "string",
				name: "description",
				description: "Description",
				displayableInList: true
			}];
			return attributes;
		};
		this.compoundData = function(node) {
			var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
			var data = {
				type: "Compound node",
				description: label,
			};
			return data;
		};
		this.removeIfNotGridAttribute = function(attributes) {
			var ret = [];
			for (var i = 0; i < attributes.length; i++) {
				if (attributes[i].displayableInList) {
					ret.push(attributes[i]);
				}
			}
			return ret;
		};
		this.loadAttributesCallback = function(attributes) {
			this.originalAttributes = attributes.slice();
			if (this.param.displayableInList === "true") {
				attributes = this.removeIfNotGridAttribute(attributes);
			}
			if (this.param.withNotes != "true") {
				this.attributes = $.Cmdbuild.utilities.removeAttribute(
						attributes, "Notes");
			} else {
				this.attributes = attributes;
			}
			$.Cmdbuild.utilities.sortAttributes(attributes);
			$.Cmdbuild.utilities.changeAttributeType(this.attributes, "Notes",
					"text");
			this.loadData(this.param);
		};
		this.loadDataCallback = function(response, metadata) {
			this.data = response;
			this.metadata = metadata;
			onObjectReady();
		};
		this.loadData = function() {
			if (this.param.cardId) {
				$.Cmdbuild.utilities.proxy.getCardData(this.type,
						this.param.cardId, {}, this.loadDataCallback, this);
			} else {
				for (var i = 0; i < this.attributes.length; i++) {
					this.data[this.attributes[i]._id] = "";
				}
				this.metadata = {};
				onObjectReady();
			}
		};
		this.getAttributes = function() {
			return (this.attributes) ? this.attributes : [];
		};
		this.getData = function() {
			return (this.data) ? this.data : [];
		};
		this.getMetadata = function() {
			return this.metadata;
		};
		this.getOriginalAttributes = function() {
			return this.originalAttributes;
		};

		/**
		 * Private functions
		 */
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.GraphCard = GraphCard;
})(jQuery);
