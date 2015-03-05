(function() {
	var tr = CMDBuild.Translation.administration.setup.gis;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGis", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		alias: "widget.configuregis",
		configFileName: 'gis',

		initComponent: function() {
/*	This part was begun for the unification of the Gis navigation tree with the other navigation tree and is suspended
 * 	for the imminent release of the CMDBuild 2.2 version
 * 			var navigationTreesStore = buildNavigationTreesStore();
			this.navigationTreeName = new Ext.form.field.ComboBox({
				name: "navigationTreeName",
				fieldLabel: CMDBuild.Translation.tree_navigation,
				valueField: 'name',
				displayField: 'description',
				store: navigationTreesStore
			});
*/			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: tr.enable
			},{
				xtype: 'numberfield',
				name:'center.lat',
				decimalPrecision: 6,
				fieldLabel: tr.center_lat
			},{
				xtype: 'numberfield',
				name:'center.lon',
				decimalPrecision: 6,
				fieldLabel: tr.center_lon
			},{
				xtype: 'numberfield',
				name:'initialZoomLevel',
				fieldLabel: tr.initial_zoom,
				minValue : 0,
				maxValue : 25
			}/*,
			this.navigationTreeName */
		];

			this.callParent(arguments);
		},

		//override
		populateForm: function(configurationOptions) {
			this.callParent(arguments);
		},

		afterSubmit: function(conf) {
			CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, conf);
			CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

			if (CMDBuild.Config.gis.enabled) {
				_CMMainViewportController.enableAccordionByName("gis");
			} else {
				_CMMainViewportController.disableAccordionByName("gis");
			}
		}
	});
/*	This part was begun for the unification of the Gis navigation tree with the other navigation tree and is suspended
 * 	for the imminent release of the CMDBuild 2.2 version
 *
 * 	function buildNavigationTreesStore() {
		_CMCache.listNavigationTrees({
			success: function() {
				var navigationTrees = _CMCache.getNavigationTrees();
				var data = [];

				for (var i = 0; i < navigationTrees.data.length; i++) {
					var obj = navigationTrees.data[i];
					data.push({
						name: obj.name,
						description: obj.description
					});
				}
				var navigationTrees = Ext.create('Ext.data.Store', {
					fields: ['name', 'description'],
					data : data,
					autoLoad: true,
					sorters: [{
						sorterFn: function(o1, o2){
							return o1.get('description') <  o2.get('description') ? -1 : 1;
						}
					}],
				});
				return navigationTrees;
			}
		});

	}
*/
})();