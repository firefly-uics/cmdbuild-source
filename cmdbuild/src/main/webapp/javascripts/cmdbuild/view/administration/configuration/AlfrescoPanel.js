(function() {

	Ext.define('CMDBuild.view.administration.configuration.AlfrescoPanel', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @property {Mixed}
		 */
		attachmentLookup: undefined,

		/**
		 * @cfg {Boolean}
		 */
		attachmentLookupFirstLoad: true,

		/**
		 * @cfg {String}
		 */
		configFileName: 'dms',

		/**
		 * @property {Ext.form.FieldSet}
		 */
		credentialsFieldset: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		lookupTypeCombo: undefined,

		initComponent: function() {
			this.lookupTypeCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'category.lookup',
				fieldLabel: CMDBuild.Translation.cmdbuildCategory,
				valueField: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				allowBlank: false,

				store: CMDBuild.Cache.getLookupTypeLeavesAsStore(),
				queryMode: 'local'
			});

			updateAttachementLookupAfterSetValueOnLookupTypeCombo(this);

			this.credentialsFieldset = Ext.create('Ext.form.FieldSet', {
				title: CMDBuild.Translation.credentials,
				autoHeight: true,
				defaultType: 'textfield',

				items: [
					{
						name: 'credential.user',
						fieldLabel: CMDBuild.Translation.username,
						allowBlank: false
					},
					{
						name: 'credential.password',
						fieldLabel: CMDBuild.Translation.password,
						inputType: 'password',
						allowBlank: false
					},
					this.lookupTypeCombo
				]
			});

			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.alfresco,
				items: [
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.general,
						autoHeight: true,
						defaultType: 'textfield',

						items: [
							{
								xtype: 'xcheckbox',
								name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
								fieldLabel: CMDBuild.Translation.enabled
							},
							{
								name: 'server.url',
								fieldLabel: CMDBuild.Translation.host,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.proxy.CMProxyConstants.DELAY,
								fieldLabel: CMDBuild.Translation.operationsDelay,
								allowBlank: false
							}
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.fileServer,
						autoHeight: true,
						defaultType: 'textfield',

						items: [
							{
								name: 'fileserver.type',
								fieldLabel: CMDBuild.Translation.type,
								allowBlank: false,
								disabled: true
							},
							{
								name: 'fileserver.url',
								fieldLabel: CMDBuild.Translation.host,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'fileserver.port',
								fieldLabel: CMDBuild.Translation.port,
								allowBlank: false
							}
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.repository,
						autoHeight: true,
						defaultType: 'textfield',

						items: [
								{
									name: 'repository.fspath',
									fieldLabel: CMDBuild.Translation.fileServerPath,
									width: CMDBuild.CFG_BIG_FIELD_WIDTH,
									allowBlank: false
								}, {
									name: 'repository.wspath',
									fieldLabel: CMDBuild.Translation.webServicePath,
									width: CMDBuild.CFG_BIG_FIELD_WIDTH,
									allowBlank: false
								}, {
									name: 'repository.app',
									fieldLabel: CMDBuild.Translation.application,
									allowBlank: false
								}
						]
					},
					this.credentialsFieldset
				]
			});

			this.callParent(arguments);
		},

//		getValues: function() {
//			var values = this.callParent(arguments);
//
//			if (this.attachmentLookup)
//				values['category.lookup.attachments'] = this.attachmentLookup.getRawValue();
//
//_debug('getValues', values);
//			return values;
//		},

		/**
		 * @param {Object} saveDataObject
		 *
		 * @override
		 */
		afterSubmit: function(saveDataObject) {
			if (this.valuesFromServer && this.attachmentLookup)
				this.attachmentLookup.setValue(this.valuesFromServer[this.attachmentLookup.name]);
		}
	});

	function updateAttachementLookupAfterSetValueOnLookupTypeCombo(me) {
		me.lookupTypeCombo.setValue = Ext.Function.createSequence(
			me.lookupTypeCombo.setValue,
			function(value) {
				me.credentialsFieldset.remove(me.attachmentLookup);
				if (value == null) {
					return;
				}

				var ltype = null;
				if (Ext.isArray(value)) {
					value = value[0];
				}

				if (typeof value == 'string') {
					ltype = value;
				} else {
					ltype = value.get('type');
				}
				var lookupchain = _CMCache.getLookupchainForType(ltype);
				if (lookupchain.length == 0) {
					value = '';
					return;
				}
				var conf = {
					description: CMDBuild.Translation.attachmentsLookup,
					name: 'category.lookup.attachments',
					isnotnull: false,
					fieldmode: 'write',
					type: 'LOOKUP',
					lookup: ltype,
					lookupchain: lookupchain
				};

				me.attachmentLookup = CMDBuild.Management.FieldManager.getFieldForAttr(conf, false, true);
				me.attachmentLookup.labelWidth = CMDBuild.CFG_LABEL_WIDTH;
				me.attachmentLookup.width = CMDBuild.CFG_MEDIUM_FIELD_WIDTH;
				me.attachmentLookup.labelAlign = 'left';

				// There is a rendering issue that appear resolved adding a delay to add the combo to the field-set
				Ext.Function.createDelayed(
					function() {
						me.credentialsFieldset.add(me.attachmentLookup);
						if (me.attachmentLookupFirstLoad) {
							me.attachmentLookupFirstLoad = false;
							me.afterSubmit();
						}
					},
					200
				)();
		});
	}

})();