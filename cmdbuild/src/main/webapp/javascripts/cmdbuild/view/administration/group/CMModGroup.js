(function() {

var tr = CMDBuild.Translation.administration.modsecurity;

Ext.define("CMDBuild.view.administration.group.CMModGroup", {
	extend: "Ext.panel.Panel",
	cmName: 'group',

	initComponent: function() {
	
		this.addGroupButton = new Ext.button.Button( {
			iconCls : 'add',
			text : tr.group.add_group
		});

		this.groupForm = new CMDBuild.view.administration.group.CMGroupForm({
			title: tr.tabs.properties
		});

		this.privilegeGrid = new CMDBuild.view.administration.group.CMGroupPrivilegeGrid({
			title: tr.tabs.permissions
		});

		this.userPerGroup = new CMDBuild.view.administration.group.CMGroupUsers({
			title: tr.users
		});

		this.tabPanel = new Ext.TabPanel({
			border : false,
			activeTab : 0,
			region: "center",
			items : [
				this.groupForm,
				this.privilegeGrid,
				this.userPerGroup
			]
		});

		Ext.apply(this, {
			tbar:[this.addGroupButton],
			title : tr.group.title,
			basetitle : tr.group.title+ ' - ',
			items: [this.tabPanel],
			layout: "border",
			border: false
		});

		this.callParent(arguments);
	},

	onGroupSelected: function(eventParams) {
		this.privilegeGrid.enable();
		this.userPerGroup.enable();
	},

	onAddGroup: function() {
		this.tabPanel.setActiveTab(this.tabPanel.items.get(0));
		this.privilegeGrid.disable();
		this.userPerGroup.disable();
	}
});

})();