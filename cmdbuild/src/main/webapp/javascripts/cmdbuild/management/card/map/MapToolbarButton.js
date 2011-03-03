CMDBuild.Management.MapToolbarButton = Ext.extend(Ext.Button, {
	initComponent: function(){
		CMDBuild.Management.MapToolbarButton.superclass.initComponent.apply(this, arguments)
		
		if (this.map && this.control) {
			this.map.addControl(this.control);
		} else {
			throw Error('The MapToolbarButton must have an associated map and a control');
		}
		
		Ext.apply(this, {
			enableToggle: true,
			allowDepress: false,
			toggleHandler: function(button, state){
				if (state) {
					this.control.activate();
				} else {
					this.control.deactivate();
				};
			}
		});
	}
});
Ext.reg('maptbbutton', CMDBuild.Management.MapToolbarButton);