CMDBuild.Administration.ModUser= Ext.extend(CMDBuild.ModPanel, {

  id: 'moduser',
  translation: CMDBuild.Translation.administration.modsecurity,

  initComponent: function() {
    
  CMDBuild.log.info("init group module");
		
	/** ACTIONS **/
    var addUserAction = new Ext.Action({
          iconCls:'add',
          text: this.translation.user.add_user,
          handler : function() {
			this.publish('cmdb-new-user', {
					userId: -1
			});
      	  },
      	  position: 'left',
      	  scope : this
    });
    
    /** COMPONENTS **/
    this.subscribe('cmdb-select-user', this.selectUser, this);
    
    Ext.apply(this,{
    		modtype: 'user',
			tbar:[addUserAction],
      		title : this.translation.user.title,
      		basetitle : this.translation.user.title+ ' - ',
      		layout: 'border',
      		id : this.id + '_panel',
      		items: [{
		        	id: 'usergrid',
		        	xtype: 'usergrid',
		        	region: 'center',
		        	style: {'border-bottom':'1px '+CMDBuild.Constants.colors.gray.border+' solid'}
		    	},{
		    		id: 'userform',
					xtype: 'userform',
					height: '60%',
		    		frame: false,
					border: false,						    	
					region: 'south',
					autoScroll:true,
					split:true,
					style: {'border-top':'1px '+CMDBuild.Constants.colors.gray.border+' solid'}
      		    }]
    }); 

    CMDBuild.Administration.ModUser.superclass.initComponent.apply(this, arguments);  
    },
   
  	/**EVENT  FUNCTIONS **/
	selectUser: function(eventParams) {
		if (eventParams) {
			this.publish('cmdb-init-user');
		}
    }
  
});
