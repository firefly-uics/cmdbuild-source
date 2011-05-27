CMDBuild.Management.AddDetailWindow = Ext.extend(CMDBuild.Management.DetailWindow, {
	updateEventName: 'cmdb-reload-card',
	titlePortion: CMDBuild.Translation.management.moddetail.adddetail,
	withToolBar: false,
	withButtons: true,
	
	loadCard: function() {
		var attributesToAdd = this.removeFKOrMasterDeference();
		this.cardForm.buildTabbedPanel(attributesToAdd);
		this.cardForm.newCard({
			classId: this.classId
		});
	},

	onCreateCard: function(newCardId) {
		if (this.referenceToMaster) {
			this.notifyAndcloseWindow(newCardId);
		} else {
			this.createRelation(newCardId);
		}
	},

	createRelation: function(newCardId) {
		this.relationData = {};
		this.relationData[this.idDomain] = [this.classId + "_" + newCardId];
		
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/createrelations',
			params : {
				"IdClass": this.masterData.IdClass,
				"Id": this.masterData.Id,
				"Relations":  Ext.util.JSON.encode(this.relationData)
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			method : 'POST',
			scope : this,
			success : function() {
				this.notifyAndcloseWindow(newCardId);
			}
	 	});
	}
});
Ext.reg('adddetailwindow', CMDBuild.Management.AddDetailWindow);