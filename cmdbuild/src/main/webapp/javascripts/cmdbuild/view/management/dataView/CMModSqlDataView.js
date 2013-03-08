(function() {

	var tr = CMDBuild.Translation.management.modcard;
	var UNDIPLAYABLE_ATTRIBUTES = ["id"];

	Ext.define("CMDBuild.view.management.dataView.CMModSQLDataViewDelegate", {

		/**
		 * @param {CMDBuild.view.management.dataView.CMModSQLDataView} panel
		 * the panel that calls the method
		 * @param {Ext.data.Model} record
		 */
		onModSQLDataViewGridSelected: function(panel, record) {}
	});

	Ext.define("CMDBuild.view.management.dataView.CMModSQLDataView", {
		extend: "CMDBuild.view.management.classes.CMModCard",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		cmName: "dataView",

		constructor: function() {
			this.mixins.delegable.constructor.call( //
					this, //
					"CMDBuild.view.management.dataView.CMModSQLDataViewDelegate" //
				);

			this.callParent(arguments);
		},

		// override
		buildComponents: function() {
			buildGrid(this);
			buildTabPanel(this);
		},

		configureGrid: function(store, columns) {
			this.cardGrid.reconfigure(store, columns);
			this.pagingBar.bindStore(store);
		},

		selectRecord: function(record) {
			if (record) {
				this.cardGrid.getSelectionModel().select(record);
			}
		},

		showRecordData: function(record) {
			var me = this;
			this.innerCardPanel.removeAll();
			record.fields.each(function(field) {
				var name = field.name;
				if (Ext.Array.contains(UNDIPLAYABLE_ATTRIBUTES, name)) {
					return;
				}

				addFieldToCardPanel(me, field, record.get(name));
			});
		}
	});

	function buildGrid(me) {
		var store = new Ext.data.Store({
			fields: [],
			data: []
		});

		me.cardGrid = new Ext.grid.Panel({
			tbar: getGridButtons(),
			hideMode: "offsets",
			border: false,
			bbar: buildGridPagingBar(me, store),
			store: store,
			columns: [],
			listeners: {
				select: function(grid, record, index) {
					me.callDelegates("onModSQLDataViewGridSelected", [me, record]);
				}
			}
		});

		me.pagingBar.add(new CMDBuild.field.GridSearchField({grid: me.cardGrid}));
		me.pagingBar.add(new CMDBuild.view.management.common.filter.CMFilterMenuButton({
			disabled: true
		}));
		me.pagingBar.add(new CMDBuild.PrintMenuButton({
			disabled: true
		}));
	}

	function addFieldToCardPanel(me, field, value) {
		var name = field.name;
		me.innerCardPanel.add( //
			new CMDBuild.view.common.field.CMDisplayField({
				disabled: false,
				fieldLabel: name,
				labelAlign: "right",
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: name,
				submitValue: false,
				style: {
					overflow: "hidden"
				},
				value: value,
				width: CMDBuild.BIG_FIELD_WIDTH
			}) //
		);
	}

	function buildTabPanel(me) {
		var gridratio = CMDBuild.Config.cmdbuild.grid_card_ratio || 50;

		me.innerCardPanel = new Ext.panel.Panel({
			flex: 1,
			framed: true,
			border: true,
			bodyCls: "x-panel-body-default-framed cmborder",
			bodyStyle: {
				padding: "5px 5px 0 5px"
			}
		});

		me.cardPanel = new Ext.panel.Panel({
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			bodyCls: "x-panel-body-default-framed cmbordertop",
			bodyStyle: {
				padding: "5px 5px 0 5px"
			},
			border: false,
			hideMode: "offsets",
			title: tr.tabs.card,
			tbar: getCardTabBarButtons(),
			buttonAlign: "center",
			buttons: getCardPanelButtons(),
			items: [me.innerCardPanel]
		});

		me.cardTabPanel = new Ext.tab.Panel({
			region: "south",
			hideMode: "offsets",
			border: false,
			split: true,
			height: gridratio + "%",
			bodyCls: "x-panel-body-default-framed cmbordertop",
			items: [ //
				me.cardPanel //
			, {
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
	}

	function buildGridPagingBar(me, store) {
		me.pagingBar = new Ext.toolbar.Paging({
			store: store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none,
			items: []
		});

		return me.pagingBar;
	}

	function getGridButtons() {
		return [{
			iconCls: 'add',
			text: CMDBuild.Translation.management.modcard.add_card,
			disabled: true
		}];
	}

	function getCardPanelButtons() {
		return [{
			text: CMDBuild.Translation.common.buttons.save,
			disabled: true
		}, {
			text: CMDBuild.Translation.common.btns.abort,
			disabled: true
		}];
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