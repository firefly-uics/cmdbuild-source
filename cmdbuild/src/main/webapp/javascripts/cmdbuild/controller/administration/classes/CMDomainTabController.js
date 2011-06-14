(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {
		constructor: function(view) {
			this.view = view;
			this.selection = null;
		},

		onClassSelected: function(classId) {
			this.selection = classId;
			this.view.store.load({
				params : {
					idClass : classId || -1
				}
			});
			this.view.enable();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.disable();
		}

	});

})();