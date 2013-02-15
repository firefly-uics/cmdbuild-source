(function() {
	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMFormFiledsManager",

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		build: function() {
			this.name = new Ext.form.TextField({
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: _CMProxy.parameter.NAME,
				allowBlank: false,
				vtype: "alphanum",
				cmImmutable: true
			});

			this.description= new Ext.form.TextField({
				fieldLabel : CMDBuild.Translation.administration.modClass.attributeProperties.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : _CMProxy.parameter.DESCRIPTION,
				allowBlank : false,
				vtype : "cmdbcomment"
			});

			return [this.name, this.description];
		}
	});
})();