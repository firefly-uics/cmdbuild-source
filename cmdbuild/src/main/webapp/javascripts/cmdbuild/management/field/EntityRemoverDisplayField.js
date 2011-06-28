CMDBuild.Management.EntityRemoverDisplayField = Ext.extend(Ext.form.DisplayField, {
	setRawValue : function(v) {
		var nv;
		if (this.htmlEncode) {
			nv = v;
	    } else {
	    	nv = CMDBuild.Utils.Format.htmlEntityEncode(v);
	    }
		return CMDBuild.Management.EntityRemoverDisplayField.superclass.setRawValue.call(this, nv);
   	}
});
