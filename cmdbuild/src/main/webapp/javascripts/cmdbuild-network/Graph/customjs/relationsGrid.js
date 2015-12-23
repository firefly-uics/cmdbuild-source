(function($) {
	var relationsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		var working = true;
		console.log("creating", param);

		this.init = function(param) {
			console.log("init", param);
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			var cardId = $.Cmdbuild.dataModel.getValue("selectedForm", "id");
			console.log("cardId ==== " + cardId);
			this.param = param;
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this;
				this.grid.init(param);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.cardsGrid.init");
				throw e;
			}
		};
		this.show = function() {
			this.grid.show();
		};
	};
	$.Cmdbuild.custom.relationsGrid = relationsGrid;
}) (jQuery);
