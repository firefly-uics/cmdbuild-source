(function($) {
	var CardsNavigation = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data =  [];
		this.metadata = {};
		this.param = param;
		this.filter = {};
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function(onlyRefresh) {
			this.onlyRefresh = onlyRefresh;
			this.model = $.Cmdbuild.customvariables.model; 
//			var data = this.model.getCards(0, 10);
//			this.total = data.total;
//			this.data = data.rows;
			if (onlyRefresh) {
				setTimeout(function() { onObjectReady(); }, 500);
			}
			else {
				this.loadAttributes();
			}
		};
		this.loadAttributes = function() {
			this.attributes = [
   				{
   					type: "string",
   					name: "label",
   					description: "Card",
   					displayableInList: true
   				},
   				{
   					type: "string",
   					name: "className",
   					description: "Class",
   					displayableInList: true
   				}
   			];
			setTimeout(function() { onObjectReady(); }, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = this.model.getCards(param.firstRow, param.nRows, this.filter);
			this.total = data.total;
			this.data = data.rows;
			callback.apply(callbackScope, this.data);
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
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
			return this.total;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.CardsNavigation = CardsNavigation;
	
})(jQuery);