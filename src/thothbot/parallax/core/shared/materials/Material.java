/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file based on the JavaScript source file of the THREE.JS project, 
 * licensed under MIT License.
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Parallax. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.core.shared.materials;

import java.util.HashMap;
import java.util.Map;

import thothbot.parallax.core.client.gl2.enums.BlendEquationMode;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorDest;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorSrc;
import thothbot.parallax.core.client.shader.Program;
import thothbot.parallax.core.client.shader.Shader;
import thothbot.parallax.core.client.shader.Uniform;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.core.GeometryGroup;
import thothbot.parallax.core.shared.core.WebGLCustomAttribute;
import thothbot.parallax.core.shared.objects.GeometryObject;

public abstract class Material
{
	private static int MaterialCount;

	/**
	 * Shading
	 */
	public static enum SHADING 
	{
		NO, // NoShading = 0;
		FLAT, // FlatShading = 1;
		SMOOTH // SmoothShading = 2;
	};

	/**
	 * Colors
	 */
	public static enum COLORS 
	{
		NO, // NoColors = 0;
		FACE, // FaceColors = 1;
		VERTEX // VertexColors = 2;
	};

	/**
	 * Blending modes
	 */
	public static enum BLENDING 
	{
		NO, // NoBlending = 0;
		NORMAL, // NormalBlending = 1;
		ADDITIVE, // AdditiveBlending = 2;
		SUBTRACTIVE, // SubtractiveBlending = 3;
		MULTIPLY, // MultiplyBlending = 4;
		ADDITIVE_ALPHA, // AdditiveAlphaBlending = 5;
		CUSTOM // CustomBlending = 6;
	};

	private int id;
	
	private String name;
	
	private float opacity;
	private boolean transparent;
	
	private Material.SHADING shading;
	
	private Material.BLENDING blending;
	private BlendingFactorSrc blendSrc;
	private BlendingFactorDest blendDst;
	private BlendEquationMode blendEquation;

	private boolean depthTest;
	private boolean depthWrite;
	
	private boolean polygonOffset;
	private float polygonOffsetFactor;
	private float polygonOffsetUnits;
	
	private int alphaTest;
	
	// Boolean for fixing antialiasing gaps in CanvasRenderer
	private boolean overdraw;

	private boolean visible = true;
	private boolean needsUpdate = true;
	
	// 
	
	private Map<String, Uniform> uniforms;
	private Program program;
	private Map<String, WebGLCustomAttribute> attributes;
	private String vertexShader;
	private String fragmentShader;
	
