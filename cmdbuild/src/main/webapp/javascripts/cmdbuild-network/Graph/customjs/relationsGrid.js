(function($) {
	var oldId = -1;
	var relationsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		var working = true;
		this.init = function(param) {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			var cardId = $.Cmdbuild.dataModel.getValue("selectedForm", "id");
			if (cardId == oldId) {
				return;
			}
			oldId = cardId;
			console.log("-<-<-<-<-<-", cardId);
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
