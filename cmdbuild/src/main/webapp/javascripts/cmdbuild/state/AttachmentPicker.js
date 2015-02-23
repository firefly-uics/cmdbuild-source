(function() {

	Ext.define('CMDBuild.state.AttachmentPicker', {

		/**
		 * @property {String}
		 */
		currentClassName: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.attachments.PickerWindow} configObject.picker
		 */
		constructor: function(configObject) { // TODO forse il picker non serve
			Ext.apply(this, configObject); // Apply config

			this.currentClassName = null;
			this.currentCardId = null;

			/*
			 * the structure is something
			 * like that:
			 * {
			 * 	classA: {
			 * 		card1: {
			 * 			'filea.jpg': true,
						'fileB.jpg': true
					}
			 * 		card2: {..}
			 * 	},
			 * 	classB: {...}
			 * }
			 */
			this.attachments = {};
		},

		/**
		 * @param {String} cardId
		 */
		setCardId: function(cardId) {
			this.currentCardId = cardId;
		},

		/**
		 * @param {String} className
		 */
		setClassName: function(className) {
			this.currentClassName = className;
		},






		check: function(fileName) {
			ensureExistingCardAttachments(this);
			this.attachments[this.currentClassName][this.currentCardId][fileName] = true;
		},

		uncheck: function(fileName) {
			ensureExistingCardAttachments(this);
			delete this.attachments[this.currentClassName][this.currentCardId][fileName];
		},





		syncSelection: function(records) {
			ensureExistingCardAttachments(this);
			var currentCardAttachments = this.attachments[this.currentClassName][this.currentCardId];
			if (Ext.Object.isEmpty(currentCardAttachments)) {
				return;
			}

			for (var i=0, l=records.length; i<l; ++i) {
				var r = records[i];
				var fileName = r.get('Filename');
				if (currentCardAttachments[fileName]) {
					r.set('Checked', true);
					r.commit();
				}
			}
		},

		getData: function() {
			var out = [];
			for (var className in this.attachments) {
				var classAttachments = this.attachments[className];
				for (var cardId in classAttachments) {
					var cardAttachments = classAttachments[cardId];
					for (var fileName in cardAttachments) {
						out.push({
							className: className,
							cardId: cardId,
							fileName: fileName
						});
					}
				}
			}

			return out;
		}
	});

	function ensureExistingCardAttachments(me) {
		if (typeof me.attachments[me.currentClassName] == 'undefined') {
			me.attachments[me.currentClassName] = {};
		}

		var classAttachments = me.attachments[me.currentClassName];
		if (typeof classAttachments[me.currentCardId] == 'undefined') {
			classAttachments[me.currentCardId] = {};
		}
	}

})();