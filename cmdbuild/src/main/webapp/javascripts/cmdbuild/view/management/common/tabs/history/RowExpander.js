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
				formatter: function(values) {
					if (!Ext.isEmpty(values)) {
						this.formattedArray = [];

						Ext.Object.each(values, function(key, value, myself) {
							this.formattedArray.push({
								attribute: value[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] || key,
								changed: value[CMDBuild.core.proxy.CMProxyConstants.CHANGED],
								index: value[CMDBuild.core.proxy.CMProxyConstants.INDEX],
								value: value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]
							});
						}, this);

						// Sort by index value (CMDBuild attribute sort order)
						CMDBuild.core.Utils.objectArraySort(this.formattedArray, CMDBuild.core.proxy.CMProxyConstants.INDEX);
					}
				}
			}
		)
	});

})();