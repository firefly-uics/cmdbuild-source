(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMTasksAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",

		title: '@@ Tasks',
		cmName: 'tasks',

		buildTreeStructure: function() {
			return [
				this.buildNodeConf({
					id: '1',
					text: '@@ All',
					type:'all'
				}),
				this.buildNodeConf({
					id: '2',
					text: '@@ Email',
					type:'email'
				}),
				this.buildNodeConf({
					id: '3',
					text: '@@ Event',
					type:'event'
				}),
				this.buildNodeConf({
					id: '4',
					text: '@@ Workflow',
					type:'workflow'
				})
			];
		},

		buildNodeConf: function(r) {
			return {
				id: r.id,
				text: r.text,
				leaf: true,
				cmName: 'tasks',
				type: r.type
			};
		}
	});

})();