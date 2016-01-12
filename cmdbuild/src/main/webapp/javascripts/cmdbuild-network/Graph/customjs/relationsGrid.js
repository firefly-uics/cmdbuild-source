(function($) {
	var oldId = -1;
	var relationsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.init = function(param) {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			var cardId = $.Cmdbuild.dataModel.getValue("selectedForm", "id");
			if (oldId == cardId) {
				return;
			}
			oldId = cardId;
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
