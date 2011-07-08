(function() {
	var NO_SELECTION = "No selection";

	Ext.define("CMDBuild.view.management.classes.relations.CMEditRelationWindow", {
		extend: "CMDBuild.Management.CardListWindow",

		// passed in instantiation
		relId: undefined,
		filterType: undefined,

		relation: undefined,

		// override
		initComponent: function() {
			if (this.relation == undefined) {
				throw "You must pass a relation to the CMEditRelationWindow";
			} else {
				this.idClass = this.relation.get("dst_cid");
				this.idDomain = this.relation.get("dom_id");
				this.relId = this.relation.get("rel_id");
			}

			this.callParent(arguments);
		},

		// override
		setItems: function() {
			var attributes = _CMCache.getDomainById(this.idDomain).get("attributes");

			this.attributesPanel = CMDBuild.Management.EditablePanel.build({
				autoScroll: true,
				region: "south",
				height: "30%",
				attributes: attributes,
				split: true,
				frame: false,
				border: "1 0 0 0",
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			});

			this.callParent(arguments);

			if (this.attributesPanel != null) {
				this.tabPanel.region = "center";
				this.items.push(this.attributesPanel);
				this.layout = "border";
			} else {
				this.attributesPanel = buildNullObject();
			}

			this.saveButton = new CMDBuild.buttons.SaveButton({
				scope: this,
				handler: onSaveButtonClick
			});

			this.abortButton = new CMDBuild.buttons.AbortButton({
				scope: this,
				handler: function() {
					this.close();
				}
			})
			
			Ext.apply(this, {
				buttonAlign: "center",
				buttons: [this.saveButton, this.abortButton]
			})
		},
		
		// override
		show: function() {
			this.callParent(arguments);
			this.attributesPanel.editMode();
		}
	});

	function onSaveButtonClick() {
		var p = buildSaveParams(this);
		if (p) {
			CMDBuild.ServiceProxy.relations.modify({
				params: Ext.JSON.encode(p),
				success: function() {
					alert("@@ success");
				},
				failure: function() {
					alert("@@ failure");
				}
			});
		}
	}

	function buildSaveParams(me) {
		var p = {
				id: me.relId,
				did: me.idDomain,
				attrs: {}
			},
			relKey = me.relation.get("src") == "_1" ? "_2" : "_1";

		try {
			p.attrs[relKey] = getSelectionsId(me);
		} catch (e) {
			if (e == NO_SELECTION) {
				CMDBuild.Msg.error("@@Error", "@@Devi selezionare qualcosa nella griglia");
			}
			return;
		}

		try {
			p.attrs = Ext.apply(p.attrs, getData(me.attributesPanel));
		} catch (e) {
			CMDBuild.Msg.error("@@Error", "@@I seguenti campi sono non validi" + e);
			return;
		}

		return p
	}

	function getSelectionsId(me) {
		var sel = me.grid.getSelectionModel().getSelection(),
			l=sel.length,
			id_sel = [];
		
		if (l>0) {
			for (var i=0; i<l; ++i) {
				 id_sel.push(sel[i].get("Id"));
			}

			return id_sel;
		} else {
			throw NO_SELECTION;
		}
	}

	function getData(attributesPanel) {
		var data = {},
			nonValid = "",
			ff = attributesPanel.getFields(),
			f;

		for (var i=0, l=ff.length; i<l; ++i) {
			f = ff[i];
			if (f.validate()) {
				data[f.name] = f.getValue();
			} else {
				nonValid += "<p><b>" + f.fieldLabel + "</b></p>"
			}
		}

		if (nonValid) {
			throw nonValid;
		} else {
			return data;
		}
	}

	function buildAttributeFields(attributes) {
		attributes = attributes || [];
		var out = "", a;
		
		for (var i=0, l=attributes.length; i<l; ++i) {
			a = attributes[i];
			_debug(a);
		}
	}

	function buildNullObject() {
		return {
			editMode: Ext.emptyFn,
			getFields: function() {
				return {};
			}
		}
	}

})();


