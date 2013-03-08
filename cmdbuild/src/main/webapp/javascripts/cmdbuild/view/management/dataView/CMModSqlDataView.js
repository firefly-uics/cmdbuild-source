(function() {

	var tr = CMDBuild.Translation.management.modcard;
	Ext.define("CMDBuild.view.management.dataView.CMModSQLDataViewDelegate", {
		
	});

	Ext.define("CMDBuild.view.management.dataView.CMModSQLDataView", {
		extend: "CMDBuild.view.management.classes.CMModCard",
	
		cmName: "dataView",
	
		initComponent: function() {
			this.title = "@@ SQL view";
			this.callParent(arguments);
		},
	
		buildComponents: function() {
			var gridratio = CMDBuild.Config.cmdbuild.grid_card_ratio || 50;

			var store = new Ext.data.Store({
				fields: [],
				data: []
			});

			this.cardGrid = new Ext.grid.Panel({
				tbar: getGridButtons(),
				hideMode: "offsets",
				border: false,
				bbar: buildGridPagingBar(this, store),
				store: store,
				columns: [{
					header: "@@ Cippa"
				}, {
					header: "@@ Lippa"
				}],
			});

			this.pagingBar.add(new CMDBuild.field.GridSearchField({grid: this.cardGrid}));
			this.pagingBar.add(new CMDBuild.view.management.common.filter.CMFilterMenuButton({
				disabled: true
			}));
			this.pagingBar.add(new CMDBuild.PrintMenuButton({
				disabled: true
			}));

			this.cardTabPanel = new Ext.tab.Panel({
				region: "south",
				hideMode: "offsets",
				border: false,
				split: true,
				height: gridratio + "%",
				items: [{
					title: tr.tabs.card,
					border: false,
					tbar: getCardTabBarButtons()
				}, {
					title: tr.tabs.detail,
					border: false,
					disabled: true
				}, {
					title: tr.tabs.notes,
					border: false,
					disabled: true
				}, {
					title: tr.tabs.relations,
					border: false,
					disabled: true
				}, {
					title: tr.tabs.history,
					border: false,
					disabled: true
				}, {
					title: tr.tabs.attachments,
					border: false,
					disabled: true
				}]
			});
		},

		configureGrid: function(store, columns) {
			this.cardGrid.reconfigure(store, columns);
			this.pagingBar.bindStore(store);
		}
	});

	function buildGridPagingBar(me, store) {
		me.pagingBar = new Ext.toolbar.Paging({
			store: store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none,
			items: [ //

			]
		});

		return me.pagingBar;
	}

	function getGridButtons() {
		return [{
			iconCls: 'add',
			text: CMDBuild.Translation.management.modcard.add_card,
			disabled: true
		}]
	}

	function getCardTabBarButtons() {
		var buttons = [{
			iconCls : "modify",
			text : tr.modify_card,
			disabled: true
		}, {
			iconCls : "delete",
			text : tr.delete_card,
			disabled: true
		}, {
			iconCls : "clone",
			text : tr.clone_card,
			disabled: true
		}, {
			iconCls : "graph",
			text : CMDBuild.Translation.management.graph.action,
			disabled: true
		},
			new CMDBuild.PrintMenuButton({
				text : CMDBuild.Translation.common.buttons.print+" "+CMDBuild.Translation.management.modcard.tabs.card.toLowerCase(),
				formatList: ["pdf", "odt"],
				disabled: true
			})
		];

		return buttons;
	}

})();