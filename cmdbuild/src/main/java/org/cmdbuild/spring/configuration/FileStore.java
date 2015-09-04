package org.cmdbuild.spring.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import java.io.File;

import org.cmdbuild.services.DefaultFilesStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.ForwardingFilesStore;
import org.cmdbuild.services.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStore {

	private static class FilesStoreWithLimitations extends ForwardingFilesStore {

		private static final FilesStore UNSUPPORTED_PRIVATE = newProxy(FilesStore.class,
				unsupported("method not supported"));

		private final FilesStore delegate;

		public FilesStoreWithLimitations(final FilesStore delegate) {
			this.delegate = delegate;
		}

		@Override
		protected FilesStore delegate() {
			return UNSUPPORTED_PRIVATE;
		}

		@Override
		public String[] list(final String dir) {
			return delegate.list(dir);
		}

		@Override
		public String[] list(final String dir, final String pattern) {
			return delegate.list(dir, pattern);
		}

		@Override
		public File[] listFiles(final String dir, final String pattern) {
			return delegate.listFiles(dir, pattern);
		}

		@Override
		public String getRelativeRootDirectory() {
			return delegate.getRelativeRootDirectory();
		}

		@Override
		public String getAbsoluteRootDirectory() {
			return delegate.getAbsoluteRootDirectory();
		}

		@Override
		public File getFile(final String path) {
			return delegate.getFile(path);
		}

	}

	public static final String UPLOAD = "upload";

	@Bean(name = UPLOAD)
	public FilesStore uploadFilesStore() {
		return new DefaultFilesStore(Settings.getInstance().getRootPath(), "upload");
	}

	@Bean
	public FilesStore webInfFilesStore() {
		return new FilesStoreWithLimitations(_webInfFilesStore());
	}

	@Bean
	protected FilesStore _webInfFilesStore() {
		return new DefaultFilesStore(Settings.getInstance().getRootPath(), "WEB-INF");
	}

}