/*
 * CMDBuild.Management.EditRelationWindow = Ext.extend(CMDBuild.Management.DomainCardListWindow, {
	translation: CMDBuild.Translation.management.modcard.modify_relation_window,
	currentDomain: -1,
	attributeList: {},
	notInRelation: [],
	checkedCards: {},
	updateEventName: 'cmdb-reload-card',

	initComponent: function() {
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true,
			disabled: true,
			handler : this.onSave,
			scope : this
		});

		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.onCancel,
			scope : this
		});

		this.cardListFilter = new CMDBuild.Management.DomainCardListFilter({
			IdClass: this.domainDestClassId,
			filterType: this.filterType,
			ownerWindow: this
		});
		this.cardListFilter.selectClass({
			classId: this.domainDestClassId,
			classAttributes: this.attributes
		});

		this.cardListFilter.on(CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME, this.filtersuccess, this);

		this.cardList = new CMDBuild.Management.FixedCardGrid({
			attributes: this.attributes,
			baseParams: {
				IdClass: this.domainDestClassId,
				FilterCategory: this.filterType,
				FilterSubcategory: this.id
			},
			frame: false,
			border: false,
			ownerWindow: this,
			filterType: this.filterType,
			withCheckColumn:false,
			checksmodel: false,
			isFormField: true,
			isValid: function(preventMark) {
				return this.validate();
			},
			validate: function() {
				return this.getSelectionModel().hasSelection();
			},
			tbar: [this._buildAddButton({
				classId: this.domainDestClassId,
				eventName: "cmdbuild-new-relationcard"
			})]
		});
		this.cardList.getSelectionModel().on('rowselect', function() {
			this.saveButton.enable();
		}, this);
		Ext.apply(this.cardList, {sm :new Ext.grid.RowSelectionModel()});

		this.tabPanel = new Ext.TabPanel({
			activeTab: 0,
			region: 'center',
			items: [{
				xtype: 'panel',
				layout: 'fit',
				title: CMDBuild.Translation.management.findfilter.list,
				items: [this.cardList]
			},{
				xtype: 'panel',
				layout: 'fit',
				disable: true,
				title: CMDBuild.Translation.management.findfilter.filter,
				items: [this.cardListFilter]
			}]
		});

		this.tabPanel.on('tabchange', function(tabPanel, tab) {
			tab.doLayout();
		});

		this.formPanel = new Ext.FormPanel({
			items: [{
				xtype: 'hidden',
				name: 'DomainId',
				value: this.domainId
			},{
				xtype: 'hidden',
				name: 'Class1Id',
				value: this.class1Id
			},{
				xtype: 'hidden',
				name: 'Card1Id',
				value: this.card1Id
			},{
				xtype: 'hidden',
				name: 'Class2Id',
				value: this.class2Id
			},{
				xtype: 'hidden',
				name: 'Card2Id',
				value: this.card2Id
			},{
				xtype: 'hidden',
				name: 'DomainDirection',
				value: this.domainDirection
			}]
		});

		this.complexPanel = new Ext.Panel({
			frame: true,
			broder: false,
			layout: 'border',
			labelAlign: 'right',
			monitorValid: true,
			items: [
				this.formPanel,
				this.tabPanel
			],
			buttonAlign: 'center',
			buttons: [
				this.saveButton,
				this.cancelButton
			]
		});
		Ext.apply(this, {
			frame: true,
			border: false,
			title: this.translation.window_title,
			items: this.complexPanel
		});
		CMDBuild.Management.EditRelationWindow.superclass.initComponent.apply(this);
	},

	onSave: function() {
		this.disable();
		var selectedRow = this.cardList.getSelectionModel().getSelected();
		var requestParams = this.formPanel.getForm().getValues();		
		requestParams['Id'] = selectedRow.json.Id;
		requestParams['IdClass'] = selectedRow.json.IdClass;
		var cardId = this.cardId;
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/modifyrelation',
			params: requestParams,
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			success: function() {
				this.publish(this.updateEventName, {cardId: cardId});
				this.close();
			},
			failure: function() {
				this.enable();
			},
			scope: this
		});
	},

	filtersuccess: function() {
		this.tabPanel.setActiveTab(0);
		this.cardList.reload();
	}
});

 */