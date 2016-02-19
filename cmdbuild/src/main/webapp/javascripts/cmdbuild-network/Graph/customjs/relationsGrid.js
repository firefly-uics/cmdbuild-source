(function($) {
	var oldId = -1;
	var relationsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.init = function(param) {
			if (this.loading === true) {
				this.buffer = param;
				return;
			}
			this.buffer = null;
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			var cardId = $.Cmdbuild.dataModel.getValue("selectedForm", "id");
			if (oldId == cardId) {
				return;
			}
			oldId = cardId;
			param.parent = this;
			this.param = param;
			try {
				$.Cmdbuild.dataModel.forms[this.param.form] = this;
				this.loading = true;
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
		this.onLoad = function() {
			this.loading = false;
			if (this.buffer !== null) {
				this.init(this.buffer);
			}
		};
	};
	$.Cmdbuild.custom.relationsGrid = relationsGrid;
}) (jQuery);
