/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
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
 * Squirrel. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.core.shared.curves.parametric;

import thothbot.parallax.core.shared.core.Vector3;

public final class CurveDecoratedTorusKnot5c extends CurveDecoratedTorusKnot4a 
{
	@Override
	public Vector3 getPoint(double t) 
	{
		double fi = t * Math.PI * 2;
		double x = Math.cos(4.0 * fi) * (1.0 + 0.5 * (Math.cos(5.0 * fi) + 0.4 * Math.cos(20.0 * fi)));
		double y = Math.sin(4.0 * fi) * (1.0 + 0.5 * (Math.cos(5.0 * fi) + 0.4 * Math.cos(20.0 * fi)));
		double z = 0.35 * Math.sin(15.0 * fi);

		return new Vector3(x, y, z).multiply(this.scale);
	}
}
