(function() {

	Ext.define('CMDBuild.view.management.common.tabs.history.RowExpander', {
		extend: 'Ext.grid.plugin.RowExpander',

		expandOnEnter: false,
		selectRowOnExpand: false,

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
			'<tpl if="this.formattedArray.length == 0">',
				'<p>' + CMDBuild.Translation.noAvailableData + '<p>',
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
								attribute: value.get(CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION) || key,
								changed: value.get(CMDBuild.core.proxy.CMProxyConstants.CHANGED),
								index: value.get(CMDBuild.core.proxy.CMProxyConstants.INDEX),
								value: value.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
							});
						}, this);

						// Sort by index value (CMDBuild attribute sort order)
						Ext.Array.sort(this.formattedArray, function(item1, item2) {
							if (item1[CMDBuild.core.proxy.CMProxyConstants.INDEX] < item2[CMDBuild.core.proxy.CMProxyConstants.INDEX])
								return -1;

							if (item1[CMDBuild.core.proxy.CMProxyConstants.INDEX] > item2[CMDBuild.core.proxy.CMProxyConstants.INDEX])
								return 1;

							return 0;
						}, this);
					}
				}
			}
		)
	});

})();