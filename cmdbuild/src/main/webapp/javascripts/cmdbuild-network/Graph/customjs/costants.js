(function($) {
	var costants = function() {
		this.filterForDomain =   {
			attribute:
				{or:
					[
						 {
							 simple:{
								 attribute:"source",
								 operator:"contain",
								 value:[className]
							 }
						 },
						 {
							 simple:
								{
									attribute:"destination",
									operator:"contain",
									value:[className]
								}
						 }
					]
				}
		};
		this.filterForRelation =  {
			attribute:
				{or:
					[
						 {
							 simple:{
								 attribute:"_sourceId",
								 operator:"in",
								 value:[cardId]
							 }
						 },
						 {
							 simple:
								{
									attribute:"_destinationId",
									operator:"in",
									value:[cardId]
								}
						 }
					]
				}
		};
	};
	$.Cmdbuild.g3d.costants = costants;	
})(jQuery);