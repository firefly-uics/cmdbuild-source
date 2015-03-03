(function() {

	Ext.define('CMDBuild.controller.administration.workflow.CMProcessFormController', {
		extend: 'CMDBuild.controller.administration.classes.CMClassFormController',

		requires: ['CMDBuild.core.proxy.CMProxyWorkflow'],

		/**
		 * @param {CMDBuild.view.administration.workflow.CMProcessForm} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view.downloadXPDLSubitButton.on('click', this.onDownloadSubmitClick, this);
			this.view.uploadXPDLSubitButton.on('click', this.onUploadSubmitClick, this);
		},

		/**
		 * @return {Object} params
		 *
		 * @override
		 */
		buildSaveParams: function() {
			var params = this.callParent(arguments);

			params.isprocess = true;

			return params;
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * @override
		 */
		deleteSuccessCB: function(result, options, decodedResult) {
			var removedClassId = this.selection.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			_CMCache.onProcessDeleted(removedClassId);

			this.selection = null;

			// Accordion synchronization
			_CMMainViewportController.findAccordionByCMName('process').updateStore();
			_CMMainViewportController.findAccordionByCMName('process').selectFirstSelectableNode();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.onAddClassButtonClick();
			this.view.xpdlForm.hide();
		},

		onDownloadSubmitClick: function() {
			var version = this.view.versionCombo.getValue();
			var basicForm = this.view.xpdlForm.getForm();

			basicForm.standardSubmit = true;

			CMDBuild.core.proxy.CMProxyWorkflow.downloadSubmit(basicForm, version, {
				params: {
					idClass: this.selection.getId()
				}
			});
		},

		/**
		 * @param {Int} id - processId
		 */
		onProcessSelected: function(id) {
			this.selection = _CMCache.getProcessById(id);

			if (this.selection) {
				this.view.onClassSelected(this.selection);

				// Disable the XPDL fields if the process is a superclass
				if (this.selection.get('superclass')) {
					this.view.xpdlForm.hide();
				} else {
					this.view.xpdlForm.show();
				}

				// Fill the version combo
				CMDBuild.core.proxy.CMProxyWorkflow.getXpdlVersions({
					params: { idClass: id },
					scope: this,
					success: function(response, options, decodedResponse) {
						var versions = decodedResponse.response;
						var store = this.view.versionCombo.getStore();

						// FIX: store.removeAll(); doesn't work in this case so we use also loadData of null array without fire events
						store.removeAll();
						store.loadData([], false);

						for(var index in versions) {
							var value = versions[index];

							store.add({
								id: value,
								index: value
							});
						}

						store.add({
							id: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE,
							index: 0
						});

						store.sort([
							{
								property : CMDBuild.core.proxy.CMProxyConstants.INDEX,
								direction: 'DESC'
							}
						]);

						this.view.versionCombo.setValue(store.getAt(0).getId());
					}
				});
			}
		},

		onUploadSubmitClick: function() {
			var basicForm = this.view.xpdlForm.getForm();
			basicForm.standardSubmit = false;

			CMDBuild.LoadMask.get().show();

			CMDBuild.core.proxy.CMProxyWorkflow.xpdlUpload({
				form: basicForm,
				params: {
					idClass: this.selection.getId()
				},
				scope: this,
				success: function(form, action) {
					CMDBuild.LoadMask.get().hide();

					var messages = (Ext.decode(action.response.responseText) || {}).response;
					if (messages && messages.length > 0) {
						var msg = '';

						for (var i = 0; i < messages.length; ++i)
							msg += '<p>' + CMDBuild.Translation.administration.modWorkflow.xpdlUpload[messages[i]] + '<p>';

						CMDBuild.Msg.info(CMDBuild.Translation.common.success, msg);
					}
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();

					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.administration.modWorkflow.xpdlUpload.error, true);
				}
			});
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * @override
		 */
		saveSuccessCB: function(result, options, decodedResult) {
			var savedProcessData = decodedResult[CMDBuild.core.proxy.CMProxyConstants.TABLE];
			this.selection = _CMCache.onProcessSaved(savedProcessData);

			// Accordion synchronization
			_CMMainViewportController.findAccordionByCMName('process').updateStore();
			_CMMainViewportController.findAccordionByCMName('process').selectNodeById(savedProcessData[CMDBuild.core.proxy.CMProxyConstants.ID]);
		}
	});

})();