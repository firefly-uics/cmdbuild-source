(function() {
	Ext.define("CMDBuild.view.management.classes.masterDetail.DetailWindow", {
		extend: "CMDBuild.view.management.common.CMCardWindow",

		classId: undefined, // setted in instantiation
		cardId: undefined,
		cmEditMode: false, // if true, after the attributes load go in edit mode
		withButtons: false, // true to use the buttons build by the CMCardPanel

		loadCard: function() {
			function fillForm(attributes) {
				attributes = removeFKOrMasterDeference.call(this, attributes);
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
							value: this.masterData.Id
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
		if (attribute && attribute.idDomain) {
			var invertedDirection = attribute.domainDirection ? "_I" : "_D";
			var directedDomain = attribute.idDomain + invertedDirection;
			return (directedDomain == this.idDomain);
		}
		return false;
	};
})();