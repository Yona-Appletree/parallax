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
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Vector3;

public final class HemisphereLight extends Light 
{
	public static class UniformHemisphere implements Light.UniformLight 
	{
		public Float64Array skyColors;
		public Float64Array groundColors;
		public Float64Array positions;
		
		@Override
		public void reset() 
		{
			this.skyColors    = (Float64Array) TypedArrays.createFloat64Array(0);
			this.groundColors = (Float64Array) TypedArrays.createFloat64Array(0);
			this.positions = (Float64Array) TypedArrays.createFloat64Array(0);
			
		}

		@Override
		public void refreshUniform(Map<String, Uniform> uniforms) 
		{
			uniforms.get("hemisphereLightSkyColor").setValue( skyColors );
			uniforms.get("hemisphereLightGroundColor").setValue( groundColors );
			uniforms.get("hemisphereLightPosition").setValue( positions );
			
		}
	}

	private Color groundColor;
	private double intensity;
	
	public HemisphereLight(int skyColorHex, int groundColorHex)
	{
		this(skyColorHex, groundColorHex, 1);
	}
	
	public HemisphereLight(int skyColorHex, int groundColorHex, double intensity)
	{
		super(skyColorHex);
		
		this.groundColor = new Color( groundColorHex );

		this.position = new Vector3( 0, 100, 0 );

		this.intensity = intensity;
	}
	
	public Color getGroundColor() {
		return groundColor;
	}

	public void setGroundColor(Color groundColor) {
		this.groundColor = groundColor;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	@Override
	public void setupRendererLights(RendererLights zlights, boolean isGammaInput) 
	{
		Float64Array hemiSkyColors    = zlights.hemi.skyColors;
		Float64Array hemiGroundColors = zlights.hemi.groundColors;
		Float64Array hemiPositions    = zlights.hemi.positions;
		
		Color skyColor = getColor();
		Color groundColor = getGroundColor();
		double intensity = getIntensity();

		int hemiOffset = hemiSkyColors.length() * 3;

		if (  isGammaInput ) 
		{
			setColorGamma( hemiSkyColors, hemiOffset, skyColor, intensity );
			setColorGamma( hemiGroundColors, hemiOffset, groundColor, intensity );
		} 
		else 
		{
			setColorLinear( hemiSkyColors, hemiOffset, skyColor, intensity );
			setColorLinear( hemiGroundColors, hemiOffset, groundColor, intensity );
		}

		Vector3 position = getMatrixWorld().getPosition();

		hemiPositions.set( hemiOffset,     position.getX() );
		hemiPositions.set( hemiOffset + 1, position.getY() );
		hemiPositions.set( hemiOffset + 2, position.getZ() );
	}
}
