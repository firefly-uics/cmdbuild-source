Ext.ns('CMDBuild.Msg');
CMDBuild.Msg.DetailList= [];

CMDBuild.Msg.buildDetailLink = function(stacktrace) {
	var id = CMDBuild.Msg.DetailList.length;
	CMDBuild.Msg.DetailList[id] = stacktrace;
	return '<br /><a id="detail_'+id+'" href="#" onClick="CMDBuild.Msg.buildDetaiWindow(this.id)">' + CMDBuild.Translation.errors.show_detail +'</a>';
};

CMDBuild.Msg.buildDetaiWindow = function(id) {
	var index = id.split("_")[1];
	var text = CMDBuild.Msg.DetailList[index];
	
	var closeHandler = function() {
		win.destroy();
	};
	
	var win = new CMDBuild.PopupWindow({
		title: CMDBuild.Translation.errors.detail,
		items: [{
			xtype: 'panel',
			autoScroll: true,			
			html: '<pre style="padding:5px; font-size: 1.2em">'+text+'</pre>'
		}],
		buttonAlign: 'center',
		buttons: [{
			text: CMDBuild.Translation.common.btns.close,
			handler: closeHandler
		}]
	}).show(); 
};

CMDBuild.Msg.alert = function(title, text, popup, iconCls) {
	var title = title || "&nbsp";
	var win;
	if (popup) {
		win = Ext.Msg.show({
     	   title: title,
     	   msg: text,
     	   width: 300,
     	   buttons: Ext.MessageBox.OK,
     	   icon: iconCls
     	});
	} else {
		win = new Ext.ux.Notification({
				iconCls: iconCls,
				title: title,
				html: text,
				autoDestroy: true,
				hideDelay:  5000,
				shadow: false
			}).show(document);
	}
	return win;
};

CMDBuild.Msg.success = function() {
	CMDBuild.Msg.alert("", CMDBuild.Translation.common.success, false, Ext.MessageBox.INFO);
};

CMDBuild.Msg.info = function(title, text, popup) {
	CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.INFO);
};

CMDBuild.Msg.error = function(title, body, popup) {
	var text = body;
	var title = title || CMDBuild.Translation.errors.error_message || "Error";
	if (typeof body == "object") {
		text = body.text;
		if (body.detail) {
			text += CMDBuild.Msg.buildDetailLink(body.detail);			
		}
	}
	var win = CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.ERROR);
	//TODO try to remove the stack-trace to the array CMDBuild.Msg.DetailList
	// there are some problems because the Ext.Msg.show object doesn't listen events
};

CMDBuild.Msg.warn = function(title, text, popup) {
	var title = title || CMDBuild.Translation.errors.warning_message || "Warning";
	CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.WARNING);
};
