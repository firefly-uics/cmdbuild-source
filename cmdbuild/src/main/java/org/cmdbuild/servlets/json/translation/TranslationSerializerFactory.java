package org.cmdbuild.servlets.json.translation;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translation.TranslationSerializerFactory.SerializerBuilder;
import org.json.JSONArray;

public class TranslationSerializerFactory {

	private final DataAccessLogic dataLogic;
	private final LookupStore lookupStore;
	private final TranslationLogic logic;
	private final String type;
	private final JSONArray sorters;
	private final boolean activeOnly;

	public static SerializerBuilder newInstance(){
		return new SerializerBuilder();
	}

	public TranslationSerializerFactory(SerializerBuilder builder) {
		this.dataLogic = builder.dataLogic;
		this.lookupStore = builder.lookupStore;
		this.logic = builder.translationLogic;
		this.type = builder.type;
		this.sorters = builder.sorters;
		this.activeOnly = builder.activeOnly;
	}

	public TranslationSerializer createSerializer(){
		// TODO: remove and move to specific serializers
		if (type.equalsIgnoreCase("class")) {
			return new ClassTranslationSerializer(dataLogic, activeOnly, logic);
		} else if (type.equalsIgnoreCase("process")) {
			return new ProcessTranslationSerializer(dataLogic, activeOnly, logic);
		} else if (type.equalsIgnoreCase("domain")) {
			return new DomainTranslationSerializer();
		} else if (type.equalsIgnoreCase("lookup")) {
			return new LookupTranslationSerializer();
		}
		return null;
	}
	
	
	public static final class SerializerBuilder implements Builder<TranslationSerializerFactory> {
		
		private DataAccessLogic dataLogic;
		private LookupStore lookupStore;
		private TranslationLogic translationLogic;
		private String type; 
		private JSONArray sorters;
		private boolean activeOnly;
		
		public SerializerBuilder withDataAccessLogic(DataAccessLogic dataLogic){
			this.dataLogic = dataLogic;
			return this;
		}
		
		public SerializerBuilder withTranslationLogic(TranslationLogic translationLogic){
			this.translationLogic = translationLogic;
			return this;
		}
		
		public SerializerBuilder withLookupStore(LookupStore lookupStore){
			this.lookupStore = lookupStore;
			return this;
		}
		
		public SerializerBuilder withType(String type){
			this.type = type;
			return this;
		}
		
		public SerializerBuilder withSorters(JSONArray sorters) {
			this.sorters = sorters;
			return this;
		}
		
		public SerializerBuilder withActiveOnly(boolean activeOnly) {
			this.activeOnly = activeOnly;
			return this;
		}
		
		@Override
		public TranslationSerializerFactory build() {
			return new TranslationSerializerFactory(this);
		}

	}

}
