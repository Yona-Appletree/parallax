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

/**
 * The Color class is used encapsulate colors in the default RGB color space.
 * This class doesn't work with alpha value. 
 * Each RGB value stored as values in range [0.0-1.0]. Where value of 1.0 is 255 means 
 * that the color is completely shown and an color value of 0 or 0.0 means that 
 * the color is completely hidden. 
 * 
 * @author thothbot
 */
public final class Color
{
	/**
	 * The Color in HEX.
	 */
	private int hex;

	/**
	 * The R-component of the color.
	 */
	private double r;

	/**
	 * The G-component of the color.
	 */
	private double g;

	/**
	 * The B-component of the color.
	 */
	private double b;

	/**
	 * This default constructor will create color where R=1.0, G=1.0, B=1.0
	 * ie (255, 255, 255) or 0xFFFFFF in HEX.
	 */
	public Color() 
	{		
		setHex(0xFFFFFF);
	}

	/**
	 * This constructor will create Color instance by defined HEX value.
	 * For example 0xFFFFFF will create a color in RGB(255, 255, 255), which
	 * means completely white. 
	 * 
	 * @param hex Color in HEX format
	 */
	public Color(int hex) 
	{
		setHex(hex);
	}

	/**
	 * get R-component of the color. 
	 * 
	 * @return a value in range <0.0, 1.0> 
	 */
	public double getR()
	{
		return r;
	}

	/**
	 * get G-component of the color. 
	 * 
	 * @return a value in range <0.0, 1.0> 
	 */
	public double getG()
	{
		return g;
	}
	
	/**
	 * get B-component of the color. 
	 * 
	 * @return a value in range <0.0, 1.0> 
	 */
	public double getB()
	{
		return b;
	}
	
	/**
	 * Setting R-component of the color.
	 * 
	 * @param r the value in range <0.0, 1.0> 
	 */
	public void setR(double r)
	{
		this.r = r;
	}

	/**
	 * Setting G-component of the color.
	 * 
	 * @param g the value in range <0.0, 1.0> 
	 */
	public void setG(double g)
	{
		this.g = g;
	}

	/**
	 * Setting B-component of the color.
	 * 
	 * @param b the value in range <0.0, 1.0> 
	 */
	public void setB(double b)
	{
		this.b = b;
	}
	
	/**
	 * get color in HEX. For example 0xFFFFFF, which
	 * means completely white. 
	 * 
	 * @return a color in HEX
	 */
	public int getHex()
	{
		return this.hex;
	}
	
	/**
	 * Setting color in HEX format. For example 0xFFFFFF will create 
	 * a color in RGB(255, 255, 255), which means completely white.
	 * 
	 * @param hex Color in HEX format
	 */	
	public void setHex(int hex)
	{
		this.hex = (~~hex) & 0xffffff;
		this.updateRGB();
	}
	
	/**
	 * Setting color in RGB mode. Each of R, G, B should be in 
	 * range <0.0, 1.0>. 
	 * 
	 * @param r the R-component of Color.
	 * @param g the G-component of Color.
	 * @param b the B-component of Color.
	 * 
	 * @return a current color
	 */
	public Color setRGB(double r, double g, double b)
	{
		this.setR(r);
		this.setG(g);
		this.setB(b);
		this.updateHex();
		
		return this;
	}
	
	/**
	 * Setting color based on HSV color model. Each input values H, S, V
	 * should be in range <0.0, 1.0>.
	 * 
	 * This method based on MochiKit implementation by Bob Ippolito
	 * 
	 * @param h the hue
	 * @param s the saturation
	 * @param v the value
	 * 
	 * @return a current color
	 */
	public Color setHSV(double h, double s, double v)
	{
		double r = 0, g = 0, b = 0, f, p, q, t;

		if (v == 0.0) 
		{
			r = g = b = 0.0;
		} 
		else 
		{
			int i = (int) Math.floor(h * 6.0);
			f = (h * 6.0) - i;
			p = v * (1.0 - s);
			q = v * (1.0 - (s * f));
			t = v * (1.0 - (s * (1.0 - f)));

			switch (i) {

			case 1: r = q; g = v; b = p; break;
			case 2: r = p; g = v; b = t; break;
			case 3: r = p; g = q; b = v; break;
			case 4: r = t; g = p; b = v; break;
			case 5: r = v; g = p; b = q; break;
			case 6: // fall through
			case 0: r = v; g = t; b = p; break;

			}

		}

		return this.setRGB(r, g, b);
	}

	/**
	 * Set value of the color from another color.
	 * 
	 * @param color the other color
	 */
	public void copy(Color color)
	{
		this.setRGB(color.getR(), color.getG(), color.getB());
	}
	
	/**
	 * Set value of color from gamma.
	 * 
	 * @param color the gamma
	 * 
	 * @return the current color
	 */
	public Color copyGammaToLinear(Color color)
	{
		this.setR(color.getR() * color.getR());
		this.setG(color.getG() * color.getG());
		this.setB(color.getB() * color.getB());
		
		return this;
	}

	/**
	 * Set value of the gamma from color.
	 * 
	 * @param color the color
	 * 
	 * @return a gamma
	 */
	public Color copyLinearToGamma(Color color)
	{

		this.setR(Math.sqrt(color.getR()));
		this.setG(Math.sqrt(color.getG()));
		this.setB(Math.sqrt(color.getB()));
		return this;
	}

	/**
	 * This method will convert gamma to color
	 * 
	 * @return a current color
	 */
	public Color convertGammaToLinear()
	{
		this.setR(this.getR() * this.getR());
		this.setG(this.getG() * this.getG());
		this.setB(this.getB() * this.getB());

		return this;
	}

	/**
	 * This method will convert color to gamma
	 * 
	 * @return a current gamma
	 */
	public Color convertLinearToGamma()
	{
		this.setR(Math.sqrt(this.getR()));
		this.setG(Math.sqrt(this.getG()));
		this.setB(Math.sqrt(this.getB()));

		return this;
	}

	/**
	 * Linearly interpolates between the current color and input color.
	 * 
	 * @param color the input color
	 * @param alpha the alpha value in range <0.0, 1.0>
	 */
	public void lerp(Color color, double alpha)
	{
		this.setR(this.getR() + (color.getR() - this.getR()) * alpha);
		this.setG(this.getG() + (color.getG() - this.getG()) * alpha);
		this.setB(this.getB() + (color.getB() - this.getB()) * alpha);
	}

	/**
	 * Clone the current color class.
	 * (color.clone() != color).
	 * 
	 * @return a new color instance, based on the current color class
	 */
	public Color clone()
	{
		return new Color(this.hex);
	}
	
	/**
	 * get Color class description by multiplying each value by 255. 
	 * Please not this is not real used values. This is just for readability. 
	 */
	public String toString()
	{
		return "rgb(" + Math.floor(this.r * 255) + ',' + Math.floor(this.g * 255) + ','
				+ Math.floor(this.b * 255) + ")";
	}
	
	/**
	 * Update HEX value of the color by RGB values.
	 */
	private void updateHex()
	{
		this.hex = ~~((int) Math.floor(this.r * 255)) << 16
				^ ~~((int) Math.floor(this.g * 255)) << 8 ^ ~~((int) Math.floor(this.b * 255));
	}

	/**
	 * Update RGB value of the color by HEX value.
	 */
	private void updateRGB()
	{
		this.setR((this.hex >> 16 & 255) / 255.0);
		this.setG((this.hex >> 8 & 255) / 255.0);
		this.setB((this.hex & 255) / 255.0);
	}
}
