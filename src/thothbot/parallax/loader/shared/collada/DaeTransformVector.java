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

package thothbot.parallax.loader.shared.collada;

import thothbot.parallax.core.shared.core.Vector3;

import com.google.gwt.xml.client.Node;

public class DaeTransformVector extends DaeTransform 
{
	public DaeTransformVector(Node node) {
		super(node);
	}

	public Vector3 getObject() 
	{
		double[] data = getData();
		return new Vector3( data[ 0 ], data[ 1 ], data[ 2 ] );
	}
}
