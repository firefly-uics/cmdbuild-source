(function() {

	var tr = CMDBuild.Translation.management.modcard,
		MD = "detail",
		FK = "foreignkey",
		detailURL = {
			get: "services/json/management/modcard/getdetaillist",
			remove: "services/json/management/modcard/deleterelation"
		},
		bigTablesURL = {
			get: "services/json/management/modcard/getcardlist",
			remove: "services/json/management/modcard/deletecard"
		};

	Ext.define("CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(v, sc) {
			this.callParent(arguments);

			this.view = v;
			this.superController = sc;

			this.entryType = null;
			this.card = null;

			this.currentForeignKey = null;
			this.currentDetail = null;

			this.mon(this.view.tabs,"click", onTabClick, this);
			this.mon(this.view.detailGrid, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view.detailGrid, "itemdblclick", onDetailDoubleClick, this);
			this.mon(this.view.addDetailButton, "cmClick", onAddDetailButtonClick, this);

			this.callBacks = {
				'action-masterdetail-edit': this.onEditDetailClick,
				'action-masterdetail-show': this.onShowDetailClick,
				'action-masterdetail-delete': this.onDeleteDetailClick,
				'action-masterdetail-graph': this.onOpenGraphClick,
				'action-masterdetail-note': this.onOpenNoteClick,
				'action-masterdetail-attach': this.onOpenAttachmentClick 
			};
		},

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.currentTab = null;
			this.currentForeignKey = null;
			this.currentDetail = null;

			this.view.disable();
			this.view.loadDetailsAndFKThenBuildSideTabs(this.entryType.get("id"));
			this.view.resetDetailGrid();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			this.view.setDisabled(this.view.empty);
			// given the tab is not active but enabled
			// and we change card
			// when the tab is activated
			// then the grid should be updated
			if (tabIsActive(this.view)) {
				updateDetailGrid.call(this);
			} else {
				this.view.on('activate', updateDetailGrid, this, {single: true});
			}
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			this.view.disable();
		},

		onEditDetailClick: function(model) {
			var me = this,
				w = buildWindow.call(this, {
					editable: true
				});

			w.mon(w, "destroy", function() {
				this.view.reload();
			}, this, {single: true});

			new CMDBuild.controller.management.common.CMDetailWindowController(w, {
				entryType: model.get("IdClass"),
				card: model.get("Id"),
				cmEditMode: true
			});

			w.show();
		},

		onShowDetailClick: function(model) {
			var w = buildWindow.call(this, {
				editable: false
			});

			new CMDBuild.controller.management.common.CMDetailWindowController(w, {
				entryType: model.get("IdClass"),
				card: model.get("Id"),
				cmEditMode: false
			});

			w.show();
		},

		onDeleteDetailClick: function(model) {
			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention,
				CMDBuild.Translation.management.modcard.delete_card_confirm,
				makeRequest, this);

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}
				
				// if I'm deleting a detail is needed to remove the
				// relation at first
				
				if (this.currentDetail) {
					var params = {
						"DomainId" : this.currentDetail.get("id")
					};
					addCardParams(params, masterSide(this.currentDetail), this.card);
					addCardParams(params, detailSide(this.currentDetail), model);

					CMDBuild.LoadMask.get().show();
					CMDBuild.ServiceProxy.relations.remove({
						params : params,
						scope: this,
						success: function() {
							removeCard.call(this, model);
						},
						callback: function() {
							CMDBuild.LoadMask.get().hide();
						}
					});
				} else if(this.currentForeignKey) {
					// so, remove directly the card
					removeCard.call(this, model);
				}
			};
		},

		onOpenGraphClick: function(model) {
			CMDBuild.Management.showGraphWindow(model.get("IdClass"), model.get("Id"));
		},

		onOpenNoteClick: function(model) {
			var editable = (model && model.raw && model.raw.priv_write);
			var w = new CMDBuild.view.management.common.CMNoteWindow({
				withButtons: editable
			}).show();

			var wc = new CMDBuild.view.management.common.CMNoteWindowController(w);
			wc.onCardSelected(model);

			w.mon(w, "destroy", function() {
				this.view.reload();
			}, this, {single: true});
		},

		onOpenAttachmentClick: function(model) {
			var w = new CMDBuild.view.management.common.CMAttachmentsWindow();
			new CMDBuild.controller.management.common.CMAttachmentsWindowController(w,modelToCardInfo(model));
			w.show();
		}
	});

	function masterSide(domainRecord) {
		return {
			"1:N" : 1,
			"N:1" : 2
		}[domainRecord.get("cardinality")];
	}

	function detailSide(domainRecord) {
		return 3-masterSide(domainRecord);
	}

	function addCardParams(params, side, record) {
		params["Class"+side+"Id"] = record.get("IdClass");
		params["Card"+side+"Id"] = record.get("Id");
	}

	function modelToCardInfo(model) {
		return {
			Id: model.get("Id"),
			IdClass: model.get("IdClass"),
			Description: model.get("Description")
		};
	}

	function onDetailDoubleClick(grid, model, html, index, e, options) {
		this.superController.openCard({
			Id: model.get("Id"),
			IdClass: model.get("IdClass"),
			activateFirstTab: true
		});
	}

	function removeCard(model) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.card.remove({
			scope : this,
			important: true,
			params : {
				"IdClass": model.get("IdClass"),
				"Id": model.get("Id")
			},
			success : updateDetailGrid,
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function updateDetailGrid() {
		if (this.card != null) {
			var p = {
				masterCard: this.card
			};

			if (this.currentDetail != null) {
				p["detail"] = this.currentDetail;
				this.view.updateGrid(MD, p);
			} else if (this.currentForeignKey != null) {
				p["detail"] = this.currentForeignKey;
				this.view.updateGrid(FK, p);
			} else {
				this.view.activateFirstTab();
			}
		}
	}

	function onTabClick(tab) {
		if (this.currentTab === tab || !tabIsActive(this.view)) {
			return;
		}

		var targetPanel = tab.targetPanel,
			type = targetPanel.detailType,
			detail = this.view.details[type][targetPanel.detailId];

		this.view.addDetailButton.enable();
		this.currentTab = tab;

		if (type == MD) {
			selectDetail.call(this, detail);
		} else {
			selectFK.call(this, detail);
		}

		updateDetailGrid.call(this);
	}

	function selectDetail(detail) {
		this.currentForeignKey = undefined;
		this.currentDetail = detail;
		this.view.selectDetail(detail);
	}

	function selectFK(fk) {
		this.currentDetail = undefined;
		this.currentForeignKey = fk;
		this.view.selectForeignKey(fk);
	}

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}
	
	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function onAddDetailButtonClick(o) {
		var me = this,
			w = buildWindow.call(this, {
				entryType: o.classId,
				editable: true
			});

		new CMDBuild.controller.management.common.CMAddDetailWindowController(w, {
			entryType: o.classId,
			cmEditMode: true
		});

		w.show();

		w.mon(w, "destroy", function() {
			this.view.reload();
		}, this, {single: true});
	}

	/*
	 * o = {
	 * 	classId: ...,
	 *  cardId: ...,
	 *  editable: bool
	 * }
	 */
	function buildWindow(o) {
		var me = this,
			c = {
				referencedIdClass: me.card.get("IdClass"),
				fkAttribute: me.currentForeignKey,
				masterData: me.card,
				detail: me.currentDetail,
				classId: o.classId,
				cardId: o.cardId,
				cmEditMode: o.editable,
				withButtons: o.editable
			};

		return new CMDBuild.view.management.classes.masterDetail.DetailWindow(c);
	}
})();