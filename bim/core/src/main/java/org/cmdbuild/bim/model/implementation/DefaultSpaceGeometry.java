package org.cmdbuild.bim.model.implementation;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;

import com.google.common.collect.Lists;

public class DefaultSpaceGeometry implements SpaceGeometry {
	
	private Vector3d centroid;
	private List<Double> dimensions = Lists.newArrayList();
	private List<Vector3d> vertexList = Lists.newArrayList();
	
	public DefaultSpaceGeometry(Vector3d centroid, double xdim, double ydim, double zdim){
		vertexList.add(new Vector3d(centroid.x - xdim/2, centroid.y - ydim/2, centroid.z));
		vertexList.add(new Vector3d(centroid.x + xdim/2, centroid.y - ydim/2, centroid.z));
		vertexList.add(new Vector3d(centroid.x + xdim/2, centroid.y + ydim/2, centroid.z));
		vertexList.add(new Vector3d(centroid.x - xdim/2, centroid.y + ydim/2, centroid.z));
		vertexList.add(new Vector3d(centroid.x - xdim/2, centroid.y - ydim/2, centroid.z+zdim));
		vertexList.add(new Vector3d(centroid.x + xdim/2, centroid.y - ydim/2, centroid.z+zdim));
		vertexList.add(new Vector3d(centroid.x + xdim/2, centroid.y + ydim/2, centroid.z+zdim));
		vertexList.add(new Vector3d(centroid.x - xdim/2, centroid.y + ydim/2, centroid.z+zdim));
	}
	
	public DefaultSpaceGeometry(List<Position3d> polylineProfile, double dz){
		for(Position3d position3d : polylineProfile){
			vertexList.add(position3d.getOrigin());
		}
	}
	
	public DefaultSpaceGeometry() {
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
	public double getXDim() {
		return dimensions.get(0);
	}

	@Override
	public double getYDim() {
		return dimensions.get(1);
	}

	@Override
	public double getZDim() {
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

	@Override
	public List<Vector3d> getVertexList() {
		return vertexList;
	}

}
