package org.cmdbuild.bim.model.implementation;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.SpaceGeometry;

import com.google.common.collect.Lists;

public class SpaceGeometryImpl implements SpaceGeometry {
	
	private Vector3d centroid;
	private List<Double> dimensions = Lists.newArrayList();
	
	public SpaceGeometryImpl(Vector3d centroid, double xdim, double ydim, double zdim){
		dimensions = Lists.newArrayList(new Double(xdim), new Double(ydim), new Double(zdim));
		this.centroid = centroid;
	}

	public SpaceGeometryImpl() {
		centroid = new Vector3d(0,0,0);
		dimensions.add(new Double(0));
		dimensions.add(new Double(0));
		dimensions.add(new Double(0));
	}
	
	public String toString(){
		return "Centroid: " + centroid + " Dimensions: " + dimensions;
	}

	@Override
	public Vector3d getCentroid() {
		return centroid;
	}

	@Override
	public Double getXDim() {
		return dimensions.get(0);
	}

	@Override
	public Double getYDim() {
		return dimensions.get(1);
	}

	@Override
	public Double getZDim() {
		return dimensions.get(2);
	}

	@Override
	public void setXDim(double xdim) {
		dimensions.set(0, xdim);
	}

	@Override
	public void setYDim(double ydim) {
		dimensions.set(1, ydim);
		
	}

	@Override
	public void setZDim(double zdim) {
		dimensions.set(1, zdim);
		
	}

}
