(function() {
	var ID = {
		C1: 1112,
		C2: 1113,
		C3: 1114,
		C4: 1115,
		C5: 1116
	};

	function card(id, conf) {
		var _conf = {
			IdClass: ID[id],
			IdClass_value: id
		};

		return Ext.apply(_conf, conf);
	}

	Ext.define("CMDBuild.test.data.CardsDataSet", {
		statics : {
			getCard: function(id, idClass) {
				var cards = CMDBuild.test.data.CardsDataSet.getCardsFor(idClass);
				for (var i=0, l=cards.length; i<l; ++i) {
					var c = cards[i];
					if (c.Id == id) {
						return c;
					}
				}
			},
			getCardsFor : function(classId) {
				var cards = {};
				// C1 --> C2
				cards[ID.C1] = function() {
					return cards[ID.C2]();
				};
	
				cards[ID.C2] = function() {
					return [card("C2", {
						C11: "Pluto",
						C12: 65,
						C21: 999,
						Id: 4
					})];
				};

				// C3 --> C4, C5
				cards[ID.C3] = function() {
					var c4 = cards[ID.C4]() || [];
					var c5 = cards[ID.C5]() || [];
					return c4.concat(c5);
				};

				cards[ID.C4] = function() {
					return [card("C4", {
						C31: "Pippo",
						C32: 77,
						C41: 35,
						Id: 4
					})];
				};

				cards[ID.C5] = function() {
					return [card("C5", {
						C31: "Pluto",
						C32: 64,
						C51: 7,
						Id: 7
					})];
				};


				return cards[classId]();
			}
		}
	});
})();