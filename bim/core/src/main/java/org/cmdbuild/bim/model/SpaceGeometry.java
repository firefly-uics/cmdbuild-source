package org.cmdbuild.bim.model;

import javax.vecmath.Vector3d;

public interface SpaceGeometry {

	Vector3d getCentroid();

	Double getXDim();

	Double getYDim();

	Double getZDim();

	void setXDim(double xdim);

	void setYDim(double ydim);

	void setZDim(double zdim);

}
