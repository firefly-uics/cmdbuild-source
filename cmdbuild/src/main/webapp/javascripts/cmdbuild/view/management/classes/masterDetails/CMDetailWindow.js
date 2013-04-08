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

		loadCard: function() {_deprecated();
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
})();