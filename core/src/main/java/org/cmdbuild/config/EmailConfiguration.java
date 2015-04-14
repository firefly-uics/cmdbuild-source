package org.cmdbuild.config;

public interface EmailConfiguration {

	long getQueueTime();

	void setQueueTime(long value);

	void save();

}
