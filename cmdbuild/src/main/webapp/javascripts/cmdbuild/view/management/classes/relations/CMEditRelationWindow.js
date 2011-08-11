(function() {
	var NO_SELECTION = "No selection";

	Ext.define("CMDBuild.view.management.classes.relations.CMEditRelationWindow", {
		successCb: Ext.emptyFn,

		extend: "CMDBuild.Management.CardListWindow",

		// passed in instantiation
		relation: undefined, //{dst_cid: "", dom_id: "", rel_id: "", src: "", rel_attr: []}
		currentCard: undefined, // the source of the relation

		// override
		initComponent: function() {
			if (this.relation == undefined) {
				throw "You must pass a relation to the CMEditRelationWindow";
			} else {
				this.idClass = this.relation.dst_cid;
			}

			this.callParent(arguments);
		},

		// override
		setItems: function() {
			var attributes = _CMCache.getDomainById(this.relation.dom_id).get("attributes");

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
			});
			
			Ext.apply(this, {
				buttonAlign: "center",
				buttons: [this.saveButton, this.abortButton]
			});
		},
		
		// override
		show: function() {
			this.callParent(arguments);
			this.attributesPanel.editMode();
			
			var fields = this.attributesPanel.getFields(),
				rel_attrs = this.relation.rel_attr || {};
				
				for (var i = 0, l=fields.length; i<l; ++i) {
					var f = fields[i],
						name = f.CMAttribute.name,
						val = rel_attrs[name];
					
					if (val) {
						f.setValue(val.id || val);
					}
				}
		}
	});

	function onSaveButtonClick() {
		var p = buildSaveParams(this);
		if (p) {
			if (p.id == -1) { // creation
				delete p.id;
				CMDBuild.ServiceProxy.relations.add({
					params: { JSON: Ext.JSON.encode(p) },
					scope: this,
					success: function() {
						this.successCb();
						this.close();
					}
				});
			} else { // modify
				CMDBuild.ServiceProxy.relations.modify({
					params: { JSON: Ext.JSON.encode(p) },
					scope: this,
					success: function() {
						this.successCb();
						this.close();
					}
				});
			}
		}
	}

	function buildSaveParams(me) {
		var p = {
				id: me.relation.rel_id,
				did: me.relation.dom_id,
				attrs: {}
			},
			relKey = me.relation.src == "_1" ? "_2" : "_1";

		try {
			p.attrs[relKey] = getSelections(me);
			if (me.relation.rel_id == -1) {
				p.attrs[me.relation.src] = {id: me.currentCard.get("Id"), cid: me.currentCard.get("IdClass")};
			}
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

		return p;
	}

	function getSelections(me) {
		var sel = me.grid.getSelectionModel().getSelection(),
			l=sel.length,
			id_sel = [];
		
		if (l>0) {
			for (var i=0; i<l; ++i) {
				 id_sel.push({id: sel[i].get("Id"), cid: sel[i].get("IdClass")});
			}

			if (id_sel.length == 1) {
				return id_sel[0];
			} else {
				return id_sel;
			}

		} else {
			if (me.relation.rel_id == -1) {
				// we are add a new relation, the selection is mandatory
				throw NO_SELECTION;
			}
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
				nonValid += "<p><b>" + f.fieldLabel + "</b></p>";
			}
		}

		if (nonValid) {
			throw nonValid;
		} else {
			return data;
		}
	}

	function buildNullObject() {
		return {
			editMode: Ext.emptyFn,
			getFields: function() {
				return {};
			}
		};
	}

})();