	public Material()
	{
		this.id = Material.MaterialCount++;
		
		setOpacity(1.0f);
		setTransparent(false);
		
		setShading(Material.SHADING.SMOOTH);
		
		setBlending( Material.BLENDING.NORMAL );
		setBlendSrc(BlendingFactorSrc.SRC_ALPHA);
		setBlendDst(BlendingFactorDest.ONE_MINUS_SRC_ALPHA);
		setBlendEquation(BlendEquationMode.FUNC_ADD);
		
		setDepthTest(true);
		setDepthWrite(true);
		
		setPolygonOffset(false);
		setPolygonOffsetFactor(0.0f);
		setPolygonOffsetUnits(0.0f);
		
		setAlphaTest(0);
		setOverdraw(false);
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isNeedsUpdate() {
		return this.needsUpdate;
	}
	
	public void setNeedsUpdate(boolean visible) {
		this.needsUpdate = visible;
	}
	
	public Program getProgram() {
		return this.program;
	}
	
	public void setProgram(Program program) {
		this.program = program;
	}
	
	public Map<String, Uniform> getUniforms() {
		return this.uniforms;
	}
	
	public void setUniforms(Map<String, Uniform> uniforms) {
		this.uniforms = uniforms;
	}
	
	public Map<String, WebGLCustomAttribute> getAttributes() {
		return this.attributes;
	}
	
	public void setAttributes(Map<String, WebGLCustomAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public String getVertexShaderSource() {
		return this.vertexShader;
	}
	
	public void setVertexShaderSource(String str) {
		this.vertexShader = str;
	}
	
	public String getFragmentShaderSource() {
		return this.fragmentShader;
	}
	
	public void setFragmentShaderSource(String str) {
		this.fragmentShader = str;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public Material.BLENDING getBlending() {
		return blending;
	}

	public void setBlending(Material.BLENDING blending) {
		this.blending = blending;
	}
	
	public BlendingFactorSrc getBlendSrc() {
		return blendSrc;
	}

	public void setBlendSrc(BlendingFactorSrc blendSrc) {
		this.blendSrc = blendSrc;
	}

	public BlendingFactorDest getBlendDst() {
		return blendDst;
	}

	public void setBlendDst(BlendingFactorDest blendDst) {
		this.blendDst = blendDst;
	}

	public BlendEquationMode getBlendEquation() {
		return blendEquation;
	}

	public void setBlendEquation(BlendEquationMode blendEquation) {
		this.blendEquation = blendEquation;
	}
	
	public boolean isDepthTest() {
		return depthTest;
	}

	public void setDepthTest(boolean depthTest) {
		this.depthTest = depthTest;
	}

	public boolean isDepthWrite() {
		return depthWrite;
	}

	public void setDepthWrite(boolean depthWrite) {
		this.depthWrite = depthWrite;
	}

	public boolean isPolygonOffset() {
		return polygonOffset;
	}

	public void setPolygonOffset(boolean polygonOffset) {
		this.polygonOffset = polygonOffset;
	}

	public float getPolygonOffsetFactor() {
		return polygonOffsetFactor;
	}

	public void setPolygonOffsetFactor(float polygonOffsetFactor) {
		this.polygonOffsetFactor = polygonOffsetFactor;
	}

	public float getPolygonOffsetUnits() {
		return polygonOffsetUnits;
	}

	public void setPolygonOffsetUnits(float polygonOffsetUnits) {
		this.polygonOffsetUnits = polygonOffsetUnits;
	}

	public int getAlphaTest() {
		return alphaTest;
	}

	public void setAlphaTest(int alphaTest) {
		this.alphaTest = alphaTest;
	}
	
	public boolean isOverdraw() {
		return overdraw;
	}
	
	public void setOverdraw(boolean overdraw) {
		this.overdraw = overdraw;
	}

	public Material.SHADING getShading() {
		return this.shading;
	}
	
	public void setShading(Material.SHADING shading) {
		this.shading = shading;
	}

	// Must be overwriten
	public abstract Shader getShaderId();

	public void setMaterialShaders( Shader shader) 
	{
		Log.debug("Called Material.setMaterialShaders()");

		this.uniforms = new HashMap<String, Uniform>();
		this.uniforms.putAll(shader.getUniforms());

		this.vertexShader = shader.getVertexSource();
		this.fragmentShader = shader.getFragmentSource();
	}
	

	private boolean materialNeedsSmoothNormals() 
	{
		return this.shading != null && this.shading == Material.SHADING.SMOOTH;
	}
	
	public Material.SHADING bufferGuessNormalType () 
	{
		// only MeshBasicMaterial and MeshDepthMaterial don't need normals
		if (materialNeedsSmoothNormals())
			return Material.SHADING.SMOOTH;
		else
			return Material.SHADING.FLAT;
	}
	
	public Material.COLORS bufferGuessVertexColorType () 
	{
		if(this instanceof HasVertexColors && ((HasVertexColors)this).isVertexColors() != Material.COLORS.NO)
			return ((HasVertexColors)this).isVertexColors();

		return null;
	}
	
	public boolean bufferGuessUVType () 
	{
		if(this instanceof HasMap && ((HasMap)this).getMap() != null)
			return true;
		
		if(this instanceof HasLightMap && ((HasLightMap)this).getLightMap() != null)
			return true;
		
		return false;
	}

	public static Material getBufferMaterial( GeometryObject object, GeometryGroup geometryGroup ) 
	{
		if ( object.getMaterial() != null && !( object.getMaterial() instanceof MeshFaceMaterial ) )
			return object.getMaterial();

		else if ( geometryGroup.materialIndex >= 0 )
			return object.getGeometry().getMaterials().get( geometryGroup.materialIndex );
		
		return null;
	}
	
	public boolean areCustomAttributesDirty() 
	{
		for ( String a : this.attributes.keySet())
			if ( this.attributes.get( a ).needsUpdate ) return true;

		return false;
	}

	// TODO: Check
	public void clearCustomAttributes() 
	{
		if(this.attributes == null)
			return;

		for ( String a : this.attributes.keySet() )
			this.attributes.get( a ).needsUpdate = false;
	}
}