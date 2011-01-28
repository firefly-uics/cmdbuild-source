CMDBuild.Splash = function(id, cls) {
	var body = Ext.getBody();
	var tr = CMDBuild.Translation.common.splash;
	
	var credits = '<ul>'
		+ '<li> <span class="splashBold"> <a href="http://www.tecnoteca.com" target="_blank"> Tecnoteca srl </a></span> ' + tr.design + ', '+tr.implementation+', '+ tr.maintainer +'</li>'
		+ '<li> <span class="splashBold"> <a href="http://www.comune.udine.it" target="_blank"> ' + tr.municipality + ' </a> </span> '+ tr.principal+'</li> '
		+ '<li> <span class="splashBold"> <a href="http://www.cogitek.it" target="_blank"> Cogitek srl</a> </span> '+ tr.consultant +' </li>'		
		+ '</ul>';
	
	var splashText = '<div class="spalshMotto">Open Source Configuration and Management Database</div>'
		+ '<span class="splashSubTitle">Tecnoteca srl - ' + tr.municipality + ' - Cogitek srl'+ '</span>'
		+ '<span class="splashSubTitle copyright">Copyright Tecnoteca srl</span>';
		
	Ext.DomHelper.append(body, [{
		tag: 'div',
		id: id,
		cls: cls
	}, {
		tag: 'div',
		id: id+'_image',
		cls: cls+'_image',
		children: [{
			tag: 'div', 
			id: id+"_padding"
		},{
			tag: 'div', 
			id: id+"_central", 
			cls: cls+'_central', 
			html: splashText
		}, {
			tag: 'div',
			id: id+'_text',
			cls: cls+'_text splash_loading'
		}, {
			tag: 'div',
			id: id+'_version',
			cls: cls+'_text splash_loading',
			html: CMDBuild.Translation.release
		}]
	}]);
	
	var splashEl = Ext.get(id);
	var splashImageEl = Ext.get(id+'_image');
	var centralWrapper = Ext.get(id+'_central');
	var textWrapper = Ext.get(id+'_text');
	
	var setSplashSize = function() {
		var bodyHeight = body.getHeight();
		var bodyWidth = body.getWidth();
		var imageX = (bodyWidth - splashImageEl.getBox().width)/2;
		var imageY = (bodyHeight - splashImageEl.getBox().height)/2;
		
		splashImageEl.setXY([imageX, imageY]);
		
		splashEl.setXY(body.getXY());
	};
	
	this.show = function() {
		setSplashSize();
		splashEl.show();
		splashImageEl.show();
		return this;
	};
	
	this.hide = function() {
		if (Ext.isIE) {
			splashEl.hide();//IE has some problems of rendering with the fade
			splashImageEl.hide();
		} else {
			splashEl.fadeOut({
				duration: 1
			});
			splashImageEl.fadeOut({
				duration: 1
			});
		}
		return this;
	};
	
	this.setText = function(text) {
		textWrapper.dom.innerHTML = text || "";
		return this;
	};
	
	this.setCentralText = function(text) {
		centralWrapper.dom.innerHTML = text || "";
		return this;
	};
	
	this.showAsPopUp = function() {
		this.setText('<a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a>');
		this.setCentralText(credits);
		
		splashEl.removeClass(cls);
		splashEl.addClass("ext-el-mask");
		
		splashEl.show();
		splashImageEl.show();
		
		var onClick = function(e) {
			//exit if click a link
			var target = e.getTarget(null, null, true); 
			if(target.is('a')) {
				return;
			}			
			splashEl.hide();
			splashImageEl.hide();
		};
		
		splashEl.on('click', onClick , this);
		splashImageEl.on('click', onClick , this);
		return this;
	};
	
	Ext.EventManager.onWindowResize(function() {
		setSplashSize();
	});
}