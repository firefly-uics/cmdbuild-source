(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowConfirmRegeneration', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.widget.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @cfg {Number}
		 */
		defaultSizeW: 0.80,

		/**
		 * @cfg {Number}
		 */
		defaultSizeH: 0.50,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {Array}
		 */
		records: undefined,

		buttonAlign: 'center',
		title: '@@ Confirm regeneration',
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('Ext.grid.Panel', {
				region: 'center',
				autoScroll: true,
				border: false,
				collapsible: false,
				frame: false,

				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ID,
						hidden: true
					},
//					{
//						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
//						hidden: true
//					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						hidden: true,
						sortable: true
					},
					{
						text: CMDBuild.Translation.archivingDate,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DATE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.address,
						sortable: false,
						scope: this,
						renderer: this.renderAddress,
						flex: 1
					},
					{
						text: CMDBuild.Translation.subject,
						sortable: false,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						flex: 1
					},
					{
						sortable: false,
						scope: this,
						renderer: this.renderEmailContent,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
						menuDisabled: true,
						hideable: false,
						flex: 2
					},
					{
						xtype: 'checkcolumn',
						text: '@@ Enable regeneration',
						dataIndex: '@@ enableRegeneration',
						width: 120,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					}
				],

				features: [
					{
						ftype: 'groupingsummary',
						groupHeaderTpl: [
							'{name:this.formatName}',
							{
								formatName: function(name) {
									return CMDBuild.Translation.emailLookupNames[name] || name;
								}
							}
						],
						hideGroupedHeader: true,
						enableGroupingMenu: false
					}
				],

				plugins: [
					{
						ptype: 'rowexpander',
						rowBodyTpl: new Ext.XTemplate(
							'<p><b>Subject:</b> {subject}</p>',
							'<p><b>Content:</b> {content}</p>'
						)
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.widget.ManageEmail.email',
					data: this.records || [],
					sorters: {
						property: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						direction: 'ASC'
					},
					groupField: CMDBuild.core.proxy.CMProxyConstants.STATUS
				})
			});

			Ext.apply(this, {
				items: [this.grid],
				buttons: [
					Ext.create('CMDBuild.buttons.ConfirmButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowConfirmRegenerationConfirmButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowConfirmRegenerationAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;

_debug('this.grid.getStore()', this.grid.getStore());
		},

		// Column renderers
			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 *
			 * @return {String}
			 */
			renderAddress: function(value, metadata, record) {
				if (this.delegate.recordIsReceived(record)) { // TODO: cambiare perchè andrà in un controller genitore
					return record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS);
				} else {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);
				}
			},

			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 *
			 * @return {String}
			 */
			renderEmailContent: function(value, metadata, record) {
				return Ext.util.Format.stripTags(record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT));
			}
	});

})();
