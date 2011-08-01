(function() {
	Ext.define("CMDBuild.controller.management.common.CMAddDetailWindowController", {
		extend: "CMDBuild.controller.management.common.CMCardWindowController",

		//override
		buildSaveParams: function() {
			var p = this.callParent(arguments);

			if (this.view.referenceToMaster) {
				var r = this.view.referenceToMaster;
				p[r.name] = r.value;
			} else {
				this.addRelationAfterSave = true;
			}

			return p;
		},

		//override
		onSaveSuccess: function(form, res) {
			if (this.addRelationAfterSave) {
				var p = buildParamsToCreateRelation.call(this, res);

				CMDBuild.ServiceProxy.relations.add({
					params: { JSON: Ext.JSON.encode(p) },
					scope: this,
					success: function() {
						this.view.destroy();
					}
				});
			} else {
				this.view.destroy();
			}
		}
	});

	function buildParamsToCreateRelation(response) {
		var newDetailId = response.result.id,
			detail = this.view.detail,
			master = this.view.masterData,
			masterPosition = getMasterPosition(master, detail),
			detailPosition = getDetailPosition(masterPosition),
			attrs = {};

		attrs[masterPosition] = {
			id: master.get("Id"),
			cid: master.get("IdClass")
		};
		attrs[detailPosition] = {
			id: response.result.id,
			cid: response.params.IdClass
		};

		return {
			did: detail.get("id"),
			attrs: attrs
		};
	}

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