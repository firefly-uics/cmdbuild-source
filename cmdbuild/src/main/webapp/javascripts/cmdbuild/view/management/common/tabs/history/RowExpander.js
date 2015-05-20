(function() {

	Ext.define('CMDBuild.view.management.common.tabs.history.RowExpander', {
		extend: 'Ext.grid.plugin.RowExpander',

		expandOnEnter: false,

		// XTemplate formats all values to an array of key-value objects before display
		rowBodyTpl: new Ext.XTemplate(
			'<tpl exec="this.formatter(' + CMDBuild.core.proxy.CMProxyConstants.VALUES + ')"></tpl>',
			'<tpl for="this.formattedArray">',
				'<tpl if="changed">',
					'<p class="changed">',
				'<tpl else>',
					'<p>',
				'</tpl>',
				'<b>{key}:</b> {value}</p>',
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
								key: key,
								value: value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
								changed: value.changed
							});
						}, this);
					}
				}
			}
		)
	});

})();