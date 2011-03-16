CMDBuild.InitHeader = function() {
	var msgBox = Ext.get('msg-ct');
	var manageExpand = function(evt) {
		//exit if click a link
		var target = evt.getTarget(null, null, true); 
		if(target.is('a')) {
			return;
		}
		
		var currentX = this.getX();
		var anim = !(Ext.isIE || Ext.isChrome); //Only Firefox is able to animate without error
		
		if (this.getY() > 3) {    	
			this.setY(3, anim);      
		} else {    	
			this.setY(23, anim);
		}
		
		//IE and Chrome set the X to a random value after the setY()
		this.setX(currentX);
	};
	
	Ext.get('msg').on('click', manageExpand, msgBox);	
};