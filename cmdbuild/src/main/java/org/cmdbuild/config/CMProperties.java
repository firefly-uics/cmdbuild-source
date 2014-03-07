package org.cmdbuild.config;

import java.util.Properties;

@SuppressWarnings("serial")
public abstract class CMProperties extends Properties {

	abstract void accept (final PropertiesVisitor visitor);

}
