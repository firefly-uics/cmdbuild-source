(function() {

	Ext.define('CMDBuild.app.Login', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.field.LanguageCombo}
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

		frame: false,
		border: false,
		hideMode: 'offsets',
		renderTo: 'login_box',

		statics: {
			build: function() {
				Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
				Ext.create('CMDBuild.core.Data'); // Data connections configuration

				Ext.create('CMDBuild.core.configurationBuilders.Localization', { // CMDBuild localization configuration
					callback: function(options, success, response) {
						Ext.create('CMDBuild.app.Login', { id: 'login' });
					}
				});
			}
		},

		initComponent: function() {
			Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
			Ext.create('CMDBuild.core.Data'); // Data connections configuration

			Ext.tip.QuickTipManager.init();

			// fix a problem of Ext 4.2 tooltips width
			// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
			delete Ext.tip.Tip.prototype.minWidth;

			Ext.apply(this, {
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						title: CMDBuild.Translation.login,
						frame: true,
						padding: 10,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

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
									Ext.create('CMDBuild.core.buttons.text.Login', {
										scope: this,

										handler: this.doLogin
									})
								]
							})
						],

						items: [
							(
								!Ext.isEmpty(CMDBuild.configuration.localization)
								&& CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT)
							) ? this.language = Ext.create('CMDBuild.view.common.field.LanguageCombo', {
								fieldLabel: CMDBuild.Translation.language,
								labelWidth: CMDBuild.LABEL_WIDTH_LOGIN,
								submitValue: false
							}) : null,
							this.user = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.USERNAME,
								fieldLabel: CMDBuild.Translation.username,
								labelWidth: CMDBuild.LABEL_WIDTH_LOGIN,
								allowBlank: false,

								listeners: {
									scope: this,
									change: function(field, newValue, oldValue, eOpts) {
										this.disableRoles();
									},
									specialkey: function(field, e, eOpts) {
										if (e.getKey() == e.ENTER)
											this.doLogin(field, e);
									}
								}
							}),
							this.password = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.PASSWORD,
								fieldLabel: CMDBuild.Translation.password,
								labelWidth: CMDBuild.LABEL_WIDTH_LOGIN,
								inputType: 'password',
								allowBlank: false,

								listeners: {
									scope: this,
									specialkey: function(field, e, eOpts) {
										if (e.getKey() == e.ENTER)
											this.doLogin(field, e);
									}
								}
							}),
							this.role = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.ROLE,
								hiddenName: CMDBuild.core.constants.Proxy.ROLE,
								fieldLabel: CMDBuild.Translation.chooseAGroup,
								labelWidth: CMDBuild.LABEL_WIDTH_LOGIN,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,
								forceSelection: true,

								store: Ext.create('Ext.data.ArrayStore', {
									fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
									sorters: [
										{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
									]
								}),
								queryMode: 'local',

								listeners: {
									scope: this,
									render: function(field, eOpts) {
										this.setupFields();
									},
									specialkey: function(field, e, eOpts) {
										if (e.getKey() == e.ENTER) {
											try {
												this.listKeyNav.selectHighlighted(e);

												this.doLogin();
											} catch (e) {
												_error('error setting the group', this);
											}
										}
									}
								}
							})
						]
					}),
					{
						xtype: 'panel',
						border: false,
						contentEl: 'release_box'
					}
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			afterrender: function(panel, eOpts) {
				this.setupFields();
			}
		},

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
				params[CMDBuild.core.constants.Proxy.PASSWORD] = this.password.getValue();
				params[CMDBuild.core.constants.Proxy.USERNAME] = this.user.getValue();

				CMDBuild.core.proxy.session.JsonRpc.login({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var urlParams = {};
						urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

						CMDBuild.core.proxy.session.Rest.login({
							params: params,
							urlParams: urlParams,
							scope: this,
							success: function(response, options, decodedResponse) {
								Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN, urlParams[CMDBuild.core.constants.Proxy.TOKEN]);
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
						if (!Ext.isEmpty(decodedResult) && decodedResult[CMDBuild.core.constants.Proxy.REASON] == 'AUTH_MULTIPLE_GROUPS') {
							// Multiple groups for this user
							// TODO Disable user/pass on multiple groups
							this.enableRoles(decodedResult[CMDBuild.core.constants.Proxy.GROUPS]);

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
		 */
		enableRoles: function(roles) {
			this.role.getStore().loadData(roles);
			this.role.enable();
			this.role.show();
			this.role.focus();
		},

		setupFields: function() {
			if (
				!Ext.isEmpty(CMDBuild.Runtime)
				&& !Ext.isEmpty(CMDBuild.Runtime.Username)
			) {
				this.user.setValue(CMDBuild.Runtime.Username);
				this.user.disable();

				this.password.hide();
				this.password.disable();
			} else {
				this.user.focus();
			}

			if (
				!Ext.isEmpty(CMDBuild.Runtime)
				&& !Ext.isEmpty(CMDBuild.Runtime.Groups)
			) {
				this.enableRoles(CMDBuild.Runtime.Groups);
			} else {
				this.disableRoles();
			}
		}
	});

})();