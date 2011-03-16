Ext.form.BooleanDisplayField = Ext.extend(Ext.form.DisplayField,  {
	setRawValue : function(v){
       	if (this.rendered) {
    		v = CMDBuild.Utils.evalBoolean(v);
    		v = v ?  Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
    		if (this.htmlEncode) {
                v = Ext.util.Format.htmlEncode(v);
            }  
    		return this.el.dom.innerHTML = (Ext.isEmpty(v) ? '' : v);
       	} else {
       		return this.value = v;
       	}
    }
});
