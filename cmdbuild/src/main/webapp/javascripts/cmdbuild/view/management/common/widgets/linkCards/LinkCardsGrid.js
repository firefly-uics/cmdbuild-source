(function() {

	Ext.define("CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmVisible: true,

		selectByCardId: function(cardId) {
			if (typeof cardId == 'number') {
				var recIndex = this.getStore().find("Id", cardId);

				if (recIndex >= 0)
					this.getSelectionModel().select(recIndex, true);
			}
		},

		setCmVisible: function(visible) {
			this.cmVisible = visible;

			if (this.paramsToLoadWhenVisible) {
				this.updateStoreForClassId(this.paramsToLoadWhenVisible.classId, this.paramsToLoadWhenVisible.o);
				this.paramsToLoadWhenVisible = null;
			}

			this.fireEvent("cmVisible", visible);
		},

		updateStoreForClassId: function(classId, o) {
			if (this.cmVisible) {
				this.callParent(arguments);
				this.paramsToLoadWhenVisible = null;
			} else {
				this.paramsToLoadWhenVisible = {
					classId:classId,
					o: o
				};
			}
		}
	});

})();