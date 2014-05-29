(function() {

	Ext.define('CMDBuild.core.buttons.AddRelationMenuButton', {
		alternateClassName: 'CMDBuild.AddRelationMenuButton', // Legacy class name
		extend: 'Ext.button.Split',

		iconCls: 'add',

		// Custom fields
		baseText: CMDBuild.Translation.management.modcard.add_relations,
		textPrefix: CMDBuild.Translation.management.modcard.add_relations,

		//private
		initComponent: function() {
			Ext.apply(this, {
				text: this.baseText,
				menu: {
					items: []
				},
				handler: onClick,
				scope: this
			});

			this.callParent(arguments);
		},

		setDomainsForEntryType: function(et, singleDomainId) {
			if (!Ext.isEmpty(et)) {
				var d;
				var domains = _CMCache.getDirectedDomainsByEntryType(et);
				var empty = true;
				var addAll = (typeof singleDomainId == 'undefined');

				this.menu.removeAll();

				for (var i = 0, l = domains.length; i < l; ++i) {
					d = domains[i];

					if (
						!Ext.isEmpty(et)
						&& (
							addAll
							|| d.dom_id == singleDomainId
						)
					) {
						var cachedDomain = _CMCache.getDomainById(d.dom_id);

						if (cachedDomain.hasCreatePrivileges()) {
							this.menu.add({
								text: d.description,
								domain: d,
								scope: this,
								handler: function(item, e){
									this.fireEvent('cmClick', item.domain);
								}
							});

							empty = false;
						}
					}
				}

				this.setDisabled(empty);

				// Add relations menu button sort
				this.menu.items.items.sort(function(a, b) {
					if (a.text < b.text)
						return -1;

					if (a.text > b.text)
						return 1;

					return 0;
				});

				return domains.length > 0;
			}

			return;
		},

		setTextSuffix: function(suffix) {
			this.setText(this.textPrefix + ' ' + suffix);
		},

		//private
		isEmpty: function(){
			return (this.menu.items.items.length == 0 );
		},

		//private
		resetText: function() {
			this.setText(this.baseText);
		}

	});

	/**
	 * Extjs calls the handler even when disabled
	 */
	function onClick() {
		if (!this.disabled)
			this.showMenu();
	}

})();