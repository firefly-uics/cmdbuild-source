CMDBuild.Management.AddDetailActionMenu = Ext.extend(Ext.Toolbar.SplitButton, {
	translation : CMDBuild.Translation.management.moddetail,
	iconCls: 'add',
	
	initComponent: function(){
		this.details = {};
		this.originalText = this.translation.adddetail,
		Ext.apply(this, {
			text: this.originalText,
			menu : {items :[]},
			handler: this.onClick
		});
		CMDBuild.Management.AddDetailActionMenu.superclass.initComponent.apply(this, arguments);
	},
	
	setMenuItems: function(detailSubclasses){
		this.detailSubclasses = detailSubclasses;
		this.menu.removeAll();
		for(var detailIndex = 0, len = this.detailSubclasses.length; detailIndex < len; detailIndex++) {
			var detail = detailSubclasses[detailIndex];				
			if ( this.hasCreatePrivileges(detail) && this.isAClass_NotAnActivity(detail) ) {
				this.menu.addMenuItem({
					text: detail.className,
					detailIndex: detailIndex,
					scope: this,
					handler: function(item, e){
						this.fireAddDetailEvent(item.detailIndex);
					}
				});
			}
		}
		this.disableIfEmpty();
	},
	
	hasCreatePrivileges: function(detail) {
		return detail.priv_create;
	},
	
	hasWritePrivileges: function() {
		return detail.priv_write;
	},
	
	isAClass_NotAnActivity: function(detail) {
		return detail.classType == "class";
	},
	
	disableIfEmpty: function() {
		if ( this.isEmpty() ) {
			this.disable();
		} else {
			this.enable();
		}
	},
	
	isEmpty: function(){
		return this.menu.items.items.length == 0 ? true : false;
	},
	
	fireAddDetailEvent: function(detail){
		this.fireEvent('cmdb-addDetail', this.detailSubclasses[detail]);		
	},
	
	resetText: function(){
		this.setText(this.originalText);
	},
	
	onClick: function() {
		if (this.disabled) {
			return;
		}
	
		if (this.menu.items.length == 1) {
			this.fireAddDetailEvent(this.menu.items.items[0].detailIndex);		
		} else {
			this.showMenu();
		}
		
	}
});
Ext.reg('adddetailmenu', CMDBuild.Management.AddDetailActionMenu);