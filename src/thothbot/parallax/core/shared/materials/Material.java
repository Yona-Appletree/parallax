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

package thothbot.parallax.core.shared.materials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import thothbot.parallax.core.client.gl2.WebGLProgram;
import thothbot.parallax.core.client.gl2.WebGLRenderingContext;
import thothbot.parallax.core.client.gl2.enums.BlendEquationMode;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorDest;
import thothbot.parallax.core.client.gl2.enums.BlendingFactorSrc;
import thothbot.parallax.core.client.renderers.WebGLRenderer;
import thothbot.parallax.core.client.shaders.ChunksFragmentShader;
import thothbot.parallax.core.client.shaders.ChunksVertexShader;
import thothbot.parallax.core.client.shaders.ProgramParameters;
import thothbot.parallax.core.client.shaders.Shader;
import thothbot.parallax.core.client.shaders.Uniform;
import thothbot.parallax.core.client.textures.RenderTargetCubeTexture;
import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.cameras.Camera;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.GeometryGroup;
import thothbot.parallax.core.shared.core.Vector4;
import thothbot.parallax.core.shared.objects.GeometryObject;

/**
 * Materials describe the appearance of objects. 
 * 
 * @author thothbot
 *
 */
public abstract class Material
{
	private static int MaterialCount;

	/**
	 * Material sides
	 */
	public static enum SIDE
	{
		FRONT,
		BACK,
		DOUBLE
	}

	/**
	 * Shading
	 */
	public static enum SHADING 
	{
		NO, // NoShading = 0;
		FLAT, // FlatShading = 1;
		SMOOTH // SmoothShading = 2;
	}

