(function() {
	var isTheFKFieldToTarget = function(attribute) {
		if (attribute && this.fkAttribute) {
			return attribute.name == this.fkAttribute.name;
		}
		return false;
	};
	
	var isMasterReference = function(attribute) {
		if (attribute && attribute.idDomain) {
			var invertedDirection = attribute.domainDirection ? "_I" : "_D";
			var directedDomain = attribute.idDomain + invertedDirection;
			return (directedDomain == this.idDomain);
		}
		return false;
	};
	
	CMDBuild.Management.DetailWindow = Ext.extend(CMDBuild.Management.CardWindow, {
		withToolBar: false,
		withButtons: false,
		allowNoteField: false,
		
		loadCard: function() {
			var attributesToAdd = this.removeFKOrMasterDeference();
			var r = this.buildRecord(this.cardData);
			this.cardForm.loadCard(attributesToAdd, {
				record:r,
				enableModify: this.withButtons
			});
		},

		//private
		removeFKOrMasterDeference: function() {
			var attributesToAdd = [];
			for (var i = 0; i < this.classAttributes.length; i++) {
				var attribute = this.classAttributes[i];
				if (attribute) {
					if (isTheFKFieldToTarget.call(this, attribute) 
							|| isMasterReference.call(this, attribute)) {
						// not to create the relation if the
						// detail has a reference to the master
						// used in the AddDetailWindow
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
		},

		notifyAndcloseWindow: function(newCardId) {
			if (this.updateEventName) {
				var classId = this.cardData ? this.cardData.classId : this.classId; // loaded card or newly created card
				this.publish(this.updateEventName, {classId: classId, cardId: newCardId});
			}
			this.destroy();
		}
	});
})();