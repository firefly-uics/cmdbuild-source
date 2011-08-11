(function() {
	Ext.define("CMDBuild.controller.management.common.CMDetailWindowController", {
		extend: "CMDBuild.controller.management.common.CMCardWindowController",
		
		constructor: function() {
			this.callParent(arguments);

			this.view.mon(this.view.cardPanel, "cmFormFilled", function() {
				if (this.view.hasRelationAttributes) {
					loadRelationToFillRelationAttributes.call(this);
				}
			}, this);
		},

		getRelationsAttribute: function() {
			var form = this.getForm(),
				ff = form.getFields(),
				out = [];

			Ext.Array.forEach(ff.items, function(f) { 
				if(f.CMAttribute && f.CMAttribute.cmRelationAttribute) {
					f.enable();
					out.push(f);
				}
			});

			return out;
		},
		
		// private, could be overridden
		buildParamsToSaveRelation: function(detailData) {
			var detail = this.view.detail;

			var out = {
				did: detail.get("id"),
				attrs: this.fillRelationAttributesParams(detailData, {})
			};

			if (this.relation) {
				out["id"] = this.relation.rel_id;
			}

			return out;
		},

		fillRelationAttributesParams: function(detailData, attributes) {
			var relationAttributes = this.getRelationsAttribute();

			Ext.Array.forEach(relationAttributes, function(a) {
				attributes[a.CMAttribute.name] = a.getValue();
			});

			return attributes;
		},

		//override
		beforeRequest: function(form) {
			// Disable the fields of the relation attribute
			// to don't send them with the save request
			var ff = form.getFields();
			Ext.Array.forEach(ff.items, function(f) { 
				f.setDisabled(f.CMAttribute && f.CMAttribute.cmRelationAttribute);
			});
		},
		
		//override
		onSaveSuccess: function(form, res) {
			// if this.relation is different to undefined,
			// so the relation data was loaded because has some attributes
			// use it to update the relation attributes;
			if (this.relation) {
				var p = this.buildParamsToSaveRelation({
					id: this.view.cardId,
					cid: this.view.classId
				});

				CMDBuild.ServiceProxy.relations.modify({
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

	function loadRelationToFillRelationAttributes() {
		var me = this,
			v = this.view,
			p = {
				Id: v.cardId,
				IdClass: v.classId,
				domainId: v.detail.get("id"),
				src : v.detail.getDetailSide()
			};

		CMDBuild.ServiceProxy.relations.getList({
			params: p,
			scope: this,
			success: function(a,b, response) {
				var domains = response.domains;
				/*
				 * the response structure is: domains: [{
				 * 		id: "xxx",
				 * 		relations: [{
							rel_attr: {
								name:value,
								...,
								name: value
							}
							rel_id: XXX
				 * 		}]
				 * 	}]
				 * */
				try {
					if (domains.length > 1) {
					_debug("TODO ecco perchè sbaglia il modify, il get relation torna due domini, che " +
							"in realtà è lo stesso nei due versi", domains)
					}
					me.relation = domains[0].relations[0]; // set this for the save request
					var fields = me.getRelationsAttribute(),
						attributes = me.relation.rel_attr;
	
					Ext.Array.forEach(fields, function(f) {
						f.setValue(attributes[f.name]);
					});

				} catch (e) {
					me.relation = undefined;
					_debug("No relations", e);
				}
			}
		});
	}
})();