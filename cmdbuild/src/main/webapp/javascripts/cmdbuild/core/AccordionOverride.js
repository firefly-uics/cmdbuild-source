Ext.override(Ext.layout.Accordion, {
	setActiveItem: function(c) {
		c = this.container.getComponent(c);
		if(this.activeItem != c){
			if(c.rendered && c.collapsed){
				c.expand();
			}else{
				this.activeItem = c;
			}
		}
	}
});