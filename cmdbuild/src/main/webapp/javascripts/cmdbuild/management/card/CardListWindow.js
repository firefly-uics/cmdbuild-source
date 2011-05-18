/*
 * TODO Refactor the class hierarchy, pulling down everything they have in common
 */

CMDBuild.Management.CardListWindow = Ext.extend(CMDBuild.PopupWindow, {

	_buildAddButton: function(buttonConfig) {
		var addCardButton = new CMDBuild.AddCardMenuButton(buttonConfig);
		
		addCardButton.on(addCardButton.eventName, function(p) {
			var addCardWindow = new CMDBuild.Management.AddCardWindow({
				classId: p.classId,
				className: p.className
			});
			addCardWindow.on('cmdbuild-add-card', function() {
				this.cardList.reload();
				this.publish('cmdb-reload-card', {classId: p.classId});
			}, this);
			addCardWindow.show();
		}, this);
		return addCardButton;
	}
});
