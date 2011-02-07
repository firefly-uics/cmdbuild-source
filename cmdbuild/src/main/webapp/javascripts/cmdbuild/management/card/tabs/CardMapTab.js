CMDBuild.Management.CardMapTab = Ext.extend(Ext.Panel, {
/*
	translation : CMDBuild.Translation.management.modcard.maptoolbar,

	initComponent: function(config) {	
		if (CMDBuild.Config.gis.enabled == "false") {
			Ext.apply(this, {disabled: true});
		} else {
			this.initIfEnabled(config)
		}
		CMDBuild.Management.CardMapTab.superclass.initComponent.apply(this, arguments);
	},

	initIfEnabled: function() {	
		this.map = new CMDBuild.Management.CmdbMap();

		this.toolbar = new Ext.Toolbar({
			items : [{
				xtype: 'maptbbutton',
				text: this.translation.add,
				iconCls: 'mapToolbarDrawPoint',
				control: this.map.drawPoint
			},{
				xtype: 'maptbbutton',
				text: this.translation.drag,
				iconCls: 'mapToolbarModifyCard',
				control: this.map.dragCard
			},{
				xtype: 'maptbbutton',
				text:  this.translation.zoom_in,
				iconCls: 'mapToolbarZoomIn',
				control: this.map.zoomIn
			},{
				xtype: 'maptbbutton',
				text: this.translation.zoom_out,
				iconCls: 'mapToolbarZoomOut',
				control: this.map.zoomOut
			},{
				xtype: 'maptbbutton',
				text: this.translation.drag_map,
				iconCls: 'mapToolbarPan',
				pressed: true,
				control: this.map.dragPan
			}]
		});
		
		var mainPanel = new Ext.Panel({
			layout: 'border',
			items: [{
	    		region: 'center',
	    		xtype: 'mapcomponent',
                map: this.map.getMap(),
               	tbar: this.toolbar,
               	buttonAlign: 'center',
                buttons:[{
           			xtype: 'button',
           			text: CMDBuild.Translation.common.buttons.save
					//TODO WFS.update
           		},{
           			xtype: 'button',
           			text: CMDBuild.Translation.common.buttons.abort,
           			scope: this,
           			handler: function(){
           				if(this.map.getVectorLayer().features.length == 0){
           					showErrorMsg();
           				}else{
							showConfirmMsg();
						}
					}
	            }]
	    	},{
	    		region: 'west',
	    		xtype: 'panel',
	    		width: 200,
	    		split: true,
	    		layout: 'accordion',
	    		items: [{
	    			title: 'Layer',
	    			xtype: 'layertree',
	               	map: this.map.getMap()
	    		},{
	    			xtype: 'panel',
	    			title: 'Relazioni Geografiche'
	    		},{
	    			xtype: 'panel',
	    			title: 'Selezione'
	    		},{
	    			xtype: 'panel',
	    			title: 'Stampa'
	    		},{
	    			xtype: 'panel',
	    			title: 'Tematismi'
	    		},{
	    			xtype: 'panel',
	    			title: 'Impostazoni'
	    		}]
	    	}]
		});
		
		
		function showErrorMsg(){
	   		Ext.Msg.show({
				title: 'Attenzione', 
				msg: 'Non hai inserito niente',
				buttons: {
					ok: true
				}
			});	
	    };
	    
	    function showConfirmMsg(){
	    	Ext.Msg.show({
				title: 'Attenzione', 
				msg: 'Tutte le modifiche verranno perse, continuare?',
				buttons: {
					yes: true,
					no: true
				},
				fn: function(btn){
					if(btn == "yes"){
						this.map.destroyVectorFeauture();
					}
				}
			});
		}

		Ext.apply(this, {items: [mainPanel]});

		//this.subscribe('cmdb-init-class', this.disable, this);
		//this.subscribe('cmdb-new-card', this.disable, this);
		//this.subscribe('cmdb-load-card', this.cardLoaded.createDelegate(this, [this.map], true), this);
		//this.subscribe('cmdb-select-class', this.selectClass.createDelegate(this, [this.map], true), this);
	},

	cardLoaded: function(row, map){
		this.enable();
		var cardId = row.record.data.Id;
		var cardName = row.record.data.Code;
		this.map.setActualCardId(cardId);
		this.map.setActualCardName(cardName);
		//this.map.filterCard();
	},
	selectClass: function(eventParams, map){
		var classId = eventParams.classId;
		var className = eventParams.className;
		this.map.setActualClassId(classId);
		this.map.setActualClassName(className);
		//this.map.filterClass();
		
	}*/
});
Ext.reg('cardmaptab', CMDBuild.Management.CardMapTab);