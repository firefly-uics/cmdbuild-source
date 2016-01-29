(function() {

	var tr = CMDBuild.Translation.management.modreport.reportForm;

	Ext.define('CMDBuild.view.administration.report.CMReportGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Report',
			'CMDBuild.model.Report'
		],

		filtering: false,
		reportType: '',

		layout: {
			type: 'fit',
			reserveScrollbar: true // There will be a gap even when there's no scrollbar
		},

		initComponent: function() {
			var gridColumns = [
				{
					header: 'Id',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.ID,
					hidden: true
				},
				{
					header: 'Query',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.QUERY,
					hidden: true
				},
				{
					header: tr.name,
					sortable: true,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.TITLE,
					flex: 1
				},
				{
					header: tr.description,
					sortable: true,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					flex: 1
				},
				{
					header: tr.report,
					sortable: false,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.TYPE,
					width: 60,
					fixed: true,
					tdCls: 'grid-button',
					renderer: this.loadReportIcons,
					scope: this,
					menuDisabled: true,
					hideable: false
				}
			];

			this.store = CMDBuild.core.proxy.Report.getStore();

			this.pagingBar = Ext.create('Ext.toolbar.Paging', {
				store: this.store,
				displayInfo: true,
				displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
				emptyMsg: CMDBuild.Translation.common.display_topic_none
			});

			Ext.apply(this, {
				bbar: this.pagingBar,
				columns: gridColumns
			});

			this.callParent(arguments);

			this.on('beforeitemclick', this.cellclickHandler);
		},

		/**
		 * Block the report panel to front if selected from the navigation menu
		 *
		 * @param {Object} selection
		 *
		 * @return {Boolean}
		 */
		beforeBringToFront : function(selection) {
			if (selection) {
				var r = selection.raw || selection.data;

				return !(r && r.objid);
			}
		},

		/**
		 * @param {Ext.grid.View} grid
		 * @param {CMDBuild.model.Report.grid} model
		 * @param {String} htmlelement
		 * @param {Int} rowIndex
		 * @param {Ext.EventObjectImpl} event
		 */
		cellclickHandler: function(grid, model, htmlelement, rowIndex, event) {
			var reportExtension = event.target.className;

			if (reportExtension == 'pdf' || reportExtension == 'csv' || reportExtension == 'odt' || reportExtension == 'zip' || reportExtension == 'rtf') {
				this.requestReport({
					id: model.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					type: model.get(CMDBuild.core.proxy.CMProxyConstants.TYPE),
					extension: reportExtension
				});
			} else if (reportExtension == 'sql') {
				var win = Ext.create('CMDBuild.core.PopupWindow', {
					title: 'Sql',
					items: [{
						xtype: 'panel',
						autoScroll: true,
						html: '<pre style="padding:5px; font-size: 1.2em">' + model.get(CMDBuild.core.proxy.CMProxyConstants.QUERY) + '</pre>'
					}],
					buttons: [{
						text: CMDBuild.Translation.common.buttons.close,

						handler: function() {
							win.destroy();
						}
					}]
				}).show();
			}
		},

		clearSelections: function() {
			this.getSelectionModel().clearSelections();
		},

		load: function() {
			this.getStore().load({
				scope: this,
				callback: function() {
					if (!this.getSelectionModel().hasSelection())
						this.getSelectionModel().select(0, true);
				}
			});
		},

		/**
		 * @param {String} reportType
		 *
		 * @return {String} html
		 */
		loadReportIcons: function(reportType) {
			if(reportType == 'CUSTOM') {
				var html ='<div class="cmcenter">';
				html += '<img qtip="Sql" style="cursor:pointer" class="sql" src="images/icons/ico_sql.png"/>&nbsp;&nbsp;'; // TODO: refactor and delete image file
				html += '<img qtip="Zip" style="cursor:pointer" class="zip" src="images/icons/ico_zip.png"/>&nbsp;&nbsp;'; // TODO: refactor and delete image file
				html += '</div>';

				return html;
			} else {
				// TODO: OpenOffice
			}
		},

		/**
		 * @param {Mixin} report
		 */
		onReportTypeSelected: function(report) {
			this.load();
		},

		/**
		 * @params {Object} reportParams
		 * 		ex. {
		 * 			{Int} id,
		 * 			{String} type,
		 * 			{String} extension
		 * 		}
		 */
		requestReport: function(reportParams) {
			CMDBuild.LoadMask.get().show();

			CMDBuild.core.proxy.Report.createReport({
				scope: this,
				params: reportParams,
				success: function(result, options, decodedResult) {
					CMDBuild.LoadMask.get().hide();

					if(decodedResult.filled) { // Report with no parameters
						var popup = window.open(
							'services/json/management/modreport/printreportfactory',
							'Report',
							'height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable'
						);

						if (!popup)
							CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					} else { // Show form with launch parameters
						Ext.create('CMDBuild.controller.management.report.Parameters', {
							attributeList: decodedResult.attribute
						});
					}
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});

})();