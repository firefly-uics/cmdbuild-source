CMDBuild.Management.EditDetailWindow = Ext.extend(CMDBuild.Management.DetailWindow, {
	withToolBar: false,
	withButtons: true,

	initComponent: function() {
		var _this = this;
		CMDBuild.Management.EditDetailWindow.superclass.initComponent.apply(this, arguments);	
		Ext.apply(this, {
            title: this.getTitle()
        });
	},

	getTitle: function() {
		if (this.editable) {
			return CMDBuild.Translation.management.moddetail.editdetail 
			+ " "
			+ this.cardData.Description;
		} else {
			return this.cardData.Description;
		}
	}
});