(function() {

	Ext.define("CMDBuild.Administration.LayerVisibilityGrid", {
		extend: "CMDBuild.Administration.LayerGrid",
		currentClass: undefined,

		cmCheckColumnReadOnly: false,

		initComponent: function() {
			this.callParent(arguments);
			var me = this;

			this.mon(this, "activate", function() {
				CMDBuild.LoadMask.get().show();
				this.store.load({
					callback: function(records, operation, success) {
						Ext.Function.createDelayed(function() {
							selectVisibleLayers.call(me, me.currentClassId);
							CMDBuild.LoadMask.get().hide();
						}, 500)();
					}
				});
			}, this);
		},

		onClassSelected: function(s) {
			this.currentClassId = s.id || 0;
			selectVisibleLayers.call(this, this.currentClassId);
		},

		/**
		 * @override
		 */
		onVisibilityChecked: function(cell, recordIndex, checked) {
			var record = this.store.getAt(recordIndex),
				classId = this.currentClassId,
				me = this;

			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.saveLayerVisibility({
				classId: classId,
				master: record.data.masterTableId,
				featureTypeName: record.data.name,
				checked: checked,
				success: function() {
					_CMCache.onGeoAttributeVisibilityChanged(classId, record.data, checked);
					selectVisibleLayers.call(me, classId);
				},
				failure: function() {
					record.set(column.dataIndex, !checked);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
					record.commit();
				}
			});
		}
	});

	function selectVisibleLayers(tableId) {
		var et = _CMCache.getEntryTypeById(tableId),
			visibleGeoAttrs = [],
			s = this.store,
			di = this.getVisibilityColDataIndex();

		if (et) {
			geoAttrs = et.getVisibleGeoAttrs();
		}

		function geoAttrEquals(a1, a2) {
			if (a1.masterTableId) {
				return a1.masterTableId == a2.masterTableId && a1.name == a2.name;
			} else {
				// geoserver layer
				return !a2.masterTableId && a1.name == a2.name;
			}
		};

		s.each(function(record) {
			var visible = false;
			for (var i=0, l=geoAttrs.length; i<l; ++i) {
				var attr = geoAttrs[i];
				if (geoAttrEquals(attr, record.data)) {
					visible = attr.isvisible;
					break;
				}
			}
			record.set(di, visible);
			record.commit();
		}, this);
	};
})();