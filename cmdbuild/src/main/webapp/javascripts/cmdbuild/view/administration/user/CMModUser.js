(function() {

	var tr = CMDBuild.Translation.administration.modsecurity;

	Ext.define("CMDBuild.view.administration.user.CMModUser", {
		extend : "Ext.panel.Panel",
		cmName : "users",

		initComponent : function() {

			this.addUserButton = new Ext.button.Button( {
				iconCls : 'add',
				text : tr.user.add_user
			});

			this.userGrid = new CMDBuild.view.administration.user.CMUserGrid( {
   				tbar : [ this.addUserButton ],
				title : tr.user.title,
				region : "center"
			});

			this.userForm = new CMDBuild.view.administration.user.CMUserForm( {
				region : "south",
				height : "65%",
				split : true
			});

			Ext.apply(this, {
				modtype : 'user',
				basetitle : tr.user.title + ' - ',
				layout : 'border',
				frame : false,
				border : false,
				items : [ this.userGrid, this.userForm ]
			});

			this.callParent(arguments);
		},

		selectUser : function(eventParams) {

		}

	});

})();