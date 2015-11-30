(function($) {
	var Options = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.type;
		this.data = [];
		this.metadata = {};
		var levels = [{
				_id: "1",
				description: "1"
			},{
				_id: "2",
				description: "2"
			},{
				_id: "3",
				description: "3"
			},{
				_id: "4",
				description: "4"
			},{
				_id: "5",
				description: "5"
			}
		];
	
		var layouts = [{
				_id: "Hierarchical",
				description: "Hierarchical"
			},{
				_id: "Attractive",
				description: "Attractive"
			}
		];
	
		var projections = [{
				_id: "Projection",
				description: "Projection"
			},{
				_id: "Orthogonal",
				description: "Orthogonal"
			}
		];

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadData(this.param);
		};
		this.loadData = function(response, metadata) {
			switch (this.type) {
				case "projectionType" :
					this.data = projections;
					break;
				case "layoutType" :
					this.data = layouts;
					break;
				case "explosionLevels" :
					this.data = levels;
					break;
			}
			setTimeout(function() { onObjectReady(); }, 10);
			

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
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.Options = Options;
})(jQuery);