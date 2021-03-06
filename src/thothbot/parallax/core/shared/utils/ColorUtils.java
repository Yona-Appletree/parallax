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

package thothbot.parallax.core.shared.utils;

import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Mathematics;

/**
 * This class implements some color related helpers methods.
 * 
 * This code is based on js-code written by alteredq 
 * http://alteredqualia.com/
 * 
 * @author thothbot
 *
 */
public class ColorUtils
{
	/**
	 * HSV presentation of color
	 */
	public static class HSV 
	{
		public double hue;
		public double saturation;
		public double value;
		
		public HSV() 
		{
			this.hue = 0.0;
			this.saturation = 0.0;
			this.value = 0.0;
		}
	}
	
	/**
	 * This method adjusts color by defined values of H, S, V
	 * 
	 * @param color the Color instance
	 * @param h the hue
	 * @param s the saturation
	 * @param v the value
	 */
	public static void adjustHSV(Color color, double h, double s, double v ) 
	{
		ColorUtils.HSV hsv = ColorUtils.rgbToHsv(color);
		
		hsv.hue = Mathematics.clamp( hsv.hue + h, 0, 1 );
		hsv.saturation = Mathematics.clamp( hsv.saturation + s, 0, 1 );
		hsv.value = Mathematics.clamp( hsv.value + v, 0, 1 );

		color.setHSV( hsv.hue, hsv.saturation, hsv.value );
	}
	
	/**
	 * This method will make new HSV instance and sets color value from
	 * color instance
	 * 
	 * Based on: 
	 * MochiKit implementation by Bob Ippolito
	 * 
	 * @param color the color instance
	 * @return the new HSV instance
	 */
	public static ColorUtils.HSV rgbToHsv(Color color) 
	{
		double r = color.getR();
		double g = color.getG();
		double b = color.getB();

		double max = Math.max( Math.max( r, g ), b );
		double min = Math.min( Math.min( r, g ), b );

		double hue;
		double saturation;
		double value = max;

		if ( min == max )	
		{
			hue = 0;
			saturation = 0;

		} 
		else 
		{
			double delta = ( max - min );
			saturation = delta / max;

			if ( r == max )
				hue = ( g - b ) / delta;

			else if ( g == max )
				hue = 2 + ( ( b - r ) / delta );

			else
				hue = 4 + ( ( r - g ) / delta );

			hue /= 6;

			if ( hue < 0 ) 
				hue += 1;

			if ( hue > 1 )
				hue -= 1;
		}

		ColorUtils.HSV hsv = new ColorUtils.HSV();

		hsv.hue = hue;
		hsv.saturation = saturation;
		hsv.value = value;

		return hsv;
	}
}
