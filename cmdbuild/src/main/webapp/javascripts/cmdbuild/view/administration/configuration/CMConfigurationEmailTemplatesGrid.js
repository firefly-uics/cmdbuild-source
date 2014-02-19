(function() {

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailTemplatesGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {

			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
						header: CMDBuild.Translation.description_,
						flex: 3
					},
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.SUBJECT,
						header: CMDBuild.Translation.subject,
						flex: 2
					}
				],
				store : CMDBuild.ServiceProxy.configuration.email.templates.getStore()
//				store: Ext.data.Store({
//					autoLoad: true,
//					fields: ['templateName', 'description', 'subject'],
//					data: {
//						'templates': [
//							{ templateName: 'Template 1', description: 'Description 1', subject: 'Subject 1' },
//							{ templateName: 'Template 2', description: 'Description 2', subject: 'Subject 2' },
//							{ templateName: 'Template 3', description: 'Description 3', subject: 'Subject 3' },
//							{ templateName: 'Template 4', description: 'Description 4', subject: 'Subject 4' }
//						]
//					},
//					proxy: {
//						type: 'memory',
//						reader: {
//							type: 'json',
//							root: 'templates'
//						}
//					}
//				})
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				}, null);
			},

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				var me = this;

				this.getStore().load( function() {
					me.getSelectionModel().select(0, true);
				});
			}
		}
	});

})();
