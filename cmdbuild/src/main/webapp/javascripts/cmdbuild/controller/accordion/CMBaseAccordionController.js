(function() {
	Ext.define("CMDBuild.controller.accordion.CMBaseAccordionController", {
		constructor: function(accordion) {
			this.accordion = accordion;

			this.accordion.on("expand", function() {
				if (this.accordion.cmSilent === true) {
					this.onAccordionExpanded();
				}
			}, this);
			
			if (this.accordion.getSelectionModel) {
				manageTreeEvents.call(this);
			}
		},

		onAccordionExpanded: function() {
			_CMMainViewportController.bringTofrontPanelByCmName(this.accordion.cmName);
			reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf.call(this);
		}

	});
	
	function manageTreeEvents() {
		this.accordionSM = this.accordion.getSelectionModel();

		this.onAccordionNodeSelect = function(sm, selections) {
			if (selections.length > 0) {
				var s = selections[0];
				_CMMainViewportController.bringTofrontPanelByCmName(s.get("cmName"), s);
			}
		};

		this.accordionSM.on("selectionchange", this.onAccordionNodeSelect, this);
	}

	function reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf() {
		if (this.accordionSM) {
			var selections = this.accordionSM.getSelection();
			
			if (selections.length > 0) {
				var toSelect = [selections[0]];
				this.onAccordionNodeSelect(this.accordionSM, toSelect);
			} else {
				var r = this.accordion.getRootNode();
				if (typeof r.firstChild != "undefined") {
					// Defer the call because Ext.selection.RowModel
					// for me.views.lenght says "can not refer to length of undefined"
					Ext.Function.createDelayed(function() {
						this.accordionSM.select([r.firstChild]);
					}, 100, this)();
				}
			}
		}
	}

})();