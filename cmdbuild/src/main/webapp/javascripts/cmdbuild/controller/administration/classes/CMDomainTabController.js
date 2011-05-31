(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {
		constructor: function(view) {
			this.view = view;
			this.selection = null;
		},

		onSelectClass: function(classId) {
			this.selection = classId;
			this.view.store.load({
				params : {
					idClass : classId || -1
				}
			});
		},

		onAddClassButtonClick: function() {
			this.selection = null;
		}

	});

})();