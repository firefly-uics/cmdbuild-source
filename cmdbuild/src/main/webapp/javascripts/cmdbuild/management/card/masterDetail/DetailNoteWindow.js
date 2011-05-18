CMDBuild.Management.DetailNoteWindow = Ext.extend(CMDBuild.PopupWindow, {
    masterCard: null,
    initComponent: function() {
	    this.title = String.format("{0} - {1}"
	    		, CMDBuild.Translation.management.modcard.tabs.notes 
	    		, this.masterCard.Description);
	    
	    var note = new CMDBuild.Management.CardNotesTab({
		    withButtons: false
	    });

	    var saveButton = new Ext.Button({
	        text: CMDBuild.Translation.common.buttons.save,
	        handler: note.saveNote,
	        scope: note,
	        disabled: true
	    });

	    var cancelButton = new Ext.Button({
	        text: CMDBuild.Translation.common.buttons.abort,
	        handler: note.disableModify,
	        scope: note,
	        disabled: true
	    });

	    var closeButton = new Ext.Button({
	        text: CMDBuild.Translation.common.buttons.close,
	        name: 'saveButton',
	        formBind: true,
	        handler: function() {
		        this.destroy();
	        },
	        scope: this
	    });

	    this.items = [note];
	    this.buttons = [ saveButton, cancelButton, closeButton ];
	    this.buttonAlign = "center";
	    CMDBuild.Management.DetailNoteWindow.superclass.initComponent
	            .call(this);

	    this.on("show", function() {
		    note.loadCard({
			    record: new Ext.data.Record(this.masterCard)
		    });
	    }, this);

	    note.on('saved', function() {
		    this.destroy();
	    }, this);

	    note.actualForm.on("activate", function() {
		    saveButton.enable();
		    cancelButton.enable();
	    });

	    note.actualForm.on("deactivate", function() {
		    saveButton.disable();
		    cancelButton.disable();
	    });
    }
});