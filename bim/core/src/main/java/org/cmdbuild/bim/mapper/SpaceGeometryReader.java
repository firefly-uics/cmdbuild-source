package org.cmdbuild.bim.mapper;

import org.cmdbuild.bim.model.SpaceGeometry;

public interface SpaceGeometryReader {

	SpaceGeometry computeCentroid(String spaceIdentifier);
	
}
