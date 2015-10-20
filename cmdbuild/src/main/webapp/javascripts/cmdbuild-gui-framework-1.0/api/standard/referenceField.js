(function($) {
	var referenceField = function(param) {
		this.param = param;
		this.backend = undefined;
//		this.isProcess = (param.backend == "ReferenceActivityList");
		this.init = function() {
			try {
				var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
				param.page = 0;
				param.start = 0;
				param.nRows = this.nrows();
//				param.sort = "Description";
//				param.direction = param.direction,
//				var formName = selectMenu.attr("formName");
//				var fieldName = selectMenu.attr("fieldName");
				this.backend = new backendFn(param, this.loadReference, this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.referenceField.init");
				throw e;
			}
		};
		this.loadReference = function() {
			if (this.param.readOnly) {
				this.loadReadOnlyReference();
				return;
			}

			var selectMenu = $("#" + param.id);
			var formName = selectMenu.attr("formName");
			var fieldName = selectMenu.attr("fieldName");
			$.Cmdbuild.CqlManager.resolve(formName, fieldName, function(filter) {
				/*
				 * if filter is undefined or there is an error or 
				 * a field present in the filter is not valorized
				 */
				if (filter == undefined || $.Cmdbuild.CqlManager.isUndefined(filter)) {
					this.chargeEmpty();
					return;
				}
				if (filter) {
					this.param.filter = {
							CQL: filter
					};
					this.backend.filter = this.param.filter;
//				this.backend.cqlFilter = filter;
				}
				this.loadData(this);
				
			}, this);
		};
		this.loadData = function() {
			this.backend.loadData(this.param, this.showReference, this);
		};
		this.nrows = function() {
			//nrows can be personalized for field
			return (this.param.formNRows && this.param.formNRows != "undefined") ? this.param.formNRows : $.Cmdbuild.global.getMaxLookupEntries();
		};
		this.showReference = function() {
			var selectMenu = $("#" + this.param.id);
			var bReference = this.backend.getTotalRows() > this.nrows();
			selectMenu.attr("reference", (bReference) ? "true" : "false");
			if (! bReference) {
				this.chargeCombo();
			} else if (this.param.value) {
				this.chargeValue();
			} else {
				this.chargeEmpty();
			}
			if (bReference) {
				selectMenu.selectmenu({
					  open: function( event, ui ) {
							eval(param.eventSearch);
					  }
				});
			} else {
				selectMenu.unbind( "selectmenuopen");
			}
		};
		this.inFilter = function() {
			if (! this.param.filter) {
				return true;
			}
			for (var i = 0; i < this.backend.data.length; i++) {
				if (this.backend.data[i]._id == this.param.value) {
					return true;
				}
			}
			return false;
		};
		this.chargeEmpty = function() {
			var selectMenu = $("#" + this.param.id);
			var options = [];
			options.push("<option></option>");
			selectMenu.empty();
			selectMenu.append(options.join("")).selectmenu();
			selectMenu.val("");
			this.fieldChanged();
			selectMenu.selectmenu("refresh");
		};
		this.chargeValue = function() {
			var selectMenu = $("#" + this.param.id);
			if ($.Cmdbuild.dataModel.isAProcess(param.className)) {
				$.Cmdbuild.utilities.proxy.getCardProcess(param.className, param.value, {}, function(response, metadata) {
					var options = [];
					options.push("<option selected entryValue='" + response._id + "'>" + response.Description + "</option>");
					selectMenu.empty();
					selectMenu.append(options.join("")).selectmenu();
					selectMenu.val(response.Description);
					this.fieldChanged();
					selectMenu.selectmenu("refresh");
				}, this);
			}
			else {
				$.Cmdbuild.utilities.proxy.getCardData(param.className, param.value, {}, function(response, metadata) {
					var options = [];
					options.push("<option selected entryValue='" + response._id + "'>" + response.Description + "</option>");
					selectMenu.empty();
					selectMenu.append(options.join("")).selectmenu();
					selectMenu.val(response.Description);
					this.fieldChanged();
					selectMenu.selectmenu("refresh");
				}, this);
			}
		};
		this.chargeCombo = function() {
			var options = [];
			var selectMenu = $("#" + this.param.id);
			var entryValue = " entryValue='' ";
			var valueSelected = "";
			options.push("<option " + entryValue + "></option>");
			for (var i = 0; i < this.backend.data.length; i++) {
				var entry = this.backend.data[i];
				if (this.param.value && this.param.value == entry._id) {
					valueSelected = entry.Description;
				}
				entryValue = " entryValue='" + entry._id + "' ";
				options.push("<option " + entryValue + ">" + entry.Description
						+ "</option>");
			}
			selectMenu.empty();
			selectMenu.append(options.join("")).selectmenu();
			selectMenu.val(valueSelected);
			this.fieldChanged();
			selectMenu.selectmenu("refresh");
		};
		this.fieldChanged = function() {
	    	if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
	    		$.Cmdbuild.custom.commands.fieldChanged(this.param);
	    	}
	    	else {
	    		$.Cmdbuild.standard.commands.fieldChanged(this.param);
	    	}
		};
		this.clear = function() {
			if ($("#" + this.param.id).attr("reference") == "true") {
				var options = [];
				options.push("<option entryValue='" + "" + "'>" + "" + "</option>");
				$("#" + this.param.id).append(options.join("")).selectmenu();
			}
			$("#" + this.param.id).val("");
			this.fieldChanged();
			$("#" + this.param.id).selectmenu("refresh");
		};
		this.loadReadOnlyReference = function() {
			if (this.param.value) {
				var input = $("#" + this.param.id);
				if ($.Cmdbuild.dataModel.isAProcess(this.param.className)) {
					$.Cmdbuild.utilities.proxy.getCardProcess(this.param.className, this.param.value, {}, function(response, metadata) {
						input.after(response.Description);
					}, this);
				} else {
					$.Cmdbuild.utilities.proxy.getCardData(this.param.className, this.param.value, {}, function(response, metadata) {
						input.after(response.Description);
					}, this);
				}
			}
		};
		
		this.init();
	};
	$.Cmdbuild.standard.referenceField = referenceField;
	// Statics
	$.Cmdbuild.standard.referenceField.onClearLookup = function(id) {
		$("#" + id)[0].objectField.clear();
	};
	$.Cmdbuild.standard.referenceField.onSearchLookup = function(id, container) {
//		var formName = $("#" + id).attr("formName");
//		var fieldName = $("#" + id).attr("fieldName");
		$.Cmdbuild.standard.commands.navigate({
			container : container,
			form : id + "_dialog",
			dialog : id + "_lookupDialog",
			title : "Reference values",
			width : "90%"
		});
		return true;
	};
	$.Cmdbuild.standard.referenceField.onChangeLookup = function(param) {
		var id = param.toExecCommand;
		var selectMenu = $("#" + id);
		var idValue = $.Cmdbuild.dataModel.getValue(id + "_dialogGrid", "_id");
		var descriptionValue = $.Cmdbuild.dataModel.getValue(id + "_dialogGrid", "Description");
		if (selectMenu.attr("reference") == "true") {
			var options = [];
			options.push("<option entryValue='" + idValue + "' select>" + descriptionValue + "</option>");
			selectMenu.empty();
			selectMenu.append(options.join("")).selectmenu();
		}
		selectMenu.val(descriptionValue);
		selectMenu[0].objectField.fieldChanged();
		selectMenu.selectmenu("refresh");
	};
	$.Cmdbuild.standard.referenceField.chargeLookup = function(param) {
		var selectMenu = $("#" + param.id);
		selectMenu[0].objectField.loadReference();
	};


}) (jQuery);
