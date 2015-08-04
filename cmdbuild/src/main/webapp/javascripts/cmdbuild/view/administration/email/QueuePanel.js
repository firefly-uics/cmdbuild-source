(function() {

	Ext.define('CMDBuild.view.administration.email.QueuePanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Queue}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.slider.SingleWithExtremeLabels}
		 */
		cycleIntervalField: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		queueStartButton: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		queueStopButton: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			this.queueStartButton = Ext.create('CMDBuild.core.buttons.Start', {
				text: CMDBuild.Translation.start,
				scope: this,

				handler: function(button, e) {
					this.delegate.cmfg('onEmailQueueStartButtonClick');
				}
			});

			this.queueStopButton = Ext.create('CMDBuild.core.buttons.Stop', {
				text: CMDBuild.Translation.stop,
				scope: this,

				handler: function(button, e) {
					this.delegate.cmfg('onEmailQueueStopButtonClick');
				}
			});

			this.cycleIntervalField = Ext.create('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
				name: CMDBuild.core.proxy.CMProxyConstants.TIME,
				fieldLabel: CMDBuild.Translation.frequencyCheck,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
				minValue: 1,
				maxValue: 10
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							'->',
							this.queueStartButton,
							this.queueStopButton
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.cycleIntervalField]
			});

			this.callParent(arguments);
		}
	});

})();