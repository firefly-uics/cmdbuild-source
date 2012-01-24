Ext.namespace('Ext.ux.plugins');
Ext.ux.plugins.HeaderButtons = function(config) {
	Ext.apply(this, config);
};

Ext.extend(Ext.ux.plugins.HeaderButtons, Ext.util.Observable, {
	
	init: function(panel) {
        if (panel.hbuttons) {          
            Ext.apply(panel, {
				onRender: panel.onRender.createSequence(function(ct, position) {
					if (this.headerButtons && this.headerButtons.length > 0) {
						var hl = this.header.createChild({
							cn:{html:'<div class="headerleft"></div>'}
						}, this.header.first('span', true), true); // insert before header text (but after tools)
						this.header.addClass('panelheader');
						var hr = this.header.createChild({
							cn:{html:'<div class="headerright"></div>'}
						}, undefined , true); // insert before header text (but after tools)
						
						for (var i = 0, len = this.headerButtons.length; i < len; i++) {
							var b = this.headerButtons[i];
							var button = document.createElement('div');
							if(b.position && b.position=='right') {
								b.render(hr.appendChild(button));
							} else {
								b.render(hl.appendChild(button));
							}
						}
					}		        
				}),

				addHeaderButton: function(config, handler, scope) {
					var action;
					if(config.initialConfig && config.isAction) {
						action = config;
					} else {
						var bc = {
							handler: handler,
							scope: scope,
							hideParent: true,
							iconCls: config.iconCls
						};
						if(typeof config == "string") {
							bc.text = config;
						} else {
							Ext.apply(bc, config);
						}
						action = new Ext.Action(bc);
					}
					var btn = new Ext.Button(action);
					btn.ownerCt = this;
					if(!this.headerButtons) {
						this.headerButtons = [];
					}
					this.headerButtons.push(btn);
					return btn;
				}            
            });

            var btns = panel.hbuttons;
            
            panel.headerButtons = [];
            for (var i = 0, len = btns.length; i < len; i++) {
            	if(btns[i]) {
                	if(btns[i].render) {
						panel.headerButtons.push(btns[i]);
                	} else {
                    	panel.addHeaderButton(btns[i]);
                	}
                } else {
                	CMDBuild.log.warn("An element of header has been ignored")
                };
            }
            delete panel.hbuttons;
        }
	}
});