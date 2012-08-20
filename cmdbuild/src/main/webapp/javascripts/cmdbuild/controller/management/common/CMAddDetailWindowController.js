(function() {

	Ext.define("CMDBuild.controller.management.common.CMAddDetailWindowController", {
		extend: "CMDBuild.controller.management.common.CMDetailWindowController",

		//override
		buildSaveParams: function() {
			var p = this.callParent(arguments);

			if (this.referenceToMaster) {
				// set the value to the field that was hidden
				var r = this.referenceToMaster;
				p[r.name] = r.value;
				// Then set the save relation to emptyFn because
				// the relation is automatically 
				this.saveRelationAction = Ext.emptyFn; //this.view.hasRelationAttributes ? this.updateRelation : Ext.emptyFn;
			} else {
				// set the function to create the relation
				// after the card
				this.saveRelationAction = this.addRelation;
			}

			return p;
		},

		//override
		onSaveSuccess: function(form, res) {
			if (this.saveRelationAction) {
				this.saveRelationAction(form, res);
			}
			this.view.destroy();
			CMDBuild.LoadMask.get().hide();
		},

		addRelation: function(form, res) {
			var detailData = {
				id: res.result.id,
				cid: res.params.IdClass
			};

			var p = this.buildParamsToSaveRelation(detailData);

			CMDBuild.ServiceProxy.relations.add({
				params: { JSON: Ext.JSON.encode(p) }
			});
		},

		// override
		fillRelationAttributesParams: function(detailData, attributes) {
			var out = this.callParent(arguments),
				detail = this.view.detail,
				master = this.view.masterData,
				masterPosition = getMasterPosition(master, detail),
				detailPosition = getDetailPosition(masterPosition);

			out[masterPosition] = {
				id: master.get("Id"),
				cid: master.get("IdClass")
			};

			out[detailPosition] = {
				id: detailData.id,
				cid: detailData.cid
			};

			return out;
		}
	});
	
	function getMasterPosition(m, detail) {
		var cardinality = detail.get("cardinality"),
			masterClassId = m.get("IdClass");

		if (cardinality == "1:1") {
			throw "Wrong cardinality for a MasterDetail domain"
		}

		if (Ext.Array.contains(_CMUtils.getAncestorsId(masterClassId), detail.get("idClass1"))) {
			if (cardinality == "1:N") {
				return "_1";
			} else {
				return "_2";
			}
		} else {
			if (cardinality == "N:1") {
				return "_2";
			} else {
				return "_1";
			}
		}
	}

	function getDetailPosition(masterPosition) {
		if (masterPosition == "_1") {
			return "_2";
		} else {
			return "_1";
		}
	}
})();