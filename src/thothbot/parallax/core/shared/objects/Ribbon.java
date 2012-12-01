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

package thothbot.parallax.core.shared.objects;

import java.util.List;

import com.google.gwt.typedarrays.client.Float64ArrayNative;

import thothbot.parallax.core.client.gl2.WebGLConstants;
import thothbot.parallax.core.client.gl2.WebGLRenderingContext;
import thothbot.parallax.core.client.gl2.enums.BeginMode;
import thothbot.parallax.core.client.gl2.enums.BufferTarget;
import thothbot.parallax.core.client.gl2.enums.BufferUsage;
import thothbot.parallax.core.client.renderers.WebGlRendererInfo;
import thothbot.parallax.core.client.renderers.WebGLRenderer;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.GeometryBuffer;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.materials.Material;

public class Ribbon extends GeometryObject
{
	public Ribbon(Geometry geometry, Material material) 
	{
		this.geometry = geometry;
		this.material = material;
	}

	@Override
	public void setBuffer(WebGLRenderer renderer)
	{
		if ( this.getGeometry().isVerticesNeedUpdate() || this.getGeometry().isColorsNeedUpdate() )
			this.setBuffers( renderer, geometry, BufferUsage.DYNAMIC_DRAW );

		this.getGeometry().setVerticesNeedUpdate(false);
		this.getGeometry().setColorsNeedUpdate(false);
	}

	@Override
	public void renderBuffer(WebGLRenderer renderer, GeometryBuffer geometryBuffer, boolean updateBuffers)
	{
		WebGLRenderingContext gl = renderer.getGL();
		WebGlRendererInfo info = renderer.getInfo();
		
		gl.drawArrays( BeginMode.TRIANGLE_STRIP, 0, geometryBuffer.__webglVertexCount );

		info.getRender().calls ++;
	}
	
	public void initBuffer(WebGLRenderer renderer)
	{
		Geometry geometry = this.getGeometry();

		if( geometry.__webglVertexBuffer == null ) 
		{
			createBuffers( renderer, geometry );
			initBuffers( renderer.getGL(), geometry );

			geometry.setVerticesNeedUpdate(true);
			geometry.setColorsNeedUpdate(true);
		}
	}
	
	private void createBuffers(WebGLRenderer renderer, Geometry geometry)
	{
		WebGLRenderingContext gl = renderer.getGL();
		WebGlRendererInfo info = renderer.getInfo();
		
		geometry.__webglVertexBuffer =  gl.createBuffer();
		geometry.__webglColorBuffer =  gl.createBuffer();
		
		info.getMemory().geometries ++;
	}
	
	private void initBuffers(WebGLRenderingContext gl, Geometry geometry)
	{
		int nvertices = geometry.getVertices().size();

		geometry.setWebGlVertexArray( Float64ArrayNative.create( nvertices * 3 ) );
		geometry.setWebGlVertexArray( Float64ArrayNative.create( nvertices * 3 ) );

		geometry.__webglVertexCount = nvertices;
	}

	// setRibbonBuffers
	public void setBuffers(WebGLRenderer renderer, Geometry geometry, BufferUsage hint)
	{
		WebGLRenderingContext gl = renderer.getGL();
		
		List<Vector3> vertices = geometry.getVertices();
		List<Color> colors = geometry.getColors();

		boolean dirtyVertices = geometry.isVerticesNeedUpdate();
		boolean dirtyColors = geometry.isColorsNeedUpdate();

		if (dirtyVertices) 
		{
			for (int v = 0; v < vertices.size(); v++) 
			{
				Vector3 vertex = vertices.get(v);

				int offset = v * 3;

				geometry.getWebGlVertexArray().set(offset, vertex.getX());
				geometry.getWebGlVertexArray().set(offset + 1, vertex.getY());
				geometry.getWebGlVertexArray().set(offset + 2, vertex.getZ());
			}

			gl.bindBuffer(BufferTarget.ARRAY_BUFFER, geometry.__webglVertexBuffer);
			gl.bufferData(BufferTarget.ARRAY_BUFFER, geometry.getWebGlVertexArray(), hint);
		}

		if (dirtyColors) 
		{
			for (int c = 0; c < colors.size(); c++) 
			{

				Color color = colors.get(c);

				int offset = c * 3;

				geometry.getWebGlColorArray().set(offset, color.getR());
				geometry.getWebGlColorArray().set(offset + 1, color.getG());
				geometry.getWebGlColorArray().set(offset + 2, color.getB());

			}

			gl.bindBuffer(BufferTarget.ARRAY_BUFFER, geometry.__webglColorBuffer);
			gl.bufferData(BufferTarget.ARRAY_BUFFER, geometry.getWebGlColorArray(), hint);
		}
	}
}
