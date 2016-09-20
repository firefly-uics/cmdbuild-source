(function () {
	Ext.define('CMDBuild.view.management.classes.map.CardGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.proxy.gis.Card'
		],

		/**
		 * @cfg {CMDBuild.controller.management.classes.map.CardGrid}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		map: undefined,

		/**
		 * @property {String}
		 */
		oldClassName : undefined,
		
		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent: function () {
			var store = CMDBuild.proxy.gis.Card.getStore();
			var thisGrid = this;
			var me = this;
			Ext.apply(this, {
			    dockedItems: [{
			        xtype: 'pagingtoolbar',
			        store: store,  
			        dock: 'bottom',
					displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
					emptyMsg: CMDBuild.Translation.common.display_topic_none,
			        displayInfo: true
			    }],
				columns: [
					{
						dataIndex: 'Code', 
						text: CMDBuild.Translation.code,
						flex: 1
					},
					{
						dataIndex: 'Description',
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.grid.column.Action', {
						text: "",
						align: 'center',
						width: 60,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.gis.Navigate', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.management.modcard.open_relation,
								scope: this,

								handler : function(grid, rowIndex, colIndex, actionItem,
											event, record, row) {
									me.zoomOnCard(record);
								},
								isDisabled : function(view, rowIdx, colIdx, item, record) {
									return false;
								}
							})
						]
					})
				],
				store: store
			});
			this.interactionDocument.observe(this);
			
			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onUserAndGroupUserItemDoubleClick');
			},

			select: function (row, record, index) {
				this.navigateOnCard(record);
			}
		},
		
		/**
		 * @param {Object} record
		 * @param {String} record.Id
		 * @param {String} record.IdClass
		 * 
		 * @returns {Void}
		 */
		navigateOnCard : function(record) {
			this.delegate.cmfg( 'onCardNavigation', {
				Id : record .get('Id'),
				IdClass : record .get('IdClass')
			});
		},
		zoomOnCard : function(record) {
			this.delegate.cmfg( 'onCardZoom', {
				Id : record .get('Id'),
				IdClass : record .get('IdClass')
			});
		},
		refresh : function() {
			var currentCard = this.interactionDocument.getCurrentCard();
			var currentClassName = currentCard.className;
			if (! currentClassName) {
				return;
			}
			this.store.proxy.setExtraParam("className", currentClassName);
			if (this.oldClassName !== currentClassName) {
				this.store.loadPage(1, {
				    scope: this,
				    callback: function(records, operation, success) {
				        this.getSelectionModel().select(0);
				    }
				});
				this.oldClassName = currentClassName;
			}
			else {
			this.store.load({
				scope: this,
				callback: function (records, operation, success) {
//					if (!this.getSelectionModel().hasSelection())
//						this.getSelectionModel().select(0, true);
				}
			});
			}
		}	
	});

})();
						