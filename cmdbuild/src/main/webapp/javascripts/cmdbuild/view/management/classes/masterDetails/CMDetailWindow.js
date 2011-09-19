(function() {
	Ext.define("CMDBuild.view.management.classes.masterDetail.DetailWindow", {
		extend: "CMDBuild.view.management.common.CMCardWindow",

		classId: undefined, // set on instantiation
		cardId: undefined,
		fkAttribute: undefined, // set on instantiation if needed
		detail: undefined,// set on instantiation if needed
		referencedIdClass: undefined, // set on instantiation if needed

		cmEditMode: false, // if true, after the attributes load go in edit mode
		withButtons: false, // true to use the buttons build by the CMCardPanel

		loadCard: function() {
			function fillForm(attributes) {
				attributes = removeFKOrMasterDeference.call(this, attributes);
				attributes = addDomainAttributesIfNeeded.call(this, attributes);

				this.cardPanel.fillForm(attributes, this.cmEditMode);
				if (this.cardId) {
					this.cardPanel.loadCard(this.cardId, this.classId);
				}
			}

			_CMCache.getAttributeList(this.classId, Ext.bind(fillForm, this));
		}
	});
	
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