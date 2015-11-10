(function() {

	Ext.define('CMDBuild.controller.administration.domain.Attributes', {
		extend: 'CMDBuild.controller.administration.CMBaseAttributesController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Domain}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Object}
		 */
		currentAttribute: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.attributes.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Ext.selection.RowModel}
		 */
		gridSM: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.attributes.AttributesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.domain.Domain} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			Ext.apply(this, configurationObject); // Apply configuration to class

			var view = Ext.create('CMDBuild.view.administration.domain.attributes.AttributesView');

			this.callParent([view]);

			// Shorthands
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
		 * Fake cmfg implementation waiting for future refactor
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * TODO: waiting for refactor
		 */
		cmfg: function(name, param, callBack) {
			switch (name) {
				case 'onDomainAddButtonClick':
					return this.onDomainAddButtonClick();

				case 'onDomainSelected':
					return this.onDomainSelected();

				default: {
					if (!Ext.isEmpty(this.parentDelegate) && Ext.isFunction(this.parentDelegate.cmfg))
						return this.parentDelegate.cmfg(name, param, callBack);
				}
			}
		},

		/**
		 * Cache synch
		 *
		 * @param {Array} savedAttributes
		 */
		anAttributeWasMoved: function(savedAttributes) {
			if (!this.cmfg('domainSelectedDomainIsEmpty') && !Ext.isEmpty(savedAttributes)) {
				var oldAttributes = this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.ATTRIBUTES);

				for (var i = 0; i < savedAttributes.length; ++i) {
					var newAttr = savedAttributes[i];

					for (var j = 0; j < oldAttributes.length; ++j) {
						var oldAttr = oldAttributes[j];

						if (oldAttr[CMDBuild.core.constants.Proxy.NAME] == newAttr[CMDBuild.core.constants.Proxy.NAME]) {
							oldAttr[CMDBuild.core.constants.Proxy.INDEX] = newAttr[CMDBuild.core.constants.Proxy.INDEX];

							break;
						}
					}
				}
			}
		},

		deleteAttribute: function() {
			if (!this.cmfg('domainSelectedDomainIsEmpty') && !Ext.isEmpty(this.currentAttribute)) {
				CMDBuild.core.LoadMask.show();
				CMDBuild.ServiceProxy.administration.domain.attribute.remove({
					params: {
						className: this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.NAME),
						name: this.currentAttribute.get(CMDBuild.core.constants.Proxy.NAME)
					},
					scope: this,
					success: function(result, options, decodedResult) {
						this.form.reset();

						_CMCache.onDomainAttributeDelete(
							this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.ID),
							this.currentAttribute[CMDBuild.core.constants.Proxy.DATA]
						);

						this.currentAttribute = null;
					},
					callback: function() {
						CMDBuild.core.LoadMask.hide();
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
		 * @returns {CMDBuild.view.administration.domain.attributes.AttributesView}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * @override
		 */
		getCurrentEntryTypeId: function() {
			return this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.ID);
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
				attribute[CMDBuild.core.constants.Proxy.NAME] = rec.get(CMDBuild.core.constants.Proxy.NAME);
				attribute[CMDBuild.core.constants.Proxy.INDEX] = i + 1;

				attributes.push(attribute);
			}

			var params = {};
			params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.JSON.encode(attributes);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.NAME);

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

		onDomainAddButtonClick: function() {
			this.view.disable();
		},

		onDomainSelected: function() {
			if (!this.cmfg('domainSelectedDomainIsEmpty')) {
				this.view.enable();

				this.form.domainName = this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.NAME);
				this.form.hideContextualFields();

				this.grid.refreshStore(this.cmfg('domainSelectedDomainGet'));
			}
		},

		onSaveButtonClick: function() {
			var nonValid = this.form.getNonValidFields();
			var data = this.form.getData(true);

			data[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.NAME);

			if (nonValid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				return;
			}

			CMDBuild.core.LoadMask.show();
			CMDBuild.ServiceProxy.administration.domain.attribute.save({
				params: data,
				scope: this,
				success: function(result, options, decodedResult) {
					var attribute = decodedResult.attribute;

					this.currentAttribute = null;
					this.form.disableModify();

					_CMCache.onDomainAttributeSaved(this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.ID), attribute);

					this.grid.selectAttributeByName(attribute[CMDBuild.core.constants.Proxy.NAME]);

					CMDBuild.view.common.field.translatable.Utils.commit(this.form);
				},
				callback: function() {
					CMDBuild.core.LoadMask.hide();
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