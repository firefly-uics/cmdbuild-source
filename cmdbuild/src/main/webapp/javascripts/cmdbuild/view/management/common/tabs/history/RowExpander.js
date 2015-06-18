(function() {

	Ext.define('CMDBuild.view.management.common.tabs.history.RowExpander', {
		extend: 'Ext.grid.plugin.RowExpander',

		expandOnEnter: false,

		// XTemplate formats all values to an array of key-value objects before display
		rowBodyTpl: new Ext.XTemplate(
			'<tpl exec="this.formatter(' + CMDBuild.core.proxy.Constants.VALUES + ')"></tpl>',
			'<tpl for="this.formattedArray">',
				'<tpl if="' + CMDBuild.core.proxy.Constants.CHANGED + '">',
					'<p class="' + CMDBuild.core.proxy.Constants.CHANGED + '">',
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
								attribute: value.get(CMDBuild.core.proxy.Constants.ATTRIBUTE_DESCRIPTION) || key,
								changed: value.get(CMDBuild.core.proxy.Constants.CHANGED),
								index: value.get(CMDBuild.core.proxy.Constants.INDEX),
								value: value.get(CMDBuild.core.proxy.Constants.DESCRIPTION)
							});
						}, this);

						// Sort by index value (CMDBuild attribute sort order)
						CMDBuild.core.Utils.objectArraySort(this.formattedArray, CMDBuild.core.proxy.Constants.INDEX);
					}
				}
			}
		)
	});

})();