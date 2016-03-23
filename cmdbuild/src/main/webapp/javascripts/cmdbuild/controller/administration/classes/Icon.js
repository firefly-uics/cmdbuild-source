(function () {

	Ext.define('CMDBuild.controller.administration.classes.Icon', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.classes.Icon'
		],

		/**
		 * @property {Object}
		 */
		classIconObject: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onClassPropertiesIconClassSelected',
			'onClassPropertiesIconUploadButtonClick'
		],

		/**
		 * @cfg {CMDBuild.view.administration.classes.CMClassForm}
		 */
		parentForm: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		selectedClassModel: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.class.IconForm}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.administration.class.IconForm} configurationObject.view
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			// Shorthands
			this.parentForm = this.view.parentForm;
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} selectedClassModel
		 */
		onClassPropertiesIconClassSelected: function (selectedClassModel) {
			if (
				Ext.isObject(selectedClassModel) && !Ext.Object.isEmpty(selectedClassModel)
				&& Ext.isFunction(selectedClassModel.get)
			) {
				this.selectedClassModel = selectedClassModel;

				this.view.imageIconDisplayField.setSrc(); // Field reset

				// Find icon definition object
				CMDBuild.core.proxy.classes.Icon.readAllIcons({
					scope: this,
					failure: function (response, options, decodedResponse) {
						_error('readAll failure', this);

						CMDBuild.core.LoadMask.hide();
					},
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						this.classIconObject = {};

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							this.classIconObject = Ext.Array.findBy(decodedResponse, function (iconObject, i, allIconObjects) {
								return iconObject.details.id == this.selectedClassModel.get(CMDBuild.core.constants.Proxy.NAME);
							}, this);

							if (!Ext.isEmpty(this.classIconObject)) {
								// Download and display
								this.view.imageIconDisplayField.setSrc(
									'services/rest/v2/filestores/images/folders/'
									+ this.classIconObject.image.details.folder + '/files/'
									+ this.classIconObject.image.details.file + '/download?'
									+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY + '=' + Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN)
								);

								CMDBuild.core.LoadMask.hide();
							}
						}
					}
				});
			} else {
				_error('wrong selectedClassModel parameter on icon form class selected method', this);
			}
		},

		onClassPropertiesIconUploadButtonClick: function () {
			var targetFolderModel = null;

			if (Ext.Object.isEmpty(this.classIconObject)) {
				this.uploadImageAndBindIcon();
			} else { // Delete icon after upload new one
				this.deleteImageAndIcon(this.uploadImageAndBindIcon);
			}
		},

		/**
		 * @param {Function} callback
		 *
		 * @private
		 */
		deleteImageAndIcon: function (callback) {
			CMDBuild.core.proxy.classes.Icon.remove({
				urlParams: {
					iconId: this.classIconObject._id,
					folderId: this.classIconObject.image.details.folder,
					imageId: this.classIconObject.image.details.file
				},
				scope: this,
				callback: callback
			});
		},

		/**
		 * @private
		 */
		uploadImageAndBindIcon: function () {
			// Build target folder model
			CMDBuild.core.LoadMask.show();
			CMDBuild.core.proxy.classes.Icon.getFolders({
				scope: this,
				failure: function (response, options, decodedResponse) {
					_error('read folders failure', this);

					CMDBuild.core.LoadMask.hide();
				},
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
						targetFolderModel = Ext.create('CMDBuild.model.classes.icon.Folder',
							Ext.Array.findBy(decodedResponse, function (folderObject, i, allFolderObjects) {
								return Ext.isEmpty(folderObject[CMDBuild.core.constants.Proxy.PARENT]);
							}, this)
						);

					// Upload image
					if (Ext.isObject(targetFolderModel) && !Ext.Object.isEmpty(targetFolderModel)) {
						var params = {};
						params['fileStore'] = 'images';
						params['folder'] = targetFolderModel.get('_id');

						CMDBuild.core.proxy.classes.Icon.createImage({
							form: this.view.getForm(),
							params: params,
							scope: this,
							failure: function (response, options, decodedResponse) {
								_error('upload failure', this);

								CMDBuild.core.LoadMask.hide();
							},
							success: function (response, options, decodedResponse) {
								var uploadedIconName = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

								if (!Ext.isEmpty(uploadedIconName) && Ext.isString(uploadedIconName)) {

									// Update uploaded image with class bind
									CMDBuild.core.proxy.classes.Icon.update({
										params: {
											type: 'class',
											details: {
												id: this.selectedClassModel.get('name'),
											},
											image: {
												type: 'filestore',
												details: {
													store: 'filestore',
													folder: targetFolderModel.get('_id'),
													file: uploadedIconName
												}
											}
										},
										scope: this,
										failure: function (response, options, decodedResponse) {
											_error('readAll failure', this);

											CMDBuild.core.LoadMask.hide();
										},
										success: function (response, options, decodedResponse) {
											CMDBuild.core.LoadMask.hide();
										}
									});
								} else {
									_error('uploaded icon identifier error "' + uploadedIconName + '"', this);
								}
							}
						});
					}
				}
			});
		}
	});

})();
