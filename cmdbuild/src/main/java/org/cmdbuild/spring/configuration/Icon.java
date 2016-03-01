package org.cmdbuild.spring.configuration;

import org.cmdbuild.logic.icon.DefaultIconsLogic;
import org.cmdbuild.logic.icon.DefaultIconsLogic.*;
import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.services.FilesStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Icon {

	private static final String ICONS = "icons4classes";

	@Autowired
	private FileStore fileStore;

	@Bean
	public IconsLogic defaultIconsLogic() {
		return new DefaultIconsLogic(iconsFilesStore(), uuidIdGenerator());
	}

	@Bean
	protected FilesStore iconsFilesStore() {
		return fileStore.uploadFilesStore().sub(ICONS);
	}

	@Bean
	protected IdGenerator uuidIdGenerator() {
		return new UuidIdGenerator();
	}

}
