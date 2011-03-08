CMDBuild.PrintMenuButton = Ext.extend(Ext.Toolbar.SplitButton, {
	text: CMDBuild.Translation.common.buttons.print,
	iconCls: 'print',
	
	//custom fields
	/**
	 * the function to call when click on a menu item, 
	 * it recive the print format as parameter
	 * */
	callback: function(){},
	/**
	 * when instantiate the button you can chose between these options
	 * */
	formatList: ['pdf', 'csv', 'odt', 'rtf'],

	initComponent: function(){
		this.menu = new Ext.menu.Menu();
		CMDBuild.AddCardMenuButton.superclass.initComponent.apply(this, arguments);
		this.fillMenu();
	},

	//private
	fillMenu: function() {
		var formats = this.formatList;
		for (var i=0, l=formats.length; i<l; i++) {
			var format = formats[i];
			this.menu.add({
				text: CMDBuild.Translation.common.buttons.as + " " + format.toUpperCase(),
				iconCls: format,
				handler: this.callback.createDelegate(this.scope, [format])
			});	
		}
	}
});