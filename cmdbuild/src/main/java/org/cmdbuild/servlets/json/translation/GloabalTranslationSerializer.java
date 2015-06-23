package org.cmdbuild.servlets.json.translation;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;

public class GloabalTranslationSerializer {

	private final DataAccessLogic dataLogic;
	private final LookupStore lookupStore;
	private final TranslationLogic logic;
	private final String type;

	public static SerializerBuilder newInstance(){
		return new SerializerBuilder();
	}

	public GloabalTranslationSerializer(SerializerBuilder builder) {
		this.dataLogic = builder.dataLogic;
		this.lookupStore = builder.lookupStore;
		this.logic = builder.translationLogic;
		this.type = builder.type;
	}

	public JsonResponse readStructure(){
		return null;
	}
	
	
	public static final class SerializerBuilder implements Builder<GloabalTranslationSerializer> {
		
		private DataAccessLogic dataLogic;
		private LookupStore lookupStore;
		private TranslationLogic translationLogic;
		private String type; 
		
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
		
		@Override
		public GloabalTranslationSerializer build() {
			return new GloabalTranslationSerializer(this);
		}

	}

}