	/**
	 * Colors
	 */
	public static enum COLORS 
	{
		NO, // NoColors = 0;
		FACE, // FaceColors = 1;
		VERTEX // VertexColors = 2;
	}

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
	}
	
	private static enum SHADER_DEFINE {
		VERTEX_TEXTURES, GAMMA_INPUT, GAMMA_OUTPUT, PHYSICALLY_BASED_SHADING,

		MAX_DIR_LIGHTS, // param
		MAX_POINT_LIGHTS, // param
		MAX_SPOT_LIGHTS, // param
		MAX_HEMI_LIGHTS, // param
		MAX_SHADOWS, // param
		MAX_BONES, // param

		USE_MAP, USE_ENVMAP, USE_LIGHTMAP, USE_BUMPMAP, USE_NORMALMAP, USE_SPECULARMAP, USE_COLOR, USE_SKINNING, USE_MORPHTARGETS, USE_MORPHNORMALS,

		BONE_TEXTURE, N_BONE_PIXEL_X, N_BONE_PIXEL_Y,
		PHONG_PER_PIXEL, WRAP_AROUND, DOUBLE_SIDED, FLIP_SIDED,

		USE_SHADOWMAP, SHADOWMAP_SOFT, SHADOWMAP_DEBUG, SHADOWMAP_CASCADE,

		USE_SIZEATTENUATION,

		ALPHATEST,

		USE_FOG, FOG_EXP2, METAL;

		public String getValue()
		{
			return "#define " + this.name();
		}

		public String getValue(int param)
		{
			return "#define " + this.name() + " " + param;
		}
		
		public String getValue(double param)
		{
			return "#define " + this.name() + " " + param;
		}
	}

	private int id;
	
	private String name;

	private double opacity;
	private boolean isTransparent;
	
	private Material.SHADING shading;
	
	private Material.BLENDING blending;
	private BlendingFactorSrc blendSrc;
	private BlendingFactorDest blendDst;
	private BlendEquationMode blendEquation;

	private boolean isDepthTest;
	private boolean isDepthWrite;
	
	private boolean isPolygonOffset;
	private double polygonOffsetFactor;
	private double polygonOffsetUnits;
	
	private double alphaTest;
	
	private boolean isVisible = true;
	private boolean isNeedsUpdate = true;
	
	private SIDE side = SIDE.FRONT;
	
	private boolean isShadowPass;
	
	// Store shader associated to the material
	private Shader shader;
	
	public Material()
	{
		this.id = Material.MaterialCount++;
		
		setOpacity(1.0);
		setTransparent(false);
		
		setShading(Material.SHADING.SMOOTH);
		
		setBlending( Material.BLENDING.NORMAL );
		setBlendSrc( BlendingFactorSrc.SRC_ALPHA );
		setBlendDst( BlendingFactorDest.ONE_MINUS_SRC_ALPHA );
		setBlendEquation( BlendEquationMode.FUNC_ADD );
		
		setDepthTest(true);
		setDepthWrite(true);
		
		setPolygonOffset(false);
		setPolygonOffsetFactor(0.0);
		setPolygonOffsetUnits(0.0);
		
		setAlphaTest(0);
	}
	
	// Must be overwriten
	protected abstract Shader getAssociatedShader();

	/**
	 * Gets unique number of this material instance.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets material name. Default is an empty string.
	 */
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isVisible() {
		return this.isVisible;
	}
	
	/**
	 * Defines whether this material is visible.
	 * <p> 
	 * Default is true.
	 */
	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}
	
	public SIDE getSides() {
		return this.side;
	}
	
	/**
	 * Defines which of the face sides will be rendered - front, back or both.
	 * <p>
	 * Default is {@link SIDE#FRONT}
	 * 
	 * @param side see options {@link Material.SIDE}.
	 */
	public void setSide(SIDE side) {
		this.side = side;
	}
	
	public boolean isNeedsUpdate() {
		return this.isNeedsUpdate;
	}
	
	/**
	 * Specifies that the material needs to be updated, WebGL wise. 
	 * Set it to true if you made changes that need to be reflected in WebGL.
	 * <p>
	 * This property is automatically set to true when instancing a new material.
	 */
	public void setNeedsUpdate(boolean visible) {
		this.isNeedsUpdate = visible;
	}

	/**
	 * Gets opacity. Default is 1.
	 */
	public double getOpacity() {
		return opacity;
	}

	/**
	 * Sets opacity. Default is 1.
	 */
	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	/**
	 * Defines whether this material is transparent. 
	 * <p>
	 * This has an effect on rendering, as transparent objects need an special treatment, 
	 * and are rendered after the opaque (i.e. non transparent) objects. 
	 */
	public void setTransparent(boolean transparent) {
		this.isTransparent = transparent;
	}

	public Material.BLENDING getBlending() {
		return blending;
	}

	/**
	 * Sets which blending to use when displaying objects with this material.
	 * <p>
	 * Default is {@link Material.BLENDING#NORMAL}.
	 */
	public void setBlending(Material.BLENDING blending) {
		this.blending = blending;
	}
	
	public BlendingFactorSrc getBlendSrc() {
		return blendSrc;
	}

	/**
	 * Sets blending source. It's one of the {@link BlendingFactorSrc} constants. 
	 * <p>
	 * Default is {@link BlendingFactorSrc#SRC_ALPHA}.
	 * 
	 * @param blendSrc
	 */
	public void setBlendSrc(BlendingFactorSrc blendSrc) {
		this.blendSrc = blendSrc;
	}

	public BlendingFactorDest getBlendDst() {
		return blendDst;
	}

	/**
	 * Sets blending destination. It's one of the {@link BlendingFactorDest} constants. 
	 * <p>
	 * Default is {@link BlendingFactorDest#ONE_MINUS_SRC_ALPHA}.
	 * 
	 * @param blendDst
	 */
	public void setBlendDst(BlendingFactorDest blendDst) {
		this.blendDst = blendDst;
	}

	public BlendEquationMode getBlendEquation() {
		return blendEquation;
	}

	/**
	 * Sets blending equation to use when applying blending. 
	 * It's one of the {@link BlendEquationMode} constants.
	 * <p>
	 * Default is {@link BlendEquationMode#FUNC_ADD}.
	 * 
	 * @param blendEquation
	 */
	public void setBlendEquation(BlendEquationMode blendEquation) {
		this.blendEquation = blendEquation;
	}
	
	public boolean isDepthTest() {
		return isDepthTest;
	}

	/**
	 * Whether to have depth test enabled when rendering this material. 
	 * <p>
	 * Default is true.
	 */
	public void setDepthTest(boolean depthTest) {
		this.isDepthTest = depthTest;
	}

	public boolean isDepthWrite() {
		return isDepthWrite;
	}

	/**
	 * Whether rendering this material has any effect on the depth buffer.
	 * <p> 
	 * Default is true.
	 * <p>
	 * When drawing 2D overlays it can be useful to disable the depth writing in order 
	 * to layer several things together without creating z-index artifacts.
	 */
	public void setDepthWrite(boolean depthWrite) {
		this.isDepthWrite = depthWrite;
	}

	public boolean isPolygonOffset() {
		return isPolygonOffset;
	}

	/**
	 * Whether to use polygon offset. 
	 * <p>
	 * Default is false.
	 * <p> 
	 * This corresponds to the POLYGON_OFFSET_FILL WebGL feature.
	 */
	public void setPolygonOffset(boolean polygonOffset) {
		this.isPolygonOffset = polygonOffset;
	}

	public double getPolygonOffsetFactor() {
		return polygonOffsetFactor;
	}

	/**
	 * Sets the polygon offset factor.
	 * <p> 
	 * Default is 0.
	 */
	public void setPolygonOffsetFactor(double polygonOffsetFactor) {
		this.polygonOffsetFactor = polygonOffsetFactor;
	}

	public double getPolygonOffsetUnits() {
		return polygonOffsetUnits;
	}

	/**
	 * Sets the polygon offset units.
	 * <p> 
	 * Default is 0.
	 */
	public void setPolygonOffsetUnits(double polygonOffsetUnits) {
		this.polygonOffsetUnits = polygonOffsetUnits;
	}

	public double getAlphaTest() {
		return alphaTest;
	}

	/**
	 * Sets the alpha value to be used when running an alpha test.
	 * <p> 
	 * Default is 0.
	 */
	public void setAlphaTest(double alphaTest) {
		this.alphaTest = alphaTest;
	}
	
	public Material.SHADING getShading() {
		return this.shading;
	}

	public void setShading(Material.SHADING shading) {
		this.shading = shading;
	}
	
	public boolean isShadowPass() {
		return isShadowPass;
	}

	public void setShadowPass(boolean isShadowPass) {
		this.isShadowPass = isShadowPass;
	}

	public Shader getShader() 
	{
		if(shader == null)
		{
			Log.debug("Called Material.setMaterialShaders()");

			this.shader = getAssociatedShader();
		}

		return this.shader;
	}

	public void updateProgramParameters(ProgramParameters parameters)
	{
		parameters.map       = (this instanceof HasMap && ((HasMap)this).getMap() != null);
		parameters.envMap    = (this instanceof HasEnvMap && ((HasEnvMap)this).getEnvMap() != null);
		parameters.lightMap  = (this instanceof HasLightMap &&  ((HasLightMap)this).getLightMap() != null);
		parameters.bumpMap   = (this instanceof HasBumpMap &&  ((HasBumpMap)this).getBumpMap() != null);
		parameters.normalMap = (this instanceof HasNormalMap &&  ((HasNormalMap)this).getNormalMap() != null);
		parameters.specularMap  = (this instanceof HasSpecularMap &&  ((HasSpecularMap)this).getSpecularMap() != null);

		parameters.vertexColors = (this instanceof HasVertexColors && ((HasVertexColors)this).isVertexColors() != Material.COLORS.NO);

		parameters.sizeAttenuation = this instanceof ParticleBasicMaterial && ((ParticleBasicMaterial)this).isSizeAttenuation();

		if(this instanceof HasSkinning)
		{
			parameters.skinning     = ((HasSkinning)this).isSkinning();
			parameters.morphTargets = ((HasSkinning)this).isMorphTargets();
			parameters.morphNormals = ((HasSkinning)this).isMorphNormals();
		}

		parameters.alphaTest = getAlphaTest();
		if(this instanceof MeshPhongMaterial)
		{
			parameters.metal = ((MeshPhongMaterial)this).isMetal();
			parameters.perPixel = ((MeshPhongMaterial)this).isPerPixel();
		}

		parameters.wrapAround = this instanceof HasWrap && ((HasWrap)this).isWrapAround();
		parameters.doubleSided = this.getSides() == Material.SIDE.DOUBLE;
		parameters.flipSided = this.getSides() == Material.SIDE.BACK;
	}

	public Shader buildShader(WebGLRenderingContext gl, ProgramParameters parameters)
	{
		Shader shader = getShader();

		shader.setVertexSource(getPrefixVertex(parameters) + "\n" + shader.getVertexSource());
		shader.setFragmentSource(getPrefixFragment(parameters) + "\n" + shader.getFragmentSource());

		this.shader = shader.buildProgram(gl, parameters.useVertexTexture, parameters.maxMorphTargets, parameters.maxMorphNormals);

		return this.shader;
	}

	private String getPrefixVertex(ProgramParameters parameters)
	{
		Log.debug("Called getPrefixVertex()");
		List<String> options = new ArrayList<String>();

		options.add("");
		
		if (parameters.isSupportsVertexTextures)
			options.add(SHADER_DEFINE.VERTEX_TEXTURES.getValue());

		if (parameters.gammaInput)
			options.add(SHADER_DEFINE.GAMMA_INPUT.getValue());

		if (parameters.gammaOutput)
			options.add(SHADER_DEFINE.GAMMA_OUTPUT.getValue());

		if (parameters.physicallyBasedShading)
			options.add(SHADER_DEFINE.PHYSICALLY_BASED_SHADING.getValue());

		options.add(SHADER_DEFINE.MAX_DIR_LIGHTS.getValue(parameters.maxDirLights));
		options.add(SHADER_DEFINE.MAX_POINT_LIGHTS.getValue(parameters.maxPointLights));
		options.add(SHADER_DEFINE.MAX_SPOT_LIGHTS.getValue(parameters.maxSpotLights));
		options.add(SHADER_DEFINE.MAX_HEMI_LIGHTS.getValue(parameters.maxHemiLights));

		options.add(SHADER_DEFINE.MAX_SHADOWS.getValue(parameters.maxShadows));

		options.add(SHADER_DEFINE.MAX_BONES.getValue(parameters.maxBones));

		if (parameters.map)
			options.add(SHADER_DEFINE.USE_MAP.getValue());

		if (parameters.envMap)
			options.add(SHADER_DEFINE.USE_ENVMAP.getValue());

		if (parameters.lightMap)
			options.add(SHADER_DEFINE.USE_LIGHTMAP.getValue());
		
		if (parameters.bumpMap)
			options.add(SHADER_DEFINE.USE_BUMPMAP.getValue());
		
		if (parameters.normalMap)
			options.add(SHADER_DEFINE.USE_NORMALMAP.getValue());
		
		if (parameters.specularMap)
			options.add(SHADER_DEFINE.USE_SPECULARMAP.getValue());

		if (parameters.vertexColors)
			options.add(SHADER_DEFINE.USE_COLOR.getValue());

		if (parameters.skinning)
			options.add(SHADER_DEFINE.USE_SKINNING.getValue());

		if (parameters.useVertexTexture)
			options.add(SHADER_DEFINE.BONE_TEXTURE.getValue());
		
		if (parameters.boneTextureWidth > 0)
			options.add(SHADER_DEFINE.N_BONE_PIXEL_X.getValue(parameters.boneTextureWidth));
		
		if (parameters.boneTextureHeight> 0)
			options.add(SHADER_DEFINE.N_BONE_PIXEL_Y.getValue(parameters.boneTextureHeight));
		
		if (parameters.morphTargets)
			options.add(SHADER_DEFINE.USE_MORPHTARGETS.getValue());
		if (parameters.morphNormals)
			options.add(SHADER_DEFINE.USE_MORPHNORMALS.getValue());
		
		if (parameters.perPixel)
			options.add(SHADER_DEFINE.PHONG_PER_PIXEL.getValue());
		if (parameters.wrapAround)
			options.add(SHADER_DEFINE.WRAP_AROUND.getValue());
		if (parameters.doubleSided)
			options.add(SHADER_DEFINE.DOUBLE_SIDED.getValue());
		if (parameters.flipSided)
			options.add(SHADER_DEFINE.FLIP_SIDED.getValue());

		if (parameters.shadowMapEnabled)
			options.add(SHADER_DEFINE.USE_SHADOWMAP.getValue());
		if (parameters.shadowMapSoft)
			options.add(SHADER_DEFINE.SHADOWMAP_SOFT.getValue());
		if (parameters.shadowMapDebug)
			options.add(SHADER_DEFINE.SHADOWMAP_DEBUG.getValue());
		if (parameters.shadowMapCascade)
			options.add(SHADER_DEFINE.SHADOWMAP_CASCADE.getValue());

		if (parameters.sizeAttenuation)
		{
			Log.error("Fix uniform in Particle material: size");
			options.add(SHADER_DEFINE.USE_SIZEATTENUATION.getValue());
		}

		options.add(ChunksVertexShader.DEFAULT_PARS);
		options.add("");

		String retval = "";
		for(String opt: options)
			retval += opt + "\n";
		return retval;
	}

	private String getPrefixFragment(ProgramParameters parameters)
	{
		Log.debug("Called getPrefixFragment()");
		List<String> options = new ArrayList<String>();

		options.add("");
		
		if (parameters.bumpMap || parameters.normalMap)
			options.add("#extension GL_OES_standard_derivatives : enable");
		
		options.add(SHADER_DEFINE.MAX_DIR_LIGHTS.getValue(parameters.maxDirLights));
		options.add(SHADER_DEFINE.MAX_POINT_LIGHTS.getValue(parameters.maxPointLights));
		options.add(SHADER_DEFINE.MAX_SPOT_LIGHTS.getValue(parameters.maxSpotLights));
		options.add(SHADER_DEFINE.MAX_HEMI_LIGHTS.getValue(parameters.maxHemiLights));

		options.add(SHADER_DEFINE.MAX_SHADOWS.getValue(parameters.maxShadows));

		if (parameters.alphaTest > 0)
			options.add(SHADER_DEFINE.ALPHATEST.getValue(parameters.alphaTest));

		if (parameters.gammaInput)
			options.add(SHADER_DEFINE.GAMMA_INPUT.getValue());

		if (parameters.gammaOutput)
			options.add(SHADER_DEFINE.GAMMA_OUTPUT.getValue());

		if (parameters.physicallyBasedShading)
			options.add(SHADER_DEFINE.PHYSICALLY_BASED_SHADING.getValue());

		if (parameters.useFog)
			options.add(SHADER_DEFINE.USE_FOG.getValue());

		if (parameters.useFog2)
			options.add(SHADER_DEFINE.FOG_EXP2.getValue());

		if (parameters.map)
			options.add(SHADER_DEFINE.USE_MAP.getValue());

		if (parameters.envMap)
			options.add(SHADER_DEFINE.USE_ENVMAP.getValue());

		if (parameters.lightMap)
			options.add(SHADER_DEFINE.USE_LIGHTMAP.getValue());
		
		if (parameters.bumpMap)
			options.add(SHADER_DEFINE.USE_BUMPMAP.getValue());
		
		if (parameters.normalMap)
			options.add(SHADER_DEFINE.USE_NORMALMAP.getValue());
		
		if (parameters.specularMap)
			options.add(SHADER_DEFINE.USE_SPECULARMAP.getValue());

		if (parameters.vertexColors)
			options.add(SHADER_DEFINE.USE_COLOR.getValue());

		if (parameters.metal)
			options.add(SHADER_DEFINE.METAL.getValue());

		if (parameters.perPixel)
			options.add(SHADER_DEFINE.PHONG_PER_PIXEL.getValue());
		if (parameters.wrapAround)
			options.add(SHADER_DEFINE.WRAP_AROUND.getValue());
		if (parameters.doubleSided)
			options.add(SHADER_DEFINE.DOUBLE_SIDED.getValue());
		if (parameters.flipSided)
			options.add(SHADER_DEFINE.FLIP_SIDED.getValue());

		if (parameters.shadowMapEnabled)
			options.add(SHADER_DEFINE.USE_SHADOWMAP.getValue());
		if (parameters.shadowMapSoft)
			options.add(SHADER_DEFINE.SHADOWMAP_SOFT.getValue());
		if (parameters.shadowMapDebug)
			options.add(SHADER_DEFINE.SHADOWMAP_DEBUG.getValue());
		if (parameters.shadowMapCascade)
			options.add(SHADER_DEFINE.SHADOWMAP_CASCADE.getValue());

		options.add(ChunksFragmentShader.DEFAULT_PARS);

		options.add("");
		String retval = "";
		for(String opt: options)
			retval += opt + "\n";

		return retval;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public void refreshUniforms(Camera camera, boolean isGammaInput) 
	{
		if ( ! (this instanceof HasMaterialMap) )
			return;

		Map<String, Uniform> uniforms = getShader().getUniforms();
		
		uniforms.get("opacity").setValue( getOpacity() );

		if(this instanceof HasColor)
		{
			if ( isGammaInput ) 
				((Color) uniforms.get("diffuse").getValue()).copyGammaToLinear( ((HasColor)this).getColor() );
 
			else
				uniforms.get("diffuse").setValue( ((HasColor)this).getColor() );
		}
		
		if(this instanceof HasMap)
		{
			uniforms.get("map").setValue( ((HasMap) this).getMap() );
		}

		if(this instanceof HasLightMap)
			uniforms.get("lightMap").setValue( ((HasLightMap)this).getLightMap() );	
		
		if(this instanceof HasSpecularMap)
		{
			uniforms.get("specularMap").setValue( ((HasSpecularMap)this).getSpecularMap() );
		}
		
		if(this instanceof HasBumpMap)
		{
			uniforms.get("bumpMap").setValue( ((HasBumpMap)this).getBumpMap() );
			uniforms.get("bumpScale").setValue( ((HasBumpMap)this).getBumpScale() );
		}	
		
		if(this instanceof HasNormalMap)
		{
			uniforms.get("normalMap").setValue( ((HasNormalMap)this).getNormalMap() );
			uniforms.get("normalScale").setValue( ((HasNormalMap)this).getNormalScale() );
		}	
		
		// uv repeat and offset setting priorities
		//	1. color map
		//	2. specular map
		//	3. normal map
		//  4. bump map
		Texture uvScaleMap = null;
		
		if(this instanceof HasMap)
			uvScaleMap = ((HasMap) this).getMap();
		else if(this instanceof HasSpecularMap)
			uvScaleMap = ((HasSpecularMap)this).getSpecularMap();
		else if(this instanceof HasNormalMap)
			uvScaleMap = ((HasNormalMap)this).getNormalMap();
		else if(this instanceof HasBumpMap)
			uvScaleMap = ((HasBumpMap)this).getBumpMap();
		
		if(uvScaleMap != null)
		{
			((Vector4)uniforms.get("offsetRepeat").getValue()).set( 
					uvScaleMap.getOffset().getX(), 
					uvScaleMap.getOffset().getY(), 
					uvScaleMap.getRepeat().getX(), 
					uvScaleMap.getRepeat().getY() );
		}
		
		if(this instanceof HasEnvMap)
		{
			HasEnvMap envMapMaterial = (HasEnvMap)this;

			uniforms.get("envMap").setValue( envMapMaterial.getEnvMap() );
			uniforms.get("flipEnvMap").setValue( ( envMapMaterial.getEnvMap() != null 
					&& envMapMaterial.getEnvMap().getClass() == RenderTargetCubeTexture.class ) ? 1.0 : -1.0 );

			if ( isGammaInput ) 
				uniforms.get("reflectivity").setValue( envMapMaterial.getReflectivity() );
 
			else
				uniforms.get("reflectivity").setValue( envMapMaterial.getReflectivity() );
			
			uniforms.get("refractionRatio").setValue( envMapMaterial.getRefractionRatio() );
			uniforms.get("combine").setValue( envMapMaterial.getCombine().getValue() );
			uniforms.get("useRefract").setValue( ( envMapMaterial.getEnvMap() != null 
					&& envMapMaterial.getEnvMap().getMapping() == Texture.MAPPING_MODE.CUBE_REFRACTION ) ? 1 : 0 );
		}
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
		
		if(this instanceof HasBumpMap && ((HasBumpMap)this).getBumpMap() != null)
			return true;
		
		if(this instanceof HasNormalMap && ((HasNormalMap)this).getNormalMap() != null)
			return true;
		
		if(this instanceof HasSpecularMap && ((HasSpecularMap)this).getSpecularMap() != null)
			return true;
		
		return false;
	}

	public static Material getBufferMaterial( GeometryObject object, GeometryGroup geometryGroup ) 
	{
		Material material = null;
		if ( object.getMaterial() != null && !( object.getMaterial() instanceof MeshFaceMaterial ) )
		{
			material = object.getMaterial(); 
		}
		else if ( geometryGroup.materialIndex >= 0 )
		{
			material = object.getGeometry().getMaterials().get( geometryGroup.materialIndex );	
		}
		
		return material;
	}
	
	
	public void deallocate( WebGLRenderer renderer ) 
	{
		WebGLProgram program = getShader().getProgram();
		if ( program == null ) return;

		for ( String key: renderer.getCache_programs().keySet()) 
		{
			Shader shader = renderer.getCache_programs().get(key);
			
			if ( shader == getShader() ) 
			{
				renderer.getInfo().getMemory().programs --;
				renderer.getCache_programs().remove(key);
				break;
			}
		}
	}
}
