(function() {

	Ext.define('CMDBuild.controller.administration.domain.CMDomainAttributesController', {
		extend: 'CMDBuild.controller.administration.CMBaseAttributesController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @property {Object}
		 */
		currentAttribute: undefined,

		/**
		 * @property {CMDBuild.cache.CMDomainModel}
		 */
		currentDomain: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.CMDomainAttributeFormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.CMDomainAttributeGrid}
		 */
		grid: undefined,

		/**
		 * @property {Ext.selection.RowModel}
		 */
		gridSM: undefined,

		/**
		 * @property {CMDBuild.administration.domain.CMDomainAttribute}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.administration.domain.CMDomainAttribute} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.form = this.view.form;
			this.grid = this.view.grid;

			this.gridSM = this.grid.getSelectionModel();
			this.gridSM.on('selectionchange', this.onSelectionChanged, this);

			this.form.abortButton.on('click', this.onAbortButtonClick, this);
			this.form.saveButton.on('click', this.onSaveButtonClick, this);
			this.form.deleteButton.on('click', this.onDeleteButtonClick, this);
			this.grid.addAttributeButton.on('click', this.onAddAttributeClick, this);
		},

		/**
		 * Cache synch
		 *
		 * @param {Array} savedAttributes
		 */
		anAttributeWasMoved: function(savedAttributes) {
			if (!Ext.Object.isEmpty(this.currentDomain) && !Ext.isEmpty(savedAttributes)) {
				var oldAttributes = this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES);

				for (var i = 0; i < savedAttributes.length; ++i) {
					var newAttr = savedAttributes[i];

					for (var j = 0; j < oldAttributes.length; ++j) {
						var oldAttr = oldAttributes[j];

						if (oldAttr[CMDBuild.core.proxy.CMProxyConstants.NAME] == newAttr[CMDBuild.core.proxy.CMProxyConstants.NAME]) {
							oldAttr[CMDBuild.core.proxy.CMProxyConstants.INDEX] = newAttr[CMDBuild.core.proxy.CMProxyConstants.INDEX];

							break;
						}
					}
				}
			}
		},

		deleteAttribute: function() {
			if (!Ext.isEmpty(this.currentDomain) && !Ext.isEmpty(this.currentAttribute)) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.ServiceProxy.administration.domain.attribute.remove({
					params: {
						className: this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						name: this.currentAttribute.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					},
					scope: this,
					success: function(result, options, decodedResult) {
						this.form.reset();

						_CMCache.onDomainAttributeDelete(
							this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID),
							this.currentAttribute[CMDBuild.core.proxy.CMProxyConstants.DATA]
						);

						this.currentAttribute = null;
					},
					callback: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		/**
		 * @override
		 */
		getGrid: function() {
			return this.view.grid;
		},

		/**
		 * @override
		 */
		getCurrentEntryTypeId: function() {
			return this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID);
		},

		onAbortButtonClick: function() {
			if (Ext.isEmpty(this.currentAttribute)) {
				this.form.disableModify();
				this.form.reset();
			} else {
				this.form.onAttributeSelected(this.currentAttribute);
			}
		},

		onAddAttributeClick: function() {
			this.currentAttribute = null;
			this.view.onAddAttributeClick();

			_CMCache.initAddingTranslations();
		},

		onAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * @override
		 */
		onAttributeMoved: function() {
			var me = this;
			var attributes = [];
			var store = this.grid.getStore();

			for (var i = 0; i < store.getCount(); i++) {
				var rec = store.getAt(i);

				var attribute = {};
				attribute[CMDBuild.core.proxy.CMProxyConstants.NAME] = rec.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				attribute[CMDBuild.core.proxy.CMProxyConstants.INDEX] = i + 1;

				attributes.push(attribute);
			}

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES] = Ext.JSON.encode(attributes);
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getDomainNameById(this.getCurrentEntryTypeId());

			CMDBuild.ServiceProxy.attributes.reorder({
				params: params,
				success: function() {
					me.anAttributeWasMoved(attributes);
				}
			});
		},

		onDeleteButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.administration.modClass.attributeProperties.delete_attribute,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						this.deleteAttribute();
					}
				}
			});
		},

		/**
		 * @params {CMDBuild.cache.CMDomainModel} domain
		 */
		onDomainSelected: function(domain) {
			this.currentDomain = domain;
			this.view.onDomainSelected(domain);
		},

		onSaveButtonClick: function() {
			var nonValid = this.form.getNonValidFields();
			var data = this.form.getData(true);

			data[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);

			if (nonValid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				return;
			}

			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.administration.domain.attribute.save({
				params: data,
				scope: this,
				success: function(result, options, decodedResult) {
					var attribute = decodedResult.attribute;

					this.currentAttribute = null;
					this.form.disableModify();

					_CMCache.onDomainAttributeSaved(this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID), attribute);

					this.grid.selectAttributeByName(attribute[CMDBuild.core.proxy.CMProxyConstants.NAME]);

					_CMCache.flushTranslationsToSave(
						this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						attribute[CMDBuild.core.proxy.CMProxyConstants.NAME]
					);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		/**
		 * @param {Ext.selection.RowModel} selection
		 */
		onSelectionChanged: function(selection) {
			if (selection.selected.length > 0) {
				this.currentAttribute = selection.selected.items[0];
				this.form.onAttributeSelected(this.currentAttribute);
			}
		}
	});

})();