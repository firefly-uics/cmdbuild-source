(function() {

	Ext.define('CMDBuild.view.management.common.tabs.history.RowExpander', {
		extend: 'Ext.grid.plugin.RowExpander',

		expandOnEnter: false,

		// XTemplate formats all values to an array of key-value objects before display
		rowBodyTpl: new Ext.XTemplate(
			'<tpl exec="this.formatter(' + CMDBuild.core.proxy.CMProxyConstants.VALUES + ')"></tpl>',
			'<tpl for="this.formattedArray">',
				'<tpl if="' + CMDBuild.core.proxy.CMProxyConstants.CHANGED + '">',
					'<p class="' + CMDBuild.core.proxy.CMProxyConstants.CHANGED + '">',
				'<tpl else>',
					'<p>',
				'</tpl>',
				'<b>{attribute}:</b> {value}</p>',
			'</tpl>',
			{
				/**
				 * @param {Object} values
				 */
				formatter: function(values){
					if (!Ext.isEmpty(values)) {
						this.formattedArray = [];

						Ext.Object.each(values, function(key, value, myself) {
							this.formattedArray.push({
								attribute: value[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] || key,
								value: value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
								changed: value[CMDBuild.core.proxy.CMProxyConstants.CHANGED]
							});
						}, this);
					}
				}
			}
		)
	});

})();