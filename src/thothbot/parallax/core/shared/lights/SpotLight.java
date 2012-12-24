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

package thothbot.parallax.core.shared.lights;

import java.util.Map;

import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

import thothbot.parallax.core.client.shaders.Uniform;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;

/**
 * A point light that can cast shadow in one direction.
 * <p>
 * Affects objects using {@link MeshLambertMaterial} or {@link MeshPhongMaterial}.
 * 
 * <pre>
 * {@code
 * // white spotlight shining from the side, casting shadow 
 * 
 * SpotLight spotLight = new SpotLight( 0xffffff ); 
 * spotLight.getPosition().set( 100, 1000, 100 );  
 * spotLight.setCastShadow( true );  
 * spotLight.setShadowMapWidth( 1024 ); 
 * spotLight.setShadowMapHeight( 1024 );  
 * spotLight.setShadowCameraNear( 500 ); 
 * spotLight.setShadowCameraFar( 4000 ); 
 * spotLight.setShadowCameraFov( 30 );  
 * 
 * getScene().add( spotLight );
 * }
 * </pre>
 * 
 * @author thothbot
 *
 */
public class SpotLight extends ShadowLight
{
	public static class UniformSport implements Light.UniformLight 
	{
		public Float64Array distances;
		public Float64Array colors;
		public Float64Array positions;
		
		public Float64Array directions;
		public Float64Array angles;
		public Float64Array exponents;
		
		@Override
		public void reset() 
		{
			this.colors    = (Float64Array) TypedArrays.createFloat64Array(0);
			this.distances = (Float64Array) TypedArrays.createFloat64Array(0);
			this.positions = (Float64Array) TypedArrays.createFloat64Array(0);
			
			this.directions = (Float64Array) TypedArrays.createFloat64Array(0);
			this.angles     = (Float64Array) TypedArrays.createFloat64Array(0);
			this.exponents  = (Float64Array) TypedArrays.createFloat64Array(0);
		}

		@Override
		public void refreshUniform(Map<String, Uniform> uniforms) 
		{
			uniforms.get("spotLightColor").setValue( colors );
			uniforms.get("spotLightPosition").setValue( positions );
			uniforms.get("spotLightDistance").setValue( distances );

			uniforms.get("spotLightDirection").setValue( directions );
			uniforms.get("spotLightAngle").setValue( angles );
			uniforms.get("spotLightExponent").setValue( exponents );
		}
	}

	private double angle;
	private double exponent;

	private double shadowCameraFov = 50;

	public SpotLight(int hex) 
	{
		this(hex, 1.0);
	}

	public SpotLight(int hex, double intensity)
	{
		this(hex, intensity, 0, Math.PI / 2.0, 10);
	}

	public SpotLight(int hex, double intensity, double distance, double angle, double exponent) 
	{
		super(hex);
		this.exponent = exponent;
		this.angle = angle;
		
		setIntensity(intensity);
		setDistance(distance);
	}
	
	public double getExponent() {
		return exponent;
	}

	public void setExponent(double exponent) {
		this.exponent = exponent;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getShadowCameraFov() {
		return shadowCameraFov;
	}

	public void setShadowCameraFov(double shadowCameraFov) {
		this.shadowCameraFov = shadowCameraFov;
	}
	
	@Override
	public void setupRendererLights(RendererLights zlights, boolean isGammaInput) 
	{
		Float64Array spotColors     = zlights.spot.colors;
		Float64Array spotPositions  = zlights.spot.positions;
		Float64Array spotDistances  = zlights.spot.distances;
		Float64Array spotDirections = zlights.spot.directions;
		Float64Array spotAngles     = zlights.spot.angles;
		Float64Array spotExponents  = zlights.spot.exponents;
		
		double intensity = getIntensity();
		double distance =  getDistance();

		int spotOffset = spotColors.length();

		if ( isGammaInput ) 
			setColorGamma( spotColors, spotOffset, getColor(), intensity ); 
		else 
			setColorLinear( spotColors, spotOffset, getColor(), intensity );

		Vector3 position = getMatrixWorld().getPosition();

		spotPositions.set(spotOffset,     position.getX());
		spotPositions.set(spotOffset + 1, position.getY());
		spotPositions.set(spotOffset + 2, position.getZ());

		spotDistances.set(spotOffset / 3, distance);

		Vector3 direction = new Vector3();
		direction.copy( position );
		direction.sub( getTarget().getMatrixWorld().getPosition() );
		direction.normalize();

		spotDirections.set(spotOffset,    direction.getX());
		spotDirections.set(spotOffset + 1, direction.getY());
		spotDirections.set(spotOffset + 2, direction.getZ());

		spotAngles.set(spotOffset / 3, Math.cos( getAngle() ));
		spotExponents.set( spotOffset / 3, getExponent());
	}
}
