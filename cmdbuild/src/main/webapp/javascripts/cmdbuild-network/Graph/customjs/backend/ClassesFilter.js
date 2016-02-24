(function($) {
	var NO_OPEN_NODE = "__NoOpenNode";
	var ClassesFilter = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data = [];
		this.metadata = {};
		this.filter = {};
		this.param = param;
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;
		var backend = this;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.model = $.Cmdbuild.customvariables.model;
			var data = this.model.getDistinctClasses(0, 10);
			this.total = data.total;
			this.data = data.rows;
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [{
				type: "string",
				name: "_id",
				description: "_id",
				displayableInList: false
			}, {
				type: "string",
				name: "classDescription",
				description: "Class",
				displayableInList: true
			}, {
				type: "string",
				name: "classId",
				description: "Class",
				displayableInList: false
			}];
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = this.model.getDistinctClasses(0, 10);
			for (var i = 0; i < data.rows.length; i++) {
				data.rows[i]._id = data.rows[i].classId;
				data.rows[i].id = data.rows[i].classId;
			}
			this.total = data.total;
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			if (filterClasses) {
				for (var i = 0; i < filterClasses.length; i++) {
					data.rows.push({
						_id: filterClasses[i],
						id: filterClasses[i],
						classId: filterClasses[i]
					});
				}
				this.total += i;
			}
			data.rows.sort(function(a, b) {
				return (a.classId > b.classId) ? 1  : -1;
			});
			this.data = [];
			param.nRows = parseInt(param.nRows);
			for (var i = param.firstRow; i < param.nRows + param.firstRow && i < data.rows.length; i++) {
				this.data.push(data.rows[i]);
			}
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
			onReadyFunction.apply(onReadyScope, [backend]);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			return this.total;
		};
		this.getNodesByClassName = function(classId) {
			return this.model.getNodesByClassName(classId);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.ClassesFilter = ClassesFilter;

})(jQuery);
