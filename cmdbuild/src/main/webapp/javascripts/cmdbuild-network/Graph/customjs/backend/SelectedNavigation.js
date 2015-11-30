(function($) {
	var SelectedNavigation = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data =  [];
		this.metadata = {};
		this.param = param;
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.model = $.Cmdbuild.customvariables.selected; 
//			this.selected = $.Cmdbuild.customvariables.selected; 
			this.loadAttributes();
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
			var me = this;
			this.TM = setTimeout(function() { onObjectReady(); clearTimeout(me.TM); }, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = this.model.getCards(param.firstRow, param.nRows);
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
	$.Cmdbuild.custom.backend.SelectedNavigation = SelectedNavigation;
	
})(jQuery);
