(function($) {
	var classesGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.refreshSelected = function() {
			var form2Hook = $.Cmdbuild.dataModel.forms["classesForm"];
			form2Hook.selectRows($.Cmdbuild.custom.classesGrid.getAllSelected());
		};
		this.refresh = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			$.Cmdbuild.standard.grid.onNavigate(0, this.param.form);
		};
		this.getSelection = function() {
			return $.Cmdbuild.custom.classesGrid.getAllSelected();
		};
		this.init = function(param) {
			this.param = param;
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				param.parent = this;
				this.grid.init(param);
				$.Cmdbuild.customvariables.model.observe(this);
				$.Cmdbuild.customvariables.selected.observe(this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.classesGrid.init");
				throw e;
			}
		};
		this.show = function() {
			console.log("show");
			this.grid.show();
		};
	};
	$.Cmdbuild.custom.classesGrid = classesGrid;
	$.Cmdbuild.custom.classesGrid.getAllSelected =  function() {
		var classes = {};
		var nodes = $.Cmdbuild.customvariables.model.getNodes();
		for (var i = 0; i < nodes.length; i++) {
			var className = $.Cmdbuild.g3d.Model.getGraphData(nodes[i], "className");
			if (classes[className] === undefined || classes[className] === true) {
				classes[className] = $.Cmdbuild.customvariables.selected.isSelect(nodes[i].id());
			}
		}
		return classes;
	};
}) (jQuery);
