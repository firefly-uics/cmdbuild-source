(function() {

	var tr = CMDBuild.Translation.login;

	Ext.define('CMDBuild.LoginPanel', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest'
		],

		frame: false,
		border: false,
		hideMode: 'offsets',
		renderTo: 'login_box',

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.field.LanguageCombo}
		 */
		language: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		password: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		role: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		user: undefined,

		statics: {
			buildAfterRequest: function() {
				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					scope: this,
					success: function(response, options, decodedResponse) {
						CMDBuild.Config.cmdbuild = decodedResponse.data;
					},
					callback: function() {
						Ext.create('CMDBuild.LoginPanel', {
							id: 'login'
						});
					}
				});
			}
		},

		initComponent: function() {
			Ext.tip.QuickTipManager.init();
			// fix a problem of Ext 4.2 tooltips width
			// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
			delete Ext.tip.Tip.prototype.minWidth;

			var me = this;

			this.buildLanguagesCombo();

			var enterKeyListener = {
				'specialkey': function(field, event) {
					if (event.getKey() == event.ENTER)
						me.doLogin(field, event);
				}
			};

			this.user = Ext.create('Ext.form.field.Text', {
				scope: this,
				fieldLabel: tr.username,
				name: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
				allowBlank: false,

				listeners: enterKeyListener
			});

			this.password = Ext.create('Ext.form.field.Text', {
				scope: this,
				fieldLabel: tr.password,
				name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
				inputType: 'password',
				allowBlank: false,

				listeners: enterKeyListener
			});

			this.role = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ROLE,
				hiddenName: CMDBuild.core.proxy.CMProxyConstants.ROLE,
				id: 'rolefield',
				fieldLabel: tr.multi_group,
				hideMode: 'offsets',
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				editable: false,
				forceSelection: true,

				store: Ext.create('Ext.data.Store', {
					fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
					sorters: [
						{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
					]
				}),
				queryMode: 'local',

				listeners: {
					scope: this,
					specialkey: function(field, e, eOpts) {
						if (e.getKey() == e.ENTER) {
							try {
								this.listKeyNav.selectHighlighted(e);

								me.doLogin();
							} catch (e) {
								_error('error setting the group', this);
							}
						}
					}
				}
			});

			this.form = Ext.create('Ext.form.Panel', {
				labelWidth: 100,
				title: tr.title,
				frame: true,
				defaultType: 'textfield',
				padding: 10,

				buttonAlign: 'center',
				buttons: [
					Ext.create('Ext.button.Button', {
						scope: this,
						text: tr.login,
						handler: this.doLogin
					})
				],

				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				items: this.buildFieldsArray()
			});

			Ext.apply(this, {
				items: [
					this.form,
					{
						xtype: 'panel',
						border: false,
						contentEl: 'release_box'
					}
				]
			});

			this.user.on('change', this.disableRoles, this);

			this.callParent(arguments);

			this.on('afterrender', this.setupFields, this);
			this.role.on('render', this.setupFields, this); // Backward compatibility wit Ext2.2
		},


		/**
		 * @return {Array}
		 *
		 * @private
		 */
		buildFieldsArray: function() {
			if (this.language) {
				return [this.language, this.user, this.password, this.role];
			} else {
				return [this.user, this.password, this.role];
			}
		},

		/**
		 * @private
		 */
		buildLanguagesCombo: function() {
			if (CMDBuild.Config.cmdbuild.languageprompt == 'true') {
				this.language = Ext.create('CMDBuild.view.common.field.LanguageCombo', {
					fieldLabel: tr.language
				});
			}
		},

		/**
		 * @private
		 */
		disableRoles: function() {
			this.role.disable();
			this.role.hide();
		},

		/**
		 * @param {Mixed} field
		 * @param {Ext.EventObjectImpl} event
		 *
		 * @private
		 */
		doLogin: function(field, event) {
			if (!Ext.isEmpty(this.role.getValue()) || this.form.getForm().isValid()) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.PASSWORD] = this.password.getValue();
				params[CMDBuild.core.proxy.CMProxyConstants.USERNAME] = this.user.getValue();

				if (!this.role.isHidden())
					params[CMDBuild.core.proxy.CMProxyConstants.ROLE] = this.role.getValue();

				CMDBuild.core.proxy.session.JsonRpc.login({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var urlParams = {};
						urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN] = response.getResponseHeader(CMDBuild.core.proxy.CMProxyConstants.AUTHORIZATION_HEADER_KEY);

						CMDBuild.core.proxy.session.Rest.login({
							params: params,
							urlParams: urlParams,
							scope: this,
							success: function(response, options, decodedResponse) {
								Ext.util.Cookies.set(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN, urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN]);
							},
							callback: function(records, operation, success) {
								// CMDBuild redirect
								if (/administration.jsp$/.test(window.location)) {
									window.location = 'administration.jsp' + window.location.hash;
								} else {
									window.location = 'management.jsp' + window.location.hash;
								}
							}
						});
					},
					failure: function(result, options, decodedResult) {
						if (!Ext.isEmpty(decodedResult) && decodedResult[CMDBuild.core.proxy.CMProxyConstants.REASON] == 'AUTH_MULTIPLE_GROUPS') {
							// Multiple groups for this user
							// TODO Disable user/pass on multiple groups
							this.enableRoles(decodedResult[CMDBuild.core.proxy.CMProxyConstants.GROUPS]);

							return false;
						} else {
							decodedResult.stacktrace = undefined; // To not show the detail link in the error pop-up
						}
					}
				});
			}
		},

		/**
		 * @param {Array} roles
		 *
		 * @private
		 */
		enableRoles: function(roles) {
			this.role.getStore().loadData(roles);
			this.role.enable();
			this.role.show();
			this.role.focus();
		},

		/**
		 * @private
		 */
		setupFields: function() {
			if (CMDBuild.Runtime && CMDBuild.Runtime.Username) {
				this.user.setValue(CMDBuild.Runtime.Username);
				this.user.disable();
				this.password.hide();
				this.password.disable();
			} else {
				this.user.focus();
			}

			if (CMDBuild.Runtime && CMDBuild.Runtime.Groups) {
				this.enableRoles(CMDBuild.Runtime.Groups);
			} else {
				this.disableRoles();
			}
		}
	});

})();
