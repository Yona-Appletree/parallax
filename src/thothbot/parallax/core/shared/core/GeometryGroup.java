/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution 
 * 3.0 Unported License. for more details.
 * 
 * You should have received a copy of the the Creative Commons Attribution 
 * 3.0 Unported License along with Parallax. 
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package thothbot.parallax.core.shared.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.typedarrays.shared.Float64Array;

public class GeometryGroup extends GeometryBuffer
{
	public List<Integer> faces3;
	public List<Integer> faces4;

	public int materialIndex = -1;

	public int vertices;

	public List<Float64Array> __morphTargetsArrays;
	public List<Float64Array> __morphNormalsArrays;

	private Float64Array webGlSkinIndexArray;
	private Float64Array webGlSkinWeightArray;

	public GeometryGroup(int materialIndex, int numMorphTargets, int numMorphNormals) 
	{
		super();

		this.faces3 = new ArrayList<Integer>();
		this.faces4 = new ArrayList<Integer>();
		this.materialIndex = materialIndex;
		this.vertices = 0;
		this.numMorphTargets = numMorphTargets;
		this.numMorphNormals = numMorphNormals;
	}

	public Float64Array getWebGlSkinIndexArray() 
	{
		return webGlSkinIndexArray;
	}

	public Float64Array getWebGlSkinWeightArray() 
	{
		return webGlSkinWeightArray;
	}
	
	public void setWebGlSkinIndexArray(Float64Array a)
	{
		this.webGlSkinIndexArray = a;
	}
	
	public void setWebGlSkinWeightArray(Float64Array a)
	{
		this.webGlSkinWeightArray = a;
	}
	
	@Override
	public void dispose() 
	{
		super.dispose();
		
		setWebGlSkinIndexArray( null );
		setWebGlSkinWeightArray( null );
	}
}
