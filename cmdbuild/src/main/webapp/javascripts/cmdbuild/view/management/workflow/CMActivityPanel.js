(function() {
	var tr =  CMDBuild.Translation.management.modworkflow,
		modeConvertionMatrix = {
			VIEW: "read",
			UPDATE: "write",
			REQUIRED: "required"
		};

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel", {
		extend: "Ext.panel.Panel",

		initComponent: function() {

			this.activityForm = new CMDBuild.view.management.workflow.CMActivityPanel.Form({
				region: "center",
				cmOwner: this
			});

			this.wfWidgetsPanel = new CMDBuild.view.management.workflow.CMActivityPanel.WFWidgetsPanel({
				region: 'east',
				hideMode: 'offsets',
				cls: "cmborderleft",
				autoScroll: true,
				frame: true,
				border: false,
				items: []
			});

			this.wfWidgetsPanel.hide();

			this.layout = "border";
			this.items = [this.activityForm, this.wfWidgetsPanel];
			this.callParent(arguments);

			this.activityForm.on("cmeditmode", function() {
				this.fireEvent("cmeditmode");
			}, this);

			this.activityForm.on("cmdisplaymode", function() {
				this.fireEvent("cmdisplaymode");
			}, this);

			this.mon(this, "activate", function() {
				this.activityForm.fireEvent("activate");
			}, this)

		},

		/*
		 o = {
				editMode: editMode,
				cb: cb,
				scope: this
			}
		 */
		updateForActivity: function(activity, o) {
			this.activityForm.setActivity(activity)
			this.activityForm.updateInfo(activity);
			this.wfWidgetsPanel.updateWidgets(activity);

			var loadCard = Ext.bind(function() {
				this.activityForm.loadCard(activity);
				if (o.editMode) {
					this.editMode();
				} else {
					this.displayMode(enableCMTbar = true);
				}
				if (o.cb) {
					var scope = o.scope || this;
					o.cb.call(this);
				}
			}, this);

			//we need to reload always the fields
			_CMCache.getAttributeList(
				activity.data.IdClass,
				Ext.bind(function cb(a) {
					this.activityForm.buildActivityFields(a);
					loadCard();
				}, this)
			);
		},

		updateForClosedActivity: function(activity) {
			this.activityForm.setActivity = activity;
			this.activityForm.updateInfo(activity);
			this.wfWidgetsPanel.updateWidgets(activity);
			
			_CMCache.getAttributeList(
				activity.data.IdClass,
				Ext.bind(function cb(a) {
					this.activityForm.fillForm(a, editMode = false);
					this.activityForm.loadCard(activity);
				}, this)
			);
		},

		clearForNoActivity: function() {
			this.activityForm.removeAll(destroy = true);
			this.activityForm.updateInfo();
			this.wfWidgetsPanel.removeAll(destroy = true);
			this.wfWidgetsPanel.hide();
			this.displayMode();
		},

		getForm: function() {
			return this.activityForm.getForm();
		},

		getValues: function() {
			return this.activityForm.getValues();
		},

		displayMode: function(enableCMTbar) {
			this.activityForm.displayMode(enableCMTbar);
			this.wfWidgetsPanel.displayMode();
		},

		editMode: function() {
			this.activityForm.editMode();
			this.wfWidgetsPanel.editMode();
		},

		reset: function() {
			this.activityForm.reset();
		},

		getInvalidAttributeAsHTML: function() {
			return this.activityForm.getInvalidAttributeAsHTML();
		},

		isTheActivePanel: function() {
			var out = true;
			try {
				out = this.ownerCt.layout.getActiveItem() == this;
			} catch (e) {
				// if fails, the panel is not in a TabPanel, so don't defer the call
			}

			return out;
		}
	});

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.WFWidgetsPanel", {
		extend: "Ext.panel.Panel",
		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				layout : 'vbox',
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "30px 5px 0 5px"
				}
			});
			this.callParent(arguments);
		},

		updateWidgets: function(activity) {
			var data = activity.raw || activity.data,
				widgetsDefinition = data.CmdbuildExtendedAttributes || [],
				me = this;

			this.writePrivilege = activity.raw.priv_write;

			this.removeAll();

			if (widgetsDefinition.length > 0) {
				this.show();
				Ext.each(widgetsDefinition, function(item) {
					me.add(new Ext.Button({
						text: item.btnLabel || CMDBuild.Translation.management.modworkflow[item.labelId],
						widgetDefinition: item,
						disabled: true,
						handler: function() {
							me.fireEvent("cm-wfwidgetbutton-click", item);
						},
						margins:'0 0 5 0'
					}));
				});
				me.updateButtonsWidth();
			} else {
				this.hide();
			}
		},

		updateButtonsWidth: function () {
			var maxW = 0;
			this.items.each(function(item) {
				var w = item.getWidth();
				if (w > maxW) {
					maxW = w;
				}
			});

			this.items.each(function(item) {
				item.setWidth(maxW);
			});
			// to fix the width of the panel, auto width does
			// not work with IE7
			this.setWidth(maxW + 10); // 10 is the pudding
		},

		displayMode: function() {
			this.items.each(function(i) {
				i.disable();
			});
		},

		editMode: function() {
			if (this.writePrivilege) {
				this.items.each(function(i) {
					i.enable();
				});
			}
		}
	});
	
	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.Form", {
		extend: "CMDBuild.view.management.classes.CMCardPanel",

		buildTBar: function() {
			this.cmTBar = [
				this.modifyCardButton = new Ext.button.Button({
					iconCls : "modify",
					text : tr.modify_card
				}),
				this.deleteCardButton = new Ext.button.Button({
					iconCls : "delete",
					text : tr.delete_card,
					enable: function() {
						if (this.stoppable) { // stoppable is set on setActivity
							Ext.button.Button.prototype.enable.call(this);
						} else {
							this.disable();
						}
					}
				}),
				'->','-',
				this.processStepName = new Ext.button.Button({
					overCls: Ext.button.Button.baseCls,
					pressedCls: Ext.button.Button.baseCls,
					disable: Ext.emptyFn
				}),
				'-',
				this.processStepCode = new Ext.button.Button({
					overCls: Ext.button.Button.baseCls,
					pressedCls: Ext.button.Button.baseCls,
					disable: Ext.emptyFn
				}),
				' '
			];
		},

		buildButtons: function() {
			this.cmButtons = [
				this.saveButton = new Ext.button.Button({
					text: CMDBuild.Translation.common.buttons.save
				}),

				this.advanceButton = new Ext.button.Button({
					text: CMDBuild.Translation.common.buttons.workflow.advance
				}),

				this.cancelButton = new Ext.button.Button( {
					text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort
				})
			];
		},

		setActivity: function(activity) {
			this.activityToLoad = activity;

			this.writePrivilege = activity.raw.priv_write;
			this.deleteCardButton.stoppable = activity.raw.stoppable;
		},

		updateInfo : function(activity) {
			activity = activity || {};
			var data = activity.raw || activity.data || {};

			this.processStepName.setText(data.activityPerformerName || "");
			this.processStepCode.setText(data.Code || "");
		},

		buildActivityFields: function(attributes, editMode) {
			var cleanedAttrs = [],
				data = this.activityToLoad.raw || this.activityToLoad.data; // if is a template given from the server, has not the raw data, so use the data

			for (var a in attributes) {
				a = attributes[a];
				var index = data[a.name + "_index"];
				if (index != undefined && index > -1) {
					var mode = data[a.name + "_type"];
					a.fieldmode = modeConvertionMatrix[mode];
					cleanedAttrs[index] = a;
				}
			}

			this.fillForm(cleanedAttrs, editMode);
		},

		canReconfigureTheForm: function() {
			return this.cmOwner.isTheActivePanel();
		}
	});

})();