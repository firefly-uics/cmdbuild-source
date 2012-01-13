(function() {
	Ext.define("CMDBuild.controller.management.common.CMDetailWindowController", {
		extend: "CMDBuild.controller.management.common.CMCardWindowController",

		constructor: function() {
			this.callParent(arguments);

			this.mon(this.view, this.view.CMEVENTS.formFilled, function() {
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
				this.updateRelation(form, res);
			}
			this.callParent(arguments);
		},

		updateRelation: function(form, res) {
			var p = this.buildParamsToSaveRelation({
				id: this.view.cardId,
				cid: this.view.classId
			});

			CMDBuild.ServiceProxy.relations.modify({
				params: { JSON: Ext.JSON.encode(p) }
			});
		},

		// override to remove the reference
		loadFields: function(entryTypeId, cb) {
			var me = this;

			_CMCache.getAttributeList(entryTypeId, function(attributes) {
				attributes = removeFKOrMasterDeference.call(me.view, attributes);
				attributes = addDomainAttributesIfNeeded.call(me.view, attributes);

				me.view.fillForm(attributes, editMode = false);
				if (cb) {
					cb();
				}
			});
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
							"in realtà è lo stesso nei due versi", domains);
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

	// to remove the reference
	function removeFKOrMasterDeference(attributes) {
		var attributesToAdd = [];
		for (var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];

			if (attribute) {
				if (isTheFKFieldToTarget.call(this, attribute) 
						|| isMasterReference.call(this, attribute)) {
					// not to create the relation if the
					// detail has a reference to the master
					// used to add a detail
					if (this.masterData) {
						this.referenceToMaster = {
							name: attribute.name,
							value: this.masterData.get("Id")
						};
					}
				} else {
					attributesToAdd.push(attribute);
				}
			}
		}

		return attributesToAdd;
	}

	function isTheFKFieldToTarget(attribute) {
		if (attribute && this.fkAttribute) {
			return attribute.name == this.fkAttribute.name;
		}
		return false;
	};

	function isMasterReference(attribute) {
		return this.referencedIdClass
				&& attribute
				&& attribute.referencedIdClass == this.referencedIdClass;
	};

	function addDomainAttributesIfNeeded(attributes) {
		var domainAttributes = this.detail.getAttributes() || [],
			out = [];

		if (domainAttributes.length > 0) {

			this.hasRelationAttributes = true;

			var areTheAttributesDividedInTab = false;
			for (var i=0, l=attributes.length; i<l; ++i) {
				var a = attributes[i];
				if (a.group && a.group != "") {
					areTheAttributesDividedInTab = true;
					break;
				}
			}

			// to have a useful label for the tab that has the
			// detail's attributes modify the group of all attributes
			// if this is undefined
			if (areTheAttributesDividedInTab) {
				out = [].concat(attributes);
			} else {
				Ext.Array.forEach(attributes, function(a) {
					var dolly = Ext.apply({}, a);
					dolly.group = CMDBuild.Translation.management.modcard.detail_window.detail_attributes;
					out.push(dolly);
				});
			}

			// add the attributes of the domain and add to them
			// a group to have a separated tab in the form
			Ext.Array.forEach(domainAttributes, function(a) {
				var dolly = Ext.apply({}, a);
				dolly.group = CMDBuild.Translation.management.modcard.detail_window.relation_attributes;
				// mark these attributes to be able to detect them
				// when save or load the data. There is the possibility
				// of a names collision.
				dolly.cmRelationAttribute = true;
				out.push(dolly);
			});
		} else {
			out = [].concat(attributes);
		}

		return out;
	}
})();