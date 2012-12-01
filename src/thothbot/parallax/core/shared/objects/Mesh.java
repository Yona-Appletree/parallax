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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thothbot.parallax.core.client.gl2.WebGLBuffer;
import thothbot.parallax.core.client.gl2.WebGLRenderingContext;
import thothbot.parallax.core.client.gl2.enums.BeginMode;
import thothbot.parallax.core.client.gl2.enums.BufferTarget;
import thothbot.parallax.core.client.gl2.enums.BufferUsage;
import thothbot.parallax.core.client.gl2.enums.DrawElementsType;
import thothbot.parallax.core.client.renderers.WebGlRendererInfo;
import thothbot.parallax.core.client.renderers.WebGLRenderer;
import thothbot.parallax.core.client.shaders.Attribute;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Face3;
import thothbot.parallax.core.shared.core.Face4;
import thothbot.parallax.core.shared.core.FastMap;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.GeometryBuffer;
import thothbot.parallax.core.shared.core.GeometryGroup;
import thothbot.parallax.core.shared.core.UV;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.core.Vector4;
import thothbot.parallax.core.shared.materials.HasSkinning;
import thothbot.parallax.core.shared.materials.HasWireframe;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;

import com.google.gwt.core.client.GWT;
import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Uint16ArrayNative;
import com.google.gwt.typedarrays.shared.Float64Array;

/**
 * Base class for Mesh objects.
 * 
 * @author thothbot
 *
 */
public class Mesh extends GeometryObject
{
	private Boolean overdraw;
	private Integer morphTargetBase = null;
	private List<Double> morphTargetInfluences;
	private List<Integer> morphTargetForcedOrder;
	private Map<String, Integer> morphTargetDictionary;
	public Float64Array __webglMorphTargetInfluences;

	private static MeshBasicMaterial defaultMaterial = new MeshBasicMaterial();
	static {
		defaultMaterial.setColor( new Color((int) Math.random() * 0xffffff) );
		defaultMaterial.setWireframe( true );
	};

	public Mesh(Geometry geometry) 
	{
		this(geometry, Mesh.defaultMaterial);
	}
	
	public Mesh(GeometryBuffer geometry, Material material) 
	{
		this(material);
		this.geometryBuffer = geometry;
	}
	
	public Mesh(Geometry geometry, Material material) 
	{
		this(material);
		this.geometry = geometry;
		
		if (this.geometry != null) 
		{
			// calc bound radius
			if (this.geometry.getBoundingSphere() == null)
				this.geometry.computeBoundingSphere();

			this.boundRadius = this.geometry.getBoundingSphere().radius;

			// setup morph targets
			if (this.geometry.getMorphTargets().size() != 0) 
			{
				this.morphTargetBase = -1;
				this.morphTargetForcedOrder = new ArrayList<Integer>();
				this.morphTargetInfluences = new ArrayList<Double>();
				this.morphTargetDictionary = GWT.isScript() ? 
						new FastMap<Integer>() : new HashMap<String, Integer>();

				List<Geometry.MorphTarget> morphTargets = this.geometry.getMorphTargets();
				for (int m = 0; m < morphTargets.size(); m++) 
				{
					this.morphTargetInfluences.add(0.0);
					this.morphTargetDictionary.put(morphTargets.get(m).name, m);
				}
			}
		}
	}
	
	protected Mesh(Material material)
	{
		super();
		
		this.material = material;
	}
	
	public boolean getOverdraw()
	{
		return this.overdraw;
	}

	public void setOverdraw(boolean overdraw)
	{
		this.overdraw = overdraw;
	}

	/**
	 * Get Morph Target Index by Name
	 */
	public int getMorphTargetIndexByName(String name)
	{
		if (this.morphTargetDictionary.containsKey(name))
			return this.morphTargetDictionary.get(name);

		Log.debug("Mesh.getMorphTargetIndexByName: morph target " + name
				+ " does not exist. Returning 0.");
		return 0;
	}

	public Integer getMorphTargetBase() {
		return morphTargetBase;
	}
	
	public List<Double> getMorphTargetInfluences() {
		return this.morphTargetInfluences;
	}
	
	public List<Integer> getMorphTargetForcedOrder() {
		return this.morphTargetForcedOrder;
	}

	@Override
	public void renderBuffer(WebGLRenderer renderer, GeometryBuffer geometryBuffer, boolean updateBuffers)
	{
		WebGLRenderingContext gl = renderer.getGL();
		WebGlRendererInfo info = renderer.getInfo();

		// wireframe
		if ( getMaterial() instanceof HasWireframe && ((HasWireframe)getMaterial()).isWireframe() ) 
		{
			setLineWidth( gl, ((HasWireframe)getMaterial()).getWireframeLineWidth() );

			if ( updateBuffers ) 
				gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryBuffer.__webglLineBuffer );
			
			gl.drawElements( BeginMode.LINES, geometryBuffer.__webglLineCount, DrawElementsType.UNSIGNED_SHORT, 0 );

			// triangles

		}
		else 
		{
			if ( updateBuffers ) 
				gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryBuffer.__webglFaceBuffer );
			
			gl.drawElements( BeginMode.TRIANGLES, geometryBuffer.__webglFaceCount, DrawElementsType.UNSIGNED_SHORT, 0 );
		}
		
		info.getRender().calls ++;
		info.getRender().vertices += geometryBuffer.__webglFaceCount;
		info.getRender().faces += geometryBuffer.__webglFaceCount / 3;
	}

	/*
	 * Returns geometry quantities
	 */
	@Override
	public void initBuffer(WebGLRenderer renderer) 
	{
		WebGlRendererInfo info = renderer.getInfo();
	
		Geometry geometry = this.getGeometry();

		if(geometryBuffer != null)
		{
			createBuffers(renderer, geometryBuffer );
		}
		else if(geometry instanceof Geometry) 
		{
			Log.debug("addObject() geometry.geometryGroups is null: " + ( geometry.getGeometryGroups() == null ));
			if ( geometry.getGeometryGroups() == null )
				sortFacesByMaterial( this.getGeometry() );

			// create separate VBOs per geometry chunk
			for ( GeometryGroup geometryGroup : geometry.getGeometryGroups() ) 
			{
				// initialise VBO on the first access
				if ( geometryGroup.__webglVertexBuffer == null ) 
				{
					createBuffers(renderer, geometryGroup );
					initBuffers(renderer.getGL(), geometryGroup );
					info.getMemory().geometries++;

					geometry.setVerticesNeedUpdate(true);
					geometry.setMorphTargetsNeedUpdate(true);
					geometry.setElementsNeedUpdate(true);
					geometry.setUvsNeedUpdate(true);
					geometry.setNormalsNeedUpdate(true);
					geometry.setTangentsNeedUpdate(true);
					geometry.setColorsNeedUpdate(true);
				}
			}
		}
			
	}

	// initMeshBuffers
	private void initBuffers(WebGLRenderingContext gl, GeometryGroup geometryGroup)
	{
		Geometry geometry = this.geometry;

		List<Integer> faces3 = geometryGroup.faces3;
		List<Integer> faces4 = geometryGroup.faces4;

		int nvertices = faces3.size() * 3 + faces4.size() * 4;
		int ntris = faces3.size() * 1 + faces4.size() * 2;
		int nlines = faces3.size() * 3 + faces4.size() * 4;

		Material material = Material.getBufferMaterial(this, geometryGroup);

		boolean uvType = material.bufferGuessUVType();
		Material.SHADING normalType = material.bufferGuessNormalType();
		Material.COLORS vertexColorType = material.bufferGuessVertexColorType();

		geometryGroup.setWebGlVertexArray( Float64ArrayNative.create(nvertices * 3) );

		if (normalType != null)
			geometryGroup.setWebGlNormalArray( Float64ArrayNative.create(nvertices * 3) );

		if (geometry.hasTangents())
			geometryGroup.setWebGlTangentArray( Float64ArrayNative.create(nvertices * 4) );

		if (vertexColorType != null)
			geometryGroup.setWebGlColorArray( Float64ArrayNative.create(nvertices * 3) );

		if (uvType) 
		{
			if (geometry.getFaceUvs().size() > 0 || geometry.getFaceVertexUvs().size() > 0)
				geometryGroup.setWebGlUvArray( Float64ArrayNative.create(nvertices * 2) );

			if (geometry.getFaceUvs().size() > 1 || geometry.getFaceVertexUvs().size() > 1)
				geometryGroup.setWebGlUv2Array( Float64ArrayNative.create(nvertices * 2) );
		}

		if (this.geometry.getSkinWeights().size() > 0 && this.geometry.getSkinIndices().size() > 0) 
		{
			geometryGroup.setWebGlSkinIndexArray  ( Float64ArrayNative.create(nvertices * 4) );
			geometryGroup.setWebGlSkinWeightArray ( Float64ArrayNative.create(nvertices * 4) );
		}

		geometryGroup.setWebGlFaceArray( Uint16ArrayNative.create(ntris * 3) );
		geometryGroup.setWebGlLineArray( Uint16ArrayNative.create(nlines * 2) );

		if (geometryGroup.numMorphTargets > 0) 
		{
			geometryGroup.__morphTargetsArrays = new ArrayList<Float64Array>();

			for (int m = 0; m < geometryGroup.numMorphTargets; m++)
				geometryGroup.__morphTargetsArrays.add(Float64ArrayNative.create(nvertices * 3));
		}

		if (geometryGroup.numMorphNormals > 0) 
		{
			geometryGroup.__morphNormalsArrays = new ArrayList<Float64Array>();

			for (int m = 0; m < geometryGroup.numMorphNormals; m++)
				geometryGroup.__morphNormalsArrays.add(Float64ArrayNative.create(nvertices * 3));
		}

		geometryGroup.__webglFaceCount = ntris * 3;
		geometryGroup.__webglLineCount = nlines * 2;

		// custom attributes
		Map<String, Attribute> attributes = material.getShader().getAttributes();
		
		if (attributes != null) 
		{
			if (geometryGroup.__webglCustomAttributesList == null)
				geometryGroup.__webglCustomAttributesList = new ArrayList<Attribute>();

			for (String a : attributes.keySet()) 
			{
				Attribute originalAttribute = attributes.get(a);

				// Do a shallow copy of the attribute object so different
				// geometryGroup chunks use different
				// attribute buffers which are correctly indexed in the
				// setMeshBuffers function
				Attribute attribute = originalAttribute.clone();

				if (!attribute.__webglInitialized || attribute.createUniqueBuffers) 
				{
					attribute.__webglInitialized = true;

					int size = 1; // "f" and "i"

					if (attribute.type == Attribute.TYPE.V2)
						size = 2;
					else if (attribute.type == Attribute.TYPE.V3)
						size = 3;
					else if (attribute.type == Attribute.TYPE.V4)
						size = 4;
					else if (attribute.type == Attribute.TYPE.C)
						size = 3;

					attribute.size = size;

					attribute.array = Float64ArrayNative.create(nvertices * size);

					attribute.buffer = gl.createBuffer();
					attribute.belongsToAttribute = a;

					originalAttribute.needsUpdate = true;
				}

				geometryGroup.__webglCustomAttributesList.add(attribute);
			}
		}

		geometryGroup.setArrayInitialized(true);
	}

	// createMeshBuffers
	private void createBuffers(WebGLRenderer renderer, GeometryBuffer geometryGroup)
	{
		WebGLRenderingContext gl = renderer.getGL();
		
		geometryGroup.__webglVertexBuffer = gl.createBuffer();
		geometryGroup.__webglNormalBuffer = gl.createBuffer();
		geometryGroup.__webglTangentBuffer = gl.createBuffer();
		geometryGroup.__webglColorBuffer = gl.createBuffer();
		geometryGroup.__webglUVBuffer = gl.createBuffer();
		geometryGroup.__webglUV2Buffer = gl.createBuffer();

		geometryGroup.__webglSkinIndicesBuffer = gl.createBuffer();
		geometryGroup.__webglSkinWeightsBuffer = gl.createBuffer();

		geometryGroup.__webglIndexBuffer = gl.createBuffer();
		geometryGroup.__webglFaceBuffer = gl.createBuffer();
		geometryGroup.__webglLineBuffer = gl.createBuffer();

		if (geometryGroup.numMorphTargets != 0) {
			geometryGroup.__webglMorphTargetsBuffers = new ArrayList<WebGLBuffer>();

			for (int m = 0; m < geometryGroup.numMorphTargets; m++) {
				geometryGroup.__webglMorphTargetsBuffers.add(gl.createBuffer());
			}
		}

		if (geometryGroup.numMorphNormals != 0) {
			geometryGroup.__webglMorphNormalsBuffers = new ArrayList<WebGLBuffer>();

			for (int m = 0; m < geometryGroup.numMorphNormals; m++) {
				geometryGroup.__webglMorphNormalsBuffers.add(gl.createBuffer());
			}
		}
	}

	@Override
	public void setBuffer(WebGLRenderer renderer) 
	{
		WebGLRenderingContext gl = renderer.getGL();

		if ( geometryBuffer != null ) 
		{
			if ( geometryBuffer.isVerticesNeedUpdate() || geometryBuffer.isElementsNeedUpdate() ||
				geometryBuffer.isUvsNeedUpdate() || geometryBuffer.isNormalsNeedUpdate() ||
				geometryBuffer.isColorsNeedUpdate() || geometryBuffer.isTangentsNeedUpdate() ) 
			{
				((GeometryBuffer)geometryBuffer).setDirectBuffers( renderer.getGL(), BufferUsage.DYNAMIC_DRAW, !geometryBuffer.isDynamic() );
			}

			geometryBuffer.setVerticesNeedUpdate(false);
			geometryBuffer.setElementsNeedUpdate(false);
			geometryBuffer.setUvsNeedUpdate(false);
			geometryBuffer.setNormalsNeedUpdate(false);
			geometryBuffer.setColorsNeedUpdate(false);
			geometryBuffer.setTangentsNeedUpdate(false);
		} 
		else 
		{
			// check all geometry groups

			for( GeometryGroup geometryGroup : geometry.getGeometryGroups() ) 
			{
				// TODO: try to make object's material
				Material material = Material.getBufferMaterial( this, geometryGroup );

				boolean areCustomAttributesDirty = material.getShader().areCustomAttributesDirty();
				if ( geometry.isVerticesNeedUpdate() 
						|| geometry.isMorphTargetsNeedUpdate()
						|| geometry.isElementsNeedUpdate() 
						|| geometry.isUvsNeedUpdate()      
						|| geometry.isNormalsNeedUpdate()      
						|| geometry.isColorsNeedUpdate()   
						|| geometry.isTangentsNeedUpdate()      
						|| areCustomAttributesDirty
				) {
					setBuffers( gl, geometryGroup, BufferUsage.DYNAMIC_DRAW, material);
					material.getShader().clearCustomAttributes();
				}
			}

			geometry.setVerticesNeedUpdate(false);
			geometry.setMorphTargetsNeedUpdate(false);
			geometry.setElementsNeedUpdate(false);
			geometry.setUvsNeedUpdate(false);
			geometry.setNormalsNeedUpdate(false);
			geometry.setColorsNeedUpdate(false);
			geometry.setTangentsNeedUpdate(false);
		}
	}

	// setMeshBuffers
	private void setBuffers(WebGLRenderingContext gl, GeometryGroup geometryGroup, BufferUsage hint, Material material)
	{
		Log.debug("Called Mesh.setBuffers() - material=" + material.getId() + ", " + material.getClass().getName());

		if ( ! geometryGroup.isArrayInitialized() )
			 return;
				
		 Material.SHADING normalType = material.bufferGuessNormalType();
		 Material.COLORS vertexColorType = material.bufferGuessVertexColorType();
		 boolean uvType = material.bufferGuessUVType();
		
		 boolean needsSmoothNormals = ( normalType == Material.SHADING.SMOOTH );
										 
		 List<Integer> chunk_faces3 = geometryGroup.faces3;
		 List<Integer> chunk_faces4 = geometryGroup.faces4;
		 List<Face3> obj_faces = getGeometry().getFaces();

		 List<List<UV>> obj_uvs = (getGeometry().getFaceVertexUvs().size() > 0) 
				 ? getGeometry().getFaceVertexUvs().get(0) : null;

		 List<List<UV>> obj_uvs2 = (getGeometry().getFaceVertexUvs().size() > 1) 
				 ? getGeometry().getFaceVertexUvs().get(1) : null;
						
		 List<Geometry.MorphNormal> morphNormals = getGeometry().getMorphNormals();
		 List<Geometry.MorphTarget> morphTargets = getGeometry().getMorphTargets();
		 
		 if ( getGeometry().isVerticesNeedUpdate() ) 
		 {
			 Float64Array vertexArray = geometryGroup.getWebGlVertexArray();
			 int offset = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {
				 Face3 face = obj_faces.get( chunk_faces3.get( f ) );
				 
				 Vector3 v1 = getGeometry().getVertices().get( face.getA() );
				 Vector3 v2 = getGeometry().getVertices().get( face.getB() );
				 Vector3 v3 = getGeometry().getVertices().get( face.getC() );

				 vertexArray.set(offset,  v1.getX());
				 vertexArray.set(offset + 1, v1.getY());
				 vertexArray.set(offset + 2, v1.getZ());

				 vertexArray.set(offset + 3, v2.getX());
				 vertexArray.set(offset + 4, v2.getY());
				 vertexArray.set(offset + 5, v2.getZ());

				 vertexArray.set(offset + 6, v3.getX());
				 vertexArray.set(offset + 7, v3.getY());
				 vertexArray.set(offset + 8, v3.getZ());

				 offset += 9;
			 }
			 
			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {
				 Face4 face = (Face4) obj_faces.get( chunk_faces4.get( f ));

				 Vector3 v1 = getGeometry().getVertices().get( face.getA() );
				 Vector3 v2 = getGeometry().getVertices().get( face.getB() );
				 Vector3 v3 = getGeometry().getVertices().get( face.getC() );
				 Vector3 v4 = getGeometry().getVertices().get( face.getD() );

				 vertexArray.set(offset, v1.getX());
				 vertexArray.set(offset + 1, v1.getY());
				 vertexArray.set(offset + 2, v1.getZ());

				 vertexArray.set(offset + 3, v2.getX());
				 vertexArray.set(offset + 4, v2.getY());
				 vertexArray.set(offset + 5, v2.getZ());

				 vertexArray.set(offset + 6, v3.getX());
				 vertexArray.set(offset + 7, v3.getY());
				 vertexArray.set(offset + 8, v3.getZ());

				 vertexArray.set(offset + 9, v4.getX());
				 vertexArray.set(offset + 10, v4.getY());
				 vertexArray.set(offset + 11, v4.getZ());

				 offset += 12;
			 }

			 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglVertexBuffer);
			 gl.bufferData( BufferTarget.ARRAY_BUFFER, vertexArray, hint );
		 }
		 
		 if ( getGeometry().isMorphTargetsNeedUpdate() ) 
		 {
			 
			 for ( int vk = 0, vkl = getGeometry().getMorphTargets().size(); vk < vkl; vk ++ ) 
			 {
				 int offset_morphTarget = 0;

				 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
				 {
					 int chf = chunk_faces3.get( f );
					 Face3 face = obj_faces.get( chf );

					 // morph positions

					 Geometry.MorphTarget d1 = morphTargets.get( vk ); 
					 Vector3 v1 = d1.vertices.get( face.getA() );
					 Vector3 v2 = morphTargets.get( vk ).vertices.get( face.getB() );
					 Vector3 v3 = morphTargets.get( vk ).vertices.get( face.getC() );

					 Float64Array vka = geometryGroup.__morphTargetsArrays.get(vk);

					 vka.set(offset_morphTarget, v1.getX());
					 vka.set(offset_morphTarget + 1, v1.getY());
					 vka.set(offset_morphTarget + 2, v1.getZ());

					 vka.set(offset_morphTarget + 3, v2.getX());
					 vka.set(offset_morphTarget + 4, v2.getY());
					 vka.set(offset_morphTarget + 5, v2.getZ());

					 vka.set(offset_morphTarget + 6, v3.getX());
					 vka.set(offset_morphTarget + 7, v3.getY());
					 vka.set(offset_morphTarget + 8, v3.getZ());

					 // morph normals

					 if ( material instanceof HasSkinning && ((HasSkinning)material).isMorphNormals() ) 
					 {
						 Vector3 n1, n2, n3;
						 if ( needsSmoothNormals ) 
						 {
							 Geometry.VertextNormal faceVertexNormals = morphNormals.get( vk ).vertexNormals.get( chf );

							 n1 = faceVertexNormals.a;
							 n2 = faceVertexNormals.b;
							 n3 = faceVertexNormals.c;
						 } 
						 else 
						 {
							 n1 = morphNormals.get( vk ).faceNormals.get( chf );
							 n2 = n1;
							 n3 = n1;
						 }

						 Float64Array nka = geometryGroup.__morphNormalsArrays.get( vk );

						 nka.set(offset_morphTarget, n1.getX());
						 nka.set(offset_morphTarget + 1, n1.getY());
						 nka.set(offset_morphTarget + 2, n1.getZ());

						 nka.set(offset_morphTarget + 3, n2.getX());
						 nka.set(offset_morphTarget + 4, n2.getY());
						 nka.set(offset_morphTarget + 5, n2.getZ());

						 nka.set(offset_morphTarget + 6, n3.getX());
						 nka.set(offset_morphTarget + 7, n3.getY());
						 nka.set(offset_morphTarget + 8, n3.getZ());
					 }

					 //

					 offset_morphTarget += 9;

				 }

				 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
				 {
					 int chf = chunk_faces4.get(f);
					 Face4 face = (Face4) obj_faces.get(chf);

					 // morph positions

					 Vector3 v1 = morphTargets.get(vk).vertices.get(face.getA());
					 Vector3 v2 = morphTargets.get(vk).vertices.get(face.getB());
					 Vector3 v3 = morphTargets.get(vk).vertices.get(face.getC());
					 Vector3 v4 = morphTargets.get(vk).vertices.get(face.getD());

					 Float64Array vka = geometryGroup.__morphTargetsArrays.get(vk);

					 vka.set(offset_morphTarget, v1.getX());
					 vka.set(offset_morphTarget + 1, v1.getY());
					 vka.set(offset_morphTarget + 2, v1.getZ());

					 vka.set(offset_morphTarget + 3, v2.getX());
					 vka.set(offset_morphTarget + 4, v2.getY());
					 vka.set(offset_morphTarget + 5, v2.getZ());

					 vka.set(offset_morphTarget + 6, v3.getX());
					 vka.set(offset_morphTarget + 7, v3.getY());
					 vka.set(offset_morphTarget + 8, v3.getZ());

					 vka.set(offset_morphTarget + 9, v4.getX());
					 vka.set(offset_morphTarget + 10, v4.getY());
					 vka.set(offset_morphTarget + 11, v4.getZ());

					 // morph normals

					 if (  material instanceof HasSkinning && ((HasSkinning)material).isMorphNormals() ) 
					 {
						 Vector3 n1, n2, n3, n4;
						 if ( needsSmoothNormals ) 
						 {

							 Geometry.VertextNormal faceVertexNormals = morphNormals.get( vk ).vertexNormals.get( chf );

							 n1 = faceVertexNormals.a;
							 n2 = faceVertexNormals.b;
							 n3 = faceVertexNormals.c;
							 n4 = faceVertexNormals.d;

						 } 
						 else 
						 {
							 n1 = morphNormals.get(vk).faceNormals.get(chf);
							 n2 = n1;
							 n3 = n1;
							 n4 = n1;

						 }

						 Float64Array nka = geometryGroup.__morphNormalsArrays.get( vk );

						 nka.set(offset_morphTarget, n1.getX());
						 nka.set(offset_morphTarget + 1, n1.getY());
						 nka.set(offset_morphTarget + 2, n1.getZ());

						 nka.set(offset_morphTarget + 3, n2.getX());
						 nka.set(offset_morphTarget + 4, n2.getY());
						 nka.set(offset_morphTarget + 5, n2.getZ());

						 nka.set(offset_morphTarget + 6, n3.getX());
						 nka.set(offset_morphTarget + 7, n3.getY());
						 nka.set(offset_morphTarget + 8, n3.getZ());

						 nka.set(offset_morphTarget + 9, n4.getX());
						 nka.set(offset_morphTarget + 10, n4.getY());
						 nka.set(offset_morphTarget + 11, n4.getZ());

					 }

					 //

					 offset_morphTarget += 12;

				 }

				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglMorphTargetsBuffers.get( vk ) );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, geometryGroup.__morphTargetsArrays.get( vk ), hint );

				 if ( material instanceof HasSkinning && ((HasSkinning)material).isMorphNormals() ) 
				 {
					 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglMorphNormalsBuffers.get( vk ) );
					 gl.bufferData( BufferTarget.ARRAY_BUFFER, geometryGroup.__morphNormalsArrays.get( vk ), hint );
				 }
			 }
		 }

		 if ( getGeometry().getSkinWeights().size() > 0 ) 
		 {
			 int offset_skin = 0;

			 Float64Array skinIndexArray = geometryGroup.getWebGlSkinIndexArray();
			 Float64Array skinWeightArray = geometryGroup.getWebGlSkinWeightArray();
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {
				 Face3 face = obj_faces.get( chunk_faces3.get( f ) );

				 // weights

				 Vector4 sw1 = getGeometry().getSkinWeights().get( face.getA() );
				 Vector4 sw2 = getGeometry().getSkinWeights().get( face.getB() );
				 Vector4 sw3 = getGeometry().getSkinWeights().get( face.getC() );

				 skinWeightArray.set(offset_skin, sw1.getX());
				 skinWeightArray.set(offset_skin + 1, sw1.getY());
				 skinWeightArray.set(offset_skin + 2, sw1.getZ());
				 skinWeightArray.set(offset_skin + 3, sw1.getW());

				 skinWeightArray.set(offset_skin + 4, sw2.getX());
				 skinWeightArray.set(offset_skin + 5, sw2.getY());
				 skinWeightArray.set(offset_skin + 6, sw2.getZ());
				 skinWeightArray.set(offset_skin + 7, sw2.getW());

				 skinWeightArray.set(offset_skin + 8, sw3.getX());
				 skinWeightArray.set(offset_skin + 9, sw3.getY());
				 skinWeightArray.set(offset_skin + 10, sw3.getZ());
				 skinWeightArray.set(offset_skin + 11, sw3.getW());

				 // indices

				 Vector4 si1 = (Vector4) getGeometry().getSkinIndices().get(face.getA());
				 Vector4 si2 = (Vector4) getGeometry().getSkinIndices().get(face.getB());
				 Vector4 si3 = (Vector4) getGeometry().getSkinIndices().get(face.getC());

				 skinIndexArray.set(offset_skin, si1.getX());
				 skinIndexArray.set(offset_skin + 1, si1.getY());
				 skinIndexArray.set(offset_skin + 2, si1.getZ());
				 skinIndexArray.set(offset_skin + 3, si1.getW());

				 skinIndexArray.set(offset_skin + 4, si2.getX());
				 skinIndexArray.set(offset_skin + 5, si2.getY());
				 skinIndexArray.set(offset_skin + 6, si2.getZ());
				 skinIndexArray.set(offset_skin + 7, si2.getW());

				 skinIndexArray.set(offset_skin + 8, si3.getX());
				 skinIndexArray.set(offset_skin + 9, si3.getY());
				 skinIndexArray.set(offset_skin + 10, si3.getZ());
				 skinIndexArray.set(offset_skin + 11, si3.getW());

				 offset_skin += 12;

			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {

				 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

				 // weights

				 Vector4 sw1 = getGeometry().getSkinWeights().get(face.getA());
				 Vector4 sw2 = getGeometry().getSkinWeights().get(face.getB());
				 Vector4 sw3 = getGeometry().getSkinWeights().get(face.getC());
				 Vector4 sw4 = getGeometry().getSkinWeights().get(face.getD());

				 skinWeightArray.set(offset_skin, sw1.getX());
				 skinWeightArray.set(offset_skin + 1, sw1.getY());
				 skinWeightArray.set(offset_skin + 2, sw1.getZ());
				 skinWeightArray.set(offset_skin + 3, sw1.getW());

				 skinWeightArray.set(offset_skin + 4, sw2.getX());
				 skinWeightArray.set(offset_skin + 5, sw2.getY());
				 skinWeightArray.set(offset_skin + 6, sw2.getZ());
				 skinWeightArray.set(offset_skin + 7, sw2.getW());

				 skinWeightArray.set(offset_skin + 8, sw3.getX());
				 skinWeightArray.set(offset_skin + 9, sw3.getY());
				 skinWeightArray.set(offset_skin + 10, sw3.getZ());
				 skinWeightArray.set(offset_skin + 11, sw3.getW());

				 skinWeightArray.set(offset_skin + 12, sw4.getX());
				 skinWeightArray.set(offset_skin + 13, sw4.getY());
				 skinWeightArray.set(offset_skin + 14, sw4.getZ());
				 skinWeightArray.set(offset_skin + 15, sw4.getW());

				 // indices

				 Vector4 si1 = getGeometry().getSkinIndices().get(face.getA());
				 Vector4 si2 = getGeometry().getSkinIndices().get(face.getB());
				 Vector4 si3 = getGeometry().getSkinIndices().get(face.getC());
				 Vector4 si4 = getGeometry().getSkinIndices().get(face.getD());

				 skinIndexArray.set(offset_skin, si1.getX());
				 skinIndexArray.set(offset_skin + 1, si1.getY());
				 skinIndexArray.set(offset_skin + 2, si1.getZ());
				 skinIndexArray.set(offset_skin + 3, si1.getW());

				 skinIndexArray.set(offset_skin + 4, si2.getX());
				 skinIndexArray.set(offset_skin + 5, si2.getY());
				 skinIndexArray.set(offset_skin + 6, si2.getZ());
				 skinIndexArray.set(offset_skin + 7, si2.getW());

				 skinIndexArray.set(offset_skin + 8, si3.getX());
				 skinIndexArray.set(offset_skin + 9, si3.getY());
				 skinIndexArray.set(offset_skin + 10, si3.getZ());
				 skinIndexArray.set(offset_skin + 11, si3.getW());

				 skinIndexArray.set(offset_skin + 12, si4.getX());
				 skinIndexArray.set(offset_skin + 13, si4.getY());
				 skinIndexArray.set(offset_skin + 14, si4.getZ());
				 skinIndexArray.set(offset_skin + 15, si4.getW());

				 offset_skin += 16;

			 }

			 if ( offset_skin > 0 ) 
			 {
				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglSkinIndicesBuffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, skinIndexArray, hint );

				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglSkinWeightsBuffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, skinWeightArray, hint );
			 }
		 }

		 if ( getGeometry().isColorsNeedUpdate() && (vertexColorType != null )) 
		 {
			 Float64Array colorArray = geometryGroup.getWebGlColorArray();
			 int offset_color = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {

				 Face3 face = obj_faces.get(chunk_faces3.get(f));

				 List<Color> vertexColors = face.getVertexColors();
				 Color faceColor = face.getColor();
				 Color c1, c2, c3;

				 if ( vertexColors.size() == 3 && vertexColorType == Material.COLORS.VERTEX) 
				 {
					 c1 = vertexColors.get(0);
					 c2 = vertexColors.get(1);
					 c3 = vertexColors.get(2);
				 }
				 else 
				 {
					 c1 = faceColor;
					 c2 = faceColor;
					 c3 = faceColor;
				 }

				 colorArray.set(offset_color, c1.getR());
				 colorArray.set(offset_color + 1, c1.getG());
				 colorArray.set(offset_color + 2, c1.getB());

				 colorArray.set(offset_color + 3, c2.getR());
				 colorArray.set(offset_color + 4, c2.getG());
				 colorArray.set(offset_color + 5, c2.getB());

				 colorArray.set(offset_color + 6, c3.getR());
				 colorArray.set(offset_color + 7, c3.getG());
				 colorArray.set(offset_color + 8, c3.getB());

				 offset_color += 9;

			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) {

				 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

				 List<Color> vertexColors = face.getVertexColors();
				 Color faceColor = face.getColor();
				 Color c1, c2, c3, c4;

				 if ( vertexColors.size() == 4 && vertexColorType == Material.COLORS.VERTEX) 
				 {
					 c1 = vertexColors.get(0);
					 c2 = vertexColors.get(1);
					 c3 = vertexColors.get(2);
					 c4 = vertexColors.get(3);
				 } 
				 else 
				 {
					 c1 = faceColor;
					 c2 = faceColor;
					 c3 = faceColor;
					 c4 = faceColor;
				 }

				 colorArray.set(offset_color, c1.getR());
				 colorArray.set(offset_color + 1, c1.getG());
				 colorArray.set(offset_color + 2, c1.getB());

				 colorArray.set(offset_color + 3, c2.getR());
				 colorArray.set(offset_color + 4, c2.getG());
				 colorArray.set(offset_color + 5, c2.getB());

				 colorArray.set(offset_color + 6, c3.getR());
				 colorArray.set(offset_color + 7, c3.getG());
				 colorArray.set(offset_color + 8, c3.getB());

				 colorArray.set(offset_color + 9, c4.getR());
				 colorArray.set(offset_color + 10, c4.getG());
				 colorArray.set(offset_color + 11, c4.getB());

				 offset_color += 12;

			 }

			 if ( offset_color > 0 ) 
			 {
				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglColorBuffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, colorArray, hint );
			 }
		 }

		 if ( getGeometry().isTangentsNeedUpdate() && geometry.hasTangents()) 
		 {
			 Float64Array tangentArray = geometryGroup.getWebGlTangentArray();
			 int offset_tangent = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {

				 Face3 face = obj_faces.get(chunk_faces3.get(f));

				 List<Vector4> vertexTangents = face.getVertexTangents();

				 Vector4 t1 = vertexTangents.get(0);
				 Vector4 t2 = vertexTangents.get(1);
				 Vector4 t3 = vertexTangents.get(2);

				 tangentArray.set(offset_tangent, t1.getX());
				 tangentArray.set(offset_tangent + 1, t1.getY());
				 tangentArray.set(offset_tangent + 2, t1.getZ());
				 tangentArray.set(offset_tangent + 3, t1.getW());

				 tangentArray.set(offset_tangent + 4, t2.getX());
				 tangentArray.set(offset_tangent + 5, t2.getY());
				 tangentArray.set(offset_tangent + 6, t2.getZ());
				 tangentArray.set(offset_tangent + 7, t2.getW());

				 tangentArray.set(offset_tangent + 8, t3.getX());
				 tangentArray.set(offset_tangent + 9, t3.getY());
				 tangentArray.set(offset_tangent + 10, t3.getZ());
				 tangentArray.set(offset_tangent + 11, t3.getW());

				 offset_tangent += 12;

			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) {

				 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

				 List<Vector4> vertexTangents = face.getVertexTangents();

				 Vector4 t1 = vertexTangents.get(0);
				 Vector4 t2 = vertexTangents.get(1);
				 Vector4 t3 = vertexTangents.get(2);
				 Vector4 t4 = vertexTangents.get(3);

				 tangentArray.set(offset_tangent, t1.getX());
				 tangentArray.set(offset_tangent + 1, t1.getY());
				 tangentArray.set(offset_tangent + 2, t1.getZ());
				 tangentArray.set(offset_tangent + 3, t1.getW());

				 tangentArray.set(offset_tangent + 4, t2.getX());
				 tangentArray.set(offset_tangent + 5, t2.getY());
				 tangentArray.set(offset_tangent + 6, t2.getZ());
				 tangentArray.set(offset_tangent + 7, t2.getW());

				 tangentArray.set(offset_tangent + 8, t3.getX());
				 tangentArray.set(offset_tangent + 9, t3.getY());
				 tangentArray.set(offset_tangent + 10, t3.getZ());
				 tangentArray.set(offset_tangent + 11, t3.getW());

				 tangentArray.set(offset_tangent + 12, t4.getX());
				 tangentArray.set(offset_tangent + 13, t4.getY());
				 tangentArray.set(offset_tangent + 14, t4.getZ());
				 tangentArray.set(offset_tangent + 15, t4.getW());

				 offset_tangent += 16;

			 }

			 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglTangentBuffer );
			 gl.bufferData( BufferTarget.ARRAY_BUFFER, tangentArray, hint );

		 }

		 if ( getGeometry().isNormalsNeedUpdate() && (normalType != null )) 
		 {
			 int offset_normal = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {

				 Face3 face = obj_faces.get(chunk_faces3.get(f));

				 List<Vector3> vertexNormals = face.getVertexNormals();
				 Vector3 faceNormal = face.getNormal();

				 if ( vertexNormals.size() == 3 && needsSmoothNormals ) 
				 {
					 for ( int i = 0; i < 3; i ++ ) 
					 {

						 Vector3 vn = vertexNormals.get(i);

						 geometryGroup.getWebGlNormalArray().set(offset_normal, vn.getX());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 1, vn.getY());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 2, vn.getZ());

						 offset_normal += 3;
					 }

				 } 
				 else 
				 {

					 for ( int i = 0; i < 3; i ++ ) 
					 {

						 geometryGroup.getWebGlNormalArray().set(offset_normal, faceNormal.getX());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 1, faceNormal.getY());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 2, faceNormal.getZ());

						 offset_normal += 3;
					 }
				 }
			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {

				 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

				 List<Vector3> vertexNormals = face.getVertexNormals();
				 Vector3 faceNormal = face.getNormal();

				 if ( vertexNormals.size() == 4 && needsSmoothNormals ) 
				 {
					 for ( int i = 0; i < 4; i ++ ) 
					 {

						 Vector3 vn = vertexNormals.get(i);

						 geometryGroup.getWebGlNormalArray().set(offset_normal, vn.getX());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 1, vn.getY());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 2, vn.getZ());

						 offset_normal += 3;
					 }

				 } 
				 else 
				 {

					 for ( int i = 0; i < 4; i ++ ) 
					 {

						 geometryGroup.getWebGlNormalArray().set(offset_normal, faceNormal.getX());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 1, faceNormal.getY());
						 geometryGroup.getWebGlNormalArray().set(offset_normal + 2, faceNormal.getZ());

						 offset_normal += 3;
					 }
				 }
			 }

			 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglNormalBuffer);
			 gl.bufferData( BufferTarget.ARRAY_BUFFER, geometryGroup.getWebGlNormalArray(), hint );

		 }

		 if ( getGeometry().isUvsNeedUpdate() && (obj_uvs != null) && uvType ) 
		 {
			 Float64Array uvArray = geometryGroup.getWebGlUvArray();
			 int offset_uv = 0;
			 
			 for (int  f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {

				 int fi = chunk_faces3.get(f);

				 List<UV> uv = obj_uvs.get(fi);

				 if ( uv == null ) continue;

				 for ( int i = 0; i < 3; i ++ ) {

					 UV uvi = uv.get(i);

					 uvArray.set(offset_uv, uvi.getU());
					 uvArray.set(offset_uv + 1, uvi.getV());

					 offset_uv += 2;
				 }
			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {
				 int fi = chunk_faces4.get(f);

				 List<UV>uv = obj_uvs.get(fi);

				 if ( uv == null ) continue;

				 for ( int i = 0; i < 4; i ++ ) 
				 {

					 UV uvi = uv.get(i);

					 uvArray.set(offset_uv, uvi.getU());
					 uvArray.set(offset_uv + 1, uvi.getV());

					 offset_uv += 2;
				 }
			 }

			 if ( offset_uv > 0 ) 
			 {
				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglUVBuffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, uvArray, hint );
			 }
		 }

		 if ( getGeometry().isUvsNeedUpdate() && (obj_uvs2 != null && obj_uvs2.size() > 0) && uvType ) 
		 {
			 Float64Array uv2Array = geometryGroup.getWebGlUv2Array();
			 int offset_uv2 = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {
				 int fi = chunk_faces3.get(f);

				 List<UV> uv2 = obj_uvs2.get(fi);

				 if ( uv2 == null ) continue;

				 for ( int i = 0; i < 3; i ++ ) 
				 {
					 UV uv2i = uv2.get(i);

					 uv2Array.set(offset_uv2, uv2i.getU());
					 uv2Array.set(offset_uv2 + 1, uv2i.getV());

					 offset_uv2 += 2;
				 }
			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {
				 int fi = chunk_faces4.get(f);

				 List<UV> uv2 = obj_uvs2.get(fi);

				 if ( uv2 == null ) continue;

				 for ( int i = 0; i < 4; i ++ ) 
				 {
					 UV uv2i = uv2.get(i);

					 uv2Array.set(offset_uv2, uv2i.getU());
					 uv2Array.set(offset_uv2 + 1, uv2i.getV());

					 offset_uv2 += 2;
				 }
			 }

			 if ( offset_uv2 > 0 ) 
			 {
				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, geometryGroup.__webglUV2Buffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, uv2Array, hint );
			 }
		 }

		 if (  getGeometry().isElementsNeedUpdate() ) 
		 {
			 int offset_line = 0;
			 int offset_face = 0;
			 int vertexIndex = 0;
			 
			 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
			 {
				 geometryGroup.getWebGlFaceArray().set(offset_face, vertexIndex);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 1, vertexIndex + 1);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 2, vertexIndex + 2);

				 offset_face += 3;

				 geometryGroup.getWebGlLineArray().set(offset_line, vertexIndex);
				 geometryGroup.getWebGlLineArray().set(offset_line + 1, vertexIndex + 1);

				 geometryGroup.getWebGlLineArray().set(offset_line + 2, vertexIndex);
				 geometryGroup.getWebGlLineArray().set(offset_line + 3, vertexIndex + 2);

				 geometryGroup.getWebGlLineArray().set(offset_line + 4, vertexIndex + 1);
				 geometryGroup.getWebGlLineArray().set(offset_line + 5, vertexIndex + 2);

				 offset_line += 6;

				 vertexIndex += 3;
			 }

			 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
			 {
				 geometryGroup.getWebGlFaceArray().set(offset_face, vertexIndex);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 1, vertexIndex + 1);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 2, vertexIndex + 3);

				 geometryGroup.getWebGlFaceArray().set(offset_face + 3, vertexIndex + 1);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 4, vertexIndex + 2);
				 geometryGroup.getWebGlFaceArray().set(offset_face + 5, vertexIndex + 3);

				 offset_face += 6;

				 geometryGroup.getWebGlLineArray().set(offset_line, vertexIndex);
				 geometryGroup.getWebGlLineArray().set(offset_line + 1, vertexIndex + 1);

				 geometryGroup.getWebGlLineArray().set(offset_line + 2, vertexIndex);
				 geometryGroup.getWebGlLineArray().set(offset_line + 3, vertexIndex + 3);

				 geometryGroup.getWebGlLineArray().set(offset_line + 4, vertexIndex + 1);
				 geometryGroup.getWebGlLineArray().set(offset_line + 5, vertexIndex + 2);

				 geometryGroup.getWebGlLineArray().set(offset_line + 6, vertexIndex + 2);
				 geometryGroup.getWebGlLineArray().set(offset_line + 7, vertexIndex + 3);

				 offset_line += 8;

				 vertexIndex += 4;

			 }
			 
			 gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryGroup.__webglFaceBuffer );
			 gl.bufferData( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryGroup.getWebGlFaceArray(), hint );

			 gl.bindBuffer( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryGroup.__webglLineBuffer );
			 gl.bufferData( BufferTarget.ELEMENT_ARRAY_BUFFER, geometryGroup.getWebGlLineArray(), hint );

		 }

		 if ( geometryGroup.__webglCustomAttributesList != null ) 
		 {
			 for ( int i = 0, il = geometryGroup.__webglCustomAttributesList.size(); i < il; i ++ ) 
			 {
				 Attribute customAttribute = geometryGroup.__webglCustomAttributesList.get(i);

				 if ( ! customAttribute.__original.needsUpdate ) continue;

				 int offset_custom = 0;
				 int offset_customSrc = 0;

				 if ( customAttribute.size == 1 ) 
				 {
					 if ( customAttribute.getBoundTo() == null 
							 || customAttribute.getBoundTo() == Attribute.BOUND_TO.VERTICES ) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 Face3 face = obj_faces.get(chunk_faces3.get(f));

							 customAttribute.array.set(offset_custom, (Double)customAttribute.getValue().get(face.getA()));
							 customAttribute.array.set(offset_custom + 1, (Double)customAttribute.getValue().get(face.getB()));
							 customAttribute.array.set(offset_custom + 2, (Double)customAttribute.getValue().get(face.getC()));

							 offset_custom += 3;
						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

							 customAttribute.array.set(offset_custom, (Double)customAttribute.getValue().get(face.getA()));
							 customAttribute.array.set(offset_custom + 1, (Double)customAttribute.getValue().get(face.getB()));
							 customAttribute.array.set(offset_custom + 2, (Double)customAttribute.getValue().get(face.getC()));
							 customAttribute.array.set(offset_custom + 3, (Double)customAttribute.getValue().get(face.getD()));

							 offset_custom += 4;
						 }
					 }
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACES ) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 double value = (Double) customAttribute.getValue().get(chunk_faces3.get(f));

							 customAttribute.array.set(offset_custom, value);
							 customAttribute.array.set(offset_custom + 1, value);
							 customAttribute.array.set(offset_custom + 2, value);

							 offset_custom += 3;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 double value = (Double) customAttribute.getValue().get(chunk_faces4.get(f));

							 customAttribute.array.set(offset_custom, value);
							 customAttribute.array.set(offset_custom + 1, value);
							 customAttribute.array.set(offset_custom + 2, value);
							 customAttribute.array.set(offset_custom + 3, value);

							 offset_custom += 4;
						 }
					 }
				 } 
				 else if ( customAttribute.size == 2 ) 
				 {
					 if ( customAttribute.getBoundTo() == null 
							 || customAttribute.getBoundTo() == Attribute.BOUND_TO.VERTICES ) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 Face3 face = obj_faces.get(chunk_faces3.get(f));

							 Vector3 v1 = (Vector3) customAttribute.getValue().get(face.getA());
							 Vector3 v2 = (Vector3) customAttribute.getValue().get(face.getB());
							 Vector3 v3 = (Vector3) customAttribute.getValue().get(face.getC());

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());

							 customAttribute.array.set(offset_custom + 2, v2.getX());
							 customAttribute.array.set(offset_custom + 3, v2.getY());

							 customAttribute.array.set(offset_custom + 4, v3.getX());
							 customAttribute.array.set(offset_custom + 5, v3.getY());

							 offset_custom += 6;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

							 Vector3 v1 = (Vector3) customAttribute.getValue().get(face.getA());
							 Vector3 v2 = (Vector3) customAttribute.getValue().get(face.getB());
							 Vector3 v3 = (Vector3) customAttribute.getValue().get(face.getC());
							 Vector3 v4 = (Vector3) customAttribute.getValue().get(face.getD());

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());

							 customAttribute.array.set(offset_custom + 2, v2.getX());
							 customAttribute.array.set(offset_custom + 3, v2.getY());

							 customAttribute.array.set(offset_custom + 4, v3.getX());
							 customAttribute.array.set(offset_custom + 5, v3.getY());

							 customAttribute.array.set(offset_custom + 6, v4.getX());
							 customAttribute.array.set(offset_custom + 7, v4.getY());

							 offset_custom += 8;
						 }
					 } 
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACES ) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 Vector3 value = (Vector3) customAttribute.getValue().get(chunk_faces3.get(f));

							 Vector3 v1 = value;
							 Vector3 v2 = value;
							 Vector3 v3 = value;

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());

							 customAttribute.array.set(offset_custom + 2, v2.getX());
							 customAttribute.array.set(offset_custom + 3, v2.getY());

							 customAttribute.array.set(offset_custom + 4, v3.getX());
							 customAttribute.array.set(offset_custom + 5, v3.getY());

							 offset_custom += 6;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 Vector3 value = (Vector3) customAttribute.getValue().get(chunk_faces4.get(f));

							 Vector3 v1 = value;
							 Vector3 v2 = value;
							 Vector3 v3 = value;
							 Vector3 v4 = value;

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());

							 customAttribute.array.set(offset_custom + 2, v2.getX());
							 customAttribute.array.set(offset_custom + 3, v2.getY());

							 customAttribute.array.set(offset_custom + 4, v3.getX());
							 customAttribute.array.set(offset_custom + 5, v3.getY());

							 customAttribute.array.set(offset_custom + 6, v4.getX());
							 customAttribute.array.set(offset_custom + 7, v4.getY());

							 offset_custom += 8;

						 }

					 }

				 } 
				 else if ( customAttribute.size == 3 ) 
				 {
					 if ( customAttribute.getBoundTo() == null 
							 || customAttribute.getBoundTo() == Attribute.BOUND_TO.VERTICES ) 
					 {

						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) {

							 Face3 face = obj_faces.get(chunk_faces3.get(f));

							 if(customAttribute.type == Attribute.TYPE.C) {
								 Color v1 = (Color) customAttribute.getValue().get(face.getA());
								 Color v2 = (Color) customAttribute.getValue().get(face.getB());
								 Color v3 = (Color) customAttribute.getValue().get(face.getC());

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());
							 }
							 else
							 {
								 Vector3 v1 = (Vector3) customAttribute.getValue().get(face.getA());
								 Vector3 v2 = (Vector3) customAttribute.getValue().get(face.getB());
								 Vector3 v3 = (Vector3) customAttribute.getValue().get(face.getC());

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());
							 }

							 offset_custom += 9;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {

							 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

							 if(customAttribute.type == Attribute.TYPE.C) {
								 Color v1 = (Color) customAttribute.getValue().get(face.getA());
								 Color v2 = (Color) customAttribute.getValue().get(face.getB());
								 Color v3 = (Color) customAttribute.getValue().get(face.getC());
								 Color v4 = (Color) customAttribute.getValue().get(face.getD());

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());

								 customAttribute.array.set(offset_custom + 9, v4.getR());
								 customAttribute.array.set(offset_custom + 10, v4.getG());
								 customAttribute.array.set(offset_custom + 11, v4.getB());
							 }
							 else
							 {
								 Vector3 v1 = (Vector3) customAttribute.getValue().get(face.getA());
								 Vector3 v2 = (Vector3) customAttribute.getValue().get(face.getB());
								 Vector3 v3 = (Vector3) customAttribute.getValue().get(face.getC());
								 Vector3 v4 = (Vector3) customAttribute.getValue().get(face.getD());

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());

								 customAttribute.array.set(offset_custom + 9, v4.getX());
								 customAttribute.array.set(offset_custom + 10, v4.getY());
								 customAttribute.array.set(offset_custom + 11, v4.getZ());
							 }

							 offset_custom += 12;
						 }
					 } 
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACES ) 
					 {

						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 if(customAttribute.type == Attribute.TYPE.C) 
							 {
								 Color value = (Color) customAttribute.getValue().get(chunk_faces3.get(f));
								 Color v1 = value;
								 Color v2 = value;
								 Color v3 = value;

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());
							 }
							 else
							 {
								 Vector3 value = (Vector3) customAttribute.getValue().get(chunk_faces3.get(f));
								 Vector3 v1 = value;
								 Vector3 v2 = value;
								 Vector3 v3 = value;

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());
							 }

							 offset_custom += 9;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {

							 if(customAttribute.type == Attribute.TYPE.C) 
							 {
								 Color value = (Color) customAttribute.getValue().get(chunk_faces4.get(f));
								 Color v1 = value;
								 Color v2 = value;
								 Color v3 = value;
								 Color v4 = value;

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());

								 customAttribute.array.set(offset_custom + 9, v4.getR());
								 customAttribute.array.set(offset_custom + 10, v4.getG());
								 customAttribute.array.set(offset_custom + 11, v4.getB());
							 }
							 else
							 {
								 Vector3 value = (Vector3) customAttribute.getValue().get(chunk_faces4.get(f));
								 Vector3 v1 = value;
								 Vector3 v2 = value;
								 Vector3 v3 = value;
								 Vector3 v4 = value;

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());

								 customAttribute.array.set(offset_custom + 9, v4.getX());
								 customAttribute.array.set(offset_custom + 10, v4.getY());
								 customAttribute.array.set(offset_custom + 11, v4.getZ());
							 }

							 offset_custom += 12;
						 }
					 }
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACE_VERTICES) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 if(customAttribute.type == Attribute.TYPE.C) 
							 {
								 List<Color> value = (List<Color>) customAttribute.getValue().get(chunk_faces3.get(f));
								 Color v1 = value.get(0);
								 Color v2 = value.get(1);
								 Color v3 = value.get(2);

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());
							 }
							 else
							 {
								 List<Vector3> value = (List<Vector3>) customAttribute.getValue().get(chunk_faces3.get(f));
								 Vector3 v1 = value.get(0);
								 Vector3 v2 = value.get(1);
								 Vector3 v3 = value.get(2);

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());
							 }

							 offset_custom += 9;
						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 if(customAttribute.type == Attribute.TYPE.C) 
							 {
								 List<Color> value = (List<Color>) customAttribute.getValue().get(chunk_faces4.get(f));
								 Color v1 = value.get(0);
								 Color v2 = value.get(1);
								 Color v3 = value.get(2);
								 Color v4 = value.get(3);

								 customAttribute.array.set(offset_custom, v1.getR());
								 customAttribute.array.set(offset_custom + 1, v1.getG());
								 customAttribute.array.set(offset_custom + 2, v1.getB());

								 customAttribute.array.set(offset_custom + 3, v2.getR());
								 customAttribute.array.set(offset_custom + 4, v2.getG());
								 customAttribute.array.set(offset_custom + 5, v2.getB());

								 customAttribute.array.set(offset_custom + 6, v3.getR());
								 customAttribute.array.set(offset_custom + 7, v3.getG());
								 customAttribute.array.set(offset_custom + 8, v3.getB());

								 customAttribute.array.set(offset_custom + 9, v4.getR());
								 customAttribute.array.set(offset_custom + 10, v4.getG());
								 customAttribute.array.set(offset_custom + 11, v4.getB());
							 }
							 else
							 {
								 List<Vector3> value = (List<Vector3>) customAttribute.getValue().get(chunk_faces4.get(f));
								 Vector3 v1 = value.get(0);
								 Vector3 v2 = value.get(1);
								 Vector3 v3 = value.get(2);
								 Vector3 v4 = value.get(3);

								 customAttribute.array.set(offset_custom, v1.getX());
								 customAttribute.array.set(offset_custom + 1, v1.getY());
								 customAttribute.array.set(offset_custom + 2, v1.getZ());

								 customAttribute.array.set(offset_custom + 3, v2.getX());
								 customAttribute.array.set(offset_custom + 4, v2.getY());
								 customAttribute.array.set(offset_custom + 5, v2.getZ());

								 customAttribute.array.set(offset_custom + 6, v3.getX());
								 customAttribute.array.set(offset_custom + 7, v3.getY());
								 customAttribute.array.set(offset_custom + 8, v3.getZ());

								 customAttribute.array.set(offset_custom + 9, v4.getX());
								 customAttribute.array.set(offset_custom + 10, v4.getY());
								 customAttribute.array.set(offset_custom + 11, v4.getZ());
							 }

							 offset_custom += 12;
						 }
					 }
				 }
				 else if ( customAttribute.size == 4 ) 
				 {
					 if ( customAttribute.getBoundTo() == null 
							 || customAttribute.getBoundTo() == Attribute.BOUND_TO.VERTICES ) 
					 {

						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 Face3 face = obj_faces.get(chunk_faces3.get(f));

							 Vector4 v1 = (Vector4) customAttribute.getValue().get(face.getA());
							 Vector4 v2 = (Vector4) customAttribute.getValue().get(face.getB());
							 Vector4 v3 = (Vector4) customAttribute.getValue().get(face.getC());

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 offset_custom += 12;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 Face4 face = (Face4) obj_faces.get(chunk_faces4.get(f));

							 Vector4 v1 = (Vector4) customAttribute.getValue().get(face.getA());
							 Vector4 v2 = (Vector4) customAttribute.getValue().get(face.getB());
							 Vector4 v3 = (Vector4) customAttribute.getValue().get(face.getC());
							 Vector4 v4 = (Vector4) customAttribute.getValue().get(face.getD());

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 customAttribute.array.set(offset_custom + 12, v4.getX());
							 customAttribute.array.set(offset_custom + 13, v4.getY());
							 customAttribute.array.set(offset_custom + 14, v4.getZ());
							 customAttribute.array.set(offset_custom + 15, v4.getW());

							 offset_custom += 16;
						 }
					 } 
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACES) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 Vector4 value = (Vector4) customAttribute.getValue().get(chunk_faces3.get(f));

							 Vector4 v1 = value;
							 Vector4 v2 = value;
							 Vector4 v3 = value;

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 offset_custom += 12;

						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ ) 
						 {
							 Vector4 value = (Vector4) customAttribute.getValue().get(chunk_faces4.get(f));

							 Vector4 v1 = value;
							 Vector4 v2 = value;
							 Vector4 v3 = value;
							 Vector4 v4 = value;

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 customAttribute.array.set(offset_custom + 12, v4.getX());
							 customAttribute.array.set(offset_custom + 13, v4.getY());
							 customAttribute.array.set(offset_custom + 14, v4.getZ());
							 customAttribute.array.set(offset_custom + 15, v4.getW());

							 offset_custom += 16;

						 }
					 }
					 else if ( customAttribute.getBoundTo() == Attribute.BOUND_TO.FACE_VERTICES ) 
					 {
						 for ( int f = 0, fl = chunk_faces3.size(); f < fl; f ++ ) 
						 {
							 List<Vector4> value = (List<Vector4>) customAttribute.getValue().get(chunk_faces3.get(f));

							 Vector4 v1 = value.get(0);
							 Vector4 v2 = value.get(1);
							 Vector4 v3 = value.get(2);

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 offset_custom += 12;
						 }

						 for ( int f = 0, fl = chunk_faces4.size(); f < fl; f ++ )
						 {
							 List<Vector4> value = (List<Vector4>) customAttribute.getValue().get(chunk_faces4.get(f));

							 Vector4 v1 = value.get(0);
							 Vector4 v2 = value.get(1);
							 Vector4 v3 = value.get(2);
							 Vector4 v4 = value.get(3);

							 customAttribute.array.set(offset_custom, v1.getX());
							 customAttribute.array.set(offset_custom + 1, v1.getY());
							 customAttribute.array.set(offset_custom + 2, v1.getZ());
							 customAttribute.array.set(offset_custom + 3, v1.getW());

							 customAttribute.array.set(offset_custom + 4, v2.getX());
							 customAttribute.array.set(offset_custom + 5, v2.getY());
							 customAttribute.array.set(offset_custom + 6, v2.getZ());
							 customAttribute.array.set(offset_custom + 7, v2.getW());

							 customAttribute.array.set(offset_custom + 8, v3.getX());
							 customAttribute.array.set(offset_custom + 9, v3.getY());
							 customAttribute.array.set(offset_custom + 10, v3.getZ());
							 customAttribute.array.set(offset_custom + 11, v3.getW());

							 customAttribute.array.set(offset_custom + 12, v4.getX());
							 customAttribute.array.set(offset_custom + 13, v4.getY());
							 customAttribute.array.set(offset_custom + 14, v4.getZ());
							 customAttribute.array.set(offset_custom + 15, v4.getW());

							 offset_custom += 16;
						 }
					 }
				 }

				 gl.bindBuffer( BufferTarget.ARRAY_BUFFER, customAttribute.buffer );
				 gl.bufferData( BufferTarget.ARRAY_BUFFER, customAttribute.array, hint );
			 }
		 }

		 if ( !geometry.isDynamic() ) 
			 geometryGroup.dispose();
	}
	
	@Override
	public void deleteBuffers(WebGLRenderer renderer) 
	{
		for ( GeometryGroup geometryGroup : geometry.getGeometryGroupsCache().values() )
		{
			renderer.getGL().deleteBuffer( geometryGroup.__webglVertexBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglNormalBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglTangentBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglColorBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglUVBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglUV2Buffer );

			renderer.getGL().deleteBuffer( geometryGroup.__webglSkinIndicesBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglSkinWeightsBuffer );

			renderer.getGL().deleteBuffer( geometryGroup.__webglFaceBuffer );
			renderer.getGL().deleteBuffer( geometryGroup.__webglLineBuffer );

			if ( geometryGroup.numMorphTargets != 0) 
			{
				for ( int m = 0; m < geometryGroup.numMorphTargets; m ++ ) 
				{
					renderer.getGL().deleteBuffer( geometryGroup.__webglMorphTargetsBuffers.get( m ) );
				}
			}

			if ( geometryGroup.numMorphNormals != 0 ) 
			{
				for ( int m = 0; m <  geometryGroup.numMorphNormals; m ++ ) 
				{
					renderer.getGL().deleteBuffer( geometryGroup.__webglMorphNormalsBuffers.get( m ) );
				}
			}


			if ( geometryGroup.__webglCustomAttributesList != null) 
			{
				for ( Attribute att : geometryGroup.__webglCustomAttributesList ) 
				{
					renderer.getGL().deleteBuffer( att.buffer );
				}
			}

			renderer.getInfo().getMemory().geometries --;
		}
	}

	private void sortFacesByMaterial ( Geometry geometry ) 
	{
		Log.debug("Called sortFacesByMaterial() for geometry: " + geometry.getClass().getName());

		int numMorphTargets = geometry.getMorphTargets().size();
		int numMorphNormals = geometry.getMorphNormals().size();

		geometry.setGeometryGroupsCache( new HashMap<String, GeometryGroup>() );

		Map<String, Integer> hash_map = GWT.isScript() ? 
				new FastMap<Integer>() : new HashMap<String, Integer>();

		Log.debug("sortFacesByMaterial() geometry faces count: " + geometry.getFaces().size());

		for ( int f = 0, fl = geometry.getFaces().size(); f < fl; f ++ ) 
		{
			Face3 face = geometry.getFaces().get(f);

			int materialIndex = face.getMaterialIndex();

			String materialHash = ( materialIndex != -1 ) ? String.valueOf(materialIndex) : "-1";

			if(!hash_map.containsKey(materialHash))
				hash_map.put(materialHash, 0);

			String groupHash = materialHash + '_' + hash_map.get(materialHash);
			
			if( ! geometry.getGeometryGroupsCache().containsKey(groupHash))
				geometry.getGeometryGroupsCache().put(groupHash, new GeometryGroup(materialIndex, numMorphTargets, numMorphNormals));

			int vertices = face.getClass() == Face3.class ? 3 : 4;

			if ( geometry.getGeometryGroupsCache().get(groupHash).vertices + vertices > 65535 ) 
			{
				hash_map.put(materialHash, hash_map.get(materialHash) + 1);
				groupHash = materialHash + '_' + hash_map.get( materialHash );

				if ( ! geometry.getGeometryGroupsCache().containsKey(groupHash))
					geometry.getGeometryGroupsCache().put(groupHash, new GeometryGroup(materialIndex, numMorphTargets, numMorphNormals));
			}

			if ( face.getClass() == Face3.class )
				geometry.getGeometryGroupsCache().get(groupHash).faces3.add( f );

			else
				geometry.getGeometryGroupsCache().get(groupHash).faces4.add( f );

			geometry.getGeometryGroupsCache().get(groupHash).vertices += vertices;
		}

		geometry.setGeometryGroups( new ArrayList<GeometryGroup>() );

		for ( GeometryGroup g : geometry.getGeometryGroupsCache().values() ) 
		{
			geometry.getGeometryGroups().add( g );
		}
	}
}
