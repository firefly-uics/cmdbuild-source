(function() {

	Ext.define("CMDBuild.controller.administration.gis.CMModLayerOrderController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.view.mon(this.view, "cm-rowmove", onRowMoved, this);
		},

		onViewOnFront: function() {
			this.view.store.load();
		}
	});

	/*
	 *p = {
		node: node,
		data: data,
		dropRec: dropRec,
		dropPosition: dropPosition
	}*/
	function onRowMoved(p) {
		var oldIndex = getOldIndex(p.data),
			newIndex = getNewIndex(p.dropRec, p.dropPosition);

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.saveLayerOrder({
			oldIndex: oldIndex,
			newIndex: newIndex,
			failure: function() {
				this.view.getStore().reload();
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function getOldIndex(data) {
		var oldIndex = -1;
		try {
			oldIndex = data.records[0].data.index
		} catch (e) {
			CMDBuild.log.Error("Can not get the old index");
		}

		return oldIndex;
	}

	function getNewIndex(dropRec, dropPosition) {
		var index = dropRec.data.index;
		if (dropPosition == "after") {
			return parseInt(index) + 1;
		} else {
			return parseInt(index);
		}
	}
})();