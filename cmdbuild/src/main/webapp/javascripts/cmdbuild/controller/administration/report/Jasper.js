(function() {

	Ext.define('CMDBuild.controller.administration.report.Jasper', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.report.Jasper',
			'CMDBuild.model.report.Grid',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.report.Report}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportsJasperAbortButtonClick',
			'onReportsJasperAddButtonClick',
			'onReportsJasperGenerateSqlButtonClick',
			'onReportsJasperGenerateZipButtonClick',
			'onReportsJasperModifyButtonClick = onReportsJasperItemDoubleClick',
			'onReportsJasperRemoveButtonClick',
			'onReportsJasperRowSelected',
			'onReportsJasperSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.report.jasper.form.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {Array}
		 */
		fieldsets: [],

		/**
		 * @property {CMDBuild.view.administration.report.jasper.GridPanel}
		 */
		grid: undefined,

		/**
		 * Contains all images required from report
		 *
		 * @property {Array}
		 */
		images: [],

		/**
		 * @property {CMDBuild.model.report.Grid}
		 */
		selectedReport: undefined,

		/**
		 * Contains all sub-reports required from report
		 *
		 * @property {Array}
		 */
		subreports: [],

		/**
		 * @property {CMDBuild.view.administration.report.jasper.JasperView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.report.Report} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.report.jasper.JasperView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		import: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.report.Jasper.import({
				form: this.form.step2Panel,
				scope: this,
				failure: function(form, action) {
					CMDBuild.LoadMask.get().hide();

					CMDBuild.core.Message.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.reportsImportError, false);
				},
				success: this.success
			});
		},

		onReportsJasperAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedReport)) {
				this.onReportsJasperRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onReportsJasperAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedReport = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.report.Grid'));
		},

		/**
		 * Build SQL display window
		 *
		 * @param {CMDBuild.model.report.Grid} record
		 */
		onReportsJasperGenerateSqlButtonClick: function(record) {
			var sqlWindow = Ext.create('CMDBuild.core.PopupWindow', {
				autoScroll: true,
				border: false,
				frame: true,
				title: CMDBuild.Translation.sql,

				items: [
					Ext.create('Ext.form.field.TextArea', {
						border: false,
						frame: false,
						readOnly: true,

						value: record.get(CMDBuild.core.constants.Proxy.QUERY)
					})
				],

				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Close', {
								handler: function(button, e) {
									sqlWindow.destroy();
								}
							})
						]
					})
				]
			}).show();
		},

		/**
		 * Creates and forces download of ZIP report's file
		 *
		 * @param {CMDBuild.model.report.Grid} record
		 */
		onReportsJasperGenerateZipButtonClick: function(record) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.TYPE] = record.get(CMDBuild.core.constants.Proxy.TYPE);
			params[CMDBuild.core.constants.Proxy.EXTENSION] = CMDBuild.core.constants.Proxy.ZIP;

			CMDBuild.core.proxy.report.Jasper.create({
				params: params,
				scope: this,
				success: function(result, options, decodedResult) {
					params = {};
					params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

					var form = Ext.create('Ext.form.Panel', {
						standardSubmit: true,
						url: CMDBuild.core.proxy.Index.report.printReportFactory
					});

					form.submit({
						target: '_blank',
						params: params
					});

					Ext.defer(function() { // Form cleanup
						form.close();
					}, 100);
				}
			});
		},

		onReportsJasperModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onReportsJasperRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * TODO: waiting for refactor (crud)
		 */
		onReportsJasperRowSelected: function() {
			this.selectedReport = this.grid.getSelectionModel().getSelection()[0];

			this.form.reset();

			// Step 1
			this.form.step1Panel.fileField.allowBlank = true; // If we edit report file upload is not mandatory

			// Step 2
			this.form.step2Panel.removeAll();

			this.form.getLayout().setActiveItem(0);
			this.form.loadRecord(this.selectedReport);

			this.form.setDisabledModify(true, true);
		},

		onReportsJasperSaveButtonClick: function() {
			if (this.form.getLayout().getActiveItem() == this.form.step1Panel) { // We are on step1
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.form.step1Panel.name.getValue(); // TODO: waiting for refactor (read title parameter, write as name)
				params[CMDBuild.core.constants.Proxy.REPORT_ID] = this.form.step1Panel.reportId.getValue() || -1; // TODO: waiting for refactor (read id parameter, write as reportId)

				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.report.Jasper.analize({
					form: this.form.step1Panel,
					params: params,
					scope: this,
					failure: function(form, action) {
						CMDBuild.LoadMask.get().hide();

						CMDBuild.core.Message.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.reportsAnalizeError, false);
					},
					success: function(form, action) {
						CMDBuild.LoadMask.get().hide();

						if (Ext.isBoolean(action.result.skipSecondStep) && action.result.skipSecondStep) {
							CMDBuild.core.proxy.report.Jasper.save({
								scope: this,
								success: this.success
							});
						} else {
							this.setFormDetails(action.result);

							if (Ext.isEmpty(action.result.images) && Ext.isEmpty(action.result.subreports)) {
								this.import();
							} else {
								this.form.getLayout().setActiveItem(1);
							}
						}
					}
				});
			} else {
				this.import();
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedReport)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedReport.get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.report.Jasper.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();
						this.form.getLayout().setActiveItem(0);

						// Reset server session
						CMDBuild.core.proxy.report.Jasper.resetSession({
							scope: this,
							success: function(response, options, decodedResponse) {
								this.form.getLayout().setActiveItem(0);
							}
						});

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								if (success) {
									this.grid.getSelectionModel().select(0, true);

									// If no selections disable all UI
									if (!this.grid.getSelectionModel().hasSelection())
										this.form.setDisabledModify(true, true, true, true);

									_CMCache.reloadReportStores();
								}
							}
						});
					}
				});
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					CMDBuild.LoadMask.get().hide();

					if (success) {
						me.form.step2Panel.removeAll();

						// Reset server session
						CMDBuild.core.proxy.report.Jasper.resetSession({
							scope: me,
							success: function(response, options, decodedResponse) {
								me.form.getLayout().setActiveItem(0);
							}
						});

						var rowIndex = this.find(
							CMDBuild.core.constants.Proxy.NAME,
							me.form.step1Panel.name.getValue()
						);

						me.grid.getSelectionModel().select(rowIndex, true);
						me.form.setDisabledModify(true);

						_CMCache.reloadReportStores();

						CMDBuild.view.common.field.translatable.Utils.commit(me.form.step1Panel);
					}
				}
			});
		},

		// Step 2 methods
			/**
			 * @param {Object} results
			 *
			 * TODO: this functionality should be refactored because it's not so safe to apply server response like this way
			 */
			setFormDetails: function(results) {
				delete results.success; // Delete unwanted properties

				this.duplicateimages = false; // Because it was overridden by the apply only if is true
				this.skipSecondStep = false;
				this.fieldsets = [];

				Ext.apply(this, results); // Apply images, subreports arrays

				this.form.step2Panel.removeAll();

				if (this.duplicateimages) { // TODO: Something unknown
					this.fieldsets.push(
						Ext.create('Ext.form.FieldSet', {
							title: CMDBuild.Translation.importJasperReport,
							autoHeight: true,

							items: [
								{ xtype: 'label', text: CMDBuild.Translation.warnings.reportsDuplicateImageFilename }
							]
						})
					);
				} else { // Show form
					this.buildFields(this.images, 'image');
					this.buildFields(this.subreports, 'subreport');
				}
			},

			/**
			 * @param {Array} refer
			 * @param {String} namePrefix
			 */
			buildFields: function(refer, namePrefix) {
				if (!Ext.isEmpty(refer) && Ext.isArray(refer)) {
					Ext.Array.forEach(refer, function(image, i, allImages) {
						if (!Ext.isEmpty(image[CMDBuild.core.constants.Proxy.NAME])) {
							this.form.step2Panel.add(
								Ext.create('Ext.form.field.File', {
									name: namePrefix + i,
									fieldLabel: image[CMDBuild.core.constants.Proxy.NAME],
									labelWidth: CMDBuild.LABEL_WIDTH,
									maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
									allowBlank: true
								})
							);
						} else {
							_error('import report step 2: ' + image + 'has not name attribute', this);
						}
					}, this);
				}
			}
	});

})();