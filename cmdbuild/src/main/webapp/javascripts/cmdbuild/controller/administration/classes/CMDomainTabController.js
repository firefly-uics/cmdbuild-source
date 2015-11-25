(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {

		requires: ['CMDBuild.core.proxy.domain.Domain'],

		constructor: function(view) {
			if (Ext.isEmpty(view)) {
				this.view = new CMDBuild.Administration.DomainGrid({
					title : CMDBuild.Translation.administration.modClass.tabs.domains,
					border: false,
					disabled: true
				});
			} else {
				this.view = view;
			}

			this.selection = null;

			this.view.on("itemdblclick", onItemDoubleClick, this);
			this.view.getSelectionModel().on("selectionchange", onSelectionChange, this);
			this.view.addDomainButton.on("click", onAddDomainButton, this);
			this.view.modifyButton.on("click", onModifyDomainButton, this);
			this.view.deleteButton.on("click", onDeleteDomainButton, this);
		},

		getView: function() {
			return this.view;
		},

		onClassSelected: function(classId) {
			this.selection = classId;
			var entryTypeData = _CMCache.getEntryTypeById(classId).data;
			if (entryTypeData.tableType == "simpletable") {
				this.view.disable();
				return;
			}

			var view = this.view;
			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			CMDBuild.core.LoadMask.show();
			view.store.load({
				params: params,
				callback: function() {
					CMDBuild.core.LoadMask.hide();
					view.filterInherited(view.filtering);
				}
			});

			view.enable();
			view.modifyButton.disable();
			view.deleteButton.disable();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.disable();
		}

	});

	function onSelectionChange(sm, selection) {
		if (selection.length > 0) {
			this.currentDomain = selection[0];
			this.view.modifyButton.enable();
			this.view.deleteButton.enable();
		}
	}

	function onItemDoubleClick(grid, record) {
		var domainAccordion = _CMMainViewportController.findAccordionByCMName("domain");
		domainAccordion.expand();
		Ext.Function.createDelayed(function() {
			domainAccordion.selectNodeById(record.get("idDomain"));
		}, 100)();

	}

	function onModifyDomainButton() {
		if (this.currentDomain) {
			onItemDoubleClick(this.view, this.currentDomain);
			Ext.Function.createDelayed(function() {
				_CMMainViewportController.panelControllers["domain"].view.domainForm.enableModify();
			}, 500)();
		}
	}

	function onDeleteDomainButton() {
		Ext.Msg.show({
			title: CMDBuild.Translation.administration.modClass.domainProperties.delete_domain,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteDomain.call(this);
				}
			}
		});
	}

	function deleteDomain() {
		if (this.currentDomain == null) {
			// nothing to delete
			return;
		}

		var params = {};
		params[_CMProxy.parameter.DOMAIN_NAME] = this.currentDomain.get("name");

		CMDBuild.core.proxy.domain.Domain.remove({
			params: params,
			scope: this,
			success: function(response, options, decodedResponse) {
				this.onClassSelected(this.selection);

				_CMCache.onDomainDeleted(this.currentDomain.get("idDomain"));

				this.currentDomain = null;
			}
		});
	}

	function onAddDomainButton() {
		var domainAccordion = _CMMainViewportController.findAccordionByCMName("domain");
		if (domainAccordion) {
			domainAccordion.expandForAdd();
		}
	}
})();