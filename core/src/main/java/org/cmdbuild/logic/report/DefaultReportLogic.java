package org.cmdbuild.logic.report;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.report.ReportFactory.ReportType.CUSTOM;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DefaultReportLogic implements ReportLogic {

	private static class ReportImpl implements Report {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ReportImpl> {

			private int id;
			private String title;
			private String type;
			private String description;

			@Override
			public ReportImpl build() {
				return new ReportImpl(this);
			}

			public Builder setId(final int id) {
				this.id = id;
				return this;
			}

			public Builder setTitle(final String title) {
				this.title = title;
				return this;
			}

			public Builder setType(final String type) {
				this.type = type;
				return this;
			}

			public Builder setDescription(final String description) {
				this.description = description;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final int id;
		private final String title;
		private final String type;
		private final String description;

		private ReportImpl(final Builder builder) {
			this.id = builder.id;
			this.title = builder.title;
			this.type = builder.type;
			this.description = builder.description;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Report)) {
				return false;
			}
			final Report other = Report.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getTitle(), other.getTitle()) //
					.append(this.getType(), other.getType()) //
					.append(this.getDescription(), other.getDescription()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(id) //
					.append(title) //
					.append(type) //
					.append(description) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private final ReportStore reportStore;

	public DefaultReportLogic(final ReportStore reportStore) {
		this.reportStore = reportStore;
	}

	@Override
	public Iterable<Report> readAll() {
		return from(reportStore.findReportsByType(CUSTOM)) //
				.filter(new Predicate<org.cmdbuild.model.Report>() {

					@Override
					public boolean apply(final org.cmdbuild.model.Report input) {
						return input.isUserAllowed();
					}

				}) //
				.transform(new Function<org.cmdbuild.model.Report, Report>() {

					@Override
					public Report apply(final org.cmdbuild.model.Report input) {
						return ReportImpl.newInstance() //
								.setId(input.getId()) //
								.setTitle(input.getCode()) //
								// TODO do it better
								.setType(input.getType().toString()) //
								.setDescription(input.getDescription()) //
								.build();
					}

				});

	}

}
