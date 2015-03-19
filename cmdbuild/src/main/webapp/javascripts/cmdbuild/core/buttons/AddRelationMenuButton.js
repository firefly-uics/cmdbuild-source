(function() {

	Ext.define('CMDBuild.core.buttons.AddRelationMenuButton', {
		extend: 'Ext.button.Split',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Domain'
		],

		iconCls: 'add',

		text: '@@ Add relations', // TODO CMDBuild.Translation.addRelations

		initComponent: function() {
			Ext.apply(this, {
				scope: this,
				menu: Ext.create('Ext.menu.Menu'),

				handler: function(button, e) {
					this.showMenu();
				},
			});

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		setDomainsForEntryType: function(entryType, singleDomainId) { // TODO: rename (setDomains)
			if (!Ext.isEmpty(entryType)) {
				this.menu.removeAll();

				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = entryType.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				params[CMDBuild.core.proxy.CMProxyConstants.WITH_DISABLED_CLASSES] = false;

				CMDBuild.core.proxy.Domain.getList({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						Ext.Array.forEach(decodedResult[CMDBuild.core.proxy.CMProxyConstants.DOMAINS], function(domain, i, allDomains) {
							if (_CMCache.isClassById(domain['class1id']) && _CMCache.isClassById(domain['class2id'])) {
								var domainCMObject = {};
								var originClass = _CMCache.getEntryTypeByName(domain['class1']);
								var destinationClass = _CMCache.getEntryTypeByName(domain['class2']);

								if (entryType.get(CMDBuild.core.proxy.CMProxyConstants.NAME) == domain['class1id']) {
									domainCMObject = { // TODO: use domain real object in future
										dom_id: domain['idDomain'],
										description: domain['descrdir'] + ' (' + destinationClass.get(CMDBuild.core.proxy.CMProxyConstants.TEXT) + ')',
										dst_cid: domain['class2id'],
										src_cid: domain['class1id'],
										src: '_1'
									};
								} else {
									domainCMObject = { // TODO: use domain real object in future
										dom_id: domain['idDomain'],
										description: domain['descrinv'] + ' (' + originClass.get(CMDBuild.core.proxy.CMProxyConstants.TEXT) + ')',
										dst_cid: domain['class1id'],
										src_cid: domain['class2id'],
										src: '_2'
									};
								}

								if (domain['priv_create']) // Add menu item only if i have create privileges
									this.menu.add({
										text: domainCMObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
										domain: domainCMObject,
										scope: this,

										handler: function(item, e){
											this.fireEvent('cmClick', item.domain);
										}
									});
							}
						}, this);

						// Disable button if there aren't available domains
						if (this.menu.items.getCount() == 0)
							this.setDisabled(true);

						// Ascending items sort
						this.menu.items.items.sort(function(a, b) {
							if (a.text < b.text)
								return -1;

							if (a.text > b.text)
								return 1;

							return 0;
						});
					}
				});
			}
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

})();