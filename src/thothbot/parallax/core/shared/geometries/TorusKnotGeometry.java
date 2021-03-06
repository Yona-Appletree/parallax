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

package thothbot.parallax.core.shared.geometries;

import java.util.Arrays;

import thothbot.parallax.core.shared.core.Face4;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.UV;
import thothbot.parallax.core.shared.core.Vector3;

/**
 * <img src="http://thothbot.github.com/parallax/static/docs/torus_knot.gif" />
 * <p>
 * Torus knot geometry
 * 
 * @author thothbot
 *
 */
public final class TorusKnotGeometry extends Geometry
{

	public TorusKnotGeometry() 
	{
		this(200, 40, 64, 8);
	}
	
	public TorusKnotGeometry(int radius, int tube, int segmentsR, int segmentsT)
	{
		this(radius, tube, segmentsR, segmentsT, 2, 3, 1);
	}
			
	public TorusKnotGeometry(int radius, int tube, int segmentsR, int segmentsT, int p, int q, int heightScale ) 
	{
		super();
		
		int[][] grid = new int[segmentsR][];

		Vector3 tang = new Vector3();
		Vector3 n = new Vector3();
		Vector3 bitan = new Vector3();

		for ( int i = 0; i < segmentsR; ++ i )  	
		{
			grid[ i ] = new int[segmentsT];

			for ( int j = 0; j < segmentsT; ++ j ) 
			{
				double u = i / (double)segmentsR * 2.0 * p * Math.PI;
				double v = j / (double)segmentsT * 2.0 * Math.PI;

				Vector3 p1 = getPos( u,        v, q, p, radius, heightScale );
				Vector3 p2 = getPos( u + 0.01, v, q, p, radius, heightScale );
				
				tang.sub( p2, p1 );
				n.add( p2, p1 );

				bitan.cross( tang, n );
				n.cross( bitan, tang );
				bitan.normalize();
				n.normalize();

				double cx = -tube * Math.cos( v ); // TODO: Hack: Negating it so it faces outside.
				double cy = tube * Math.sin( v );

				p1.addX(cx * n.getX() + cy * bitan.getX());
				p1.addY(cx * n.getY() + cy * bitan.getY());
				p1.addZ(cx * n.getZ() + cy * bitan.getZ());

				grid[ i ][ j ] = vert( p1.getX(), p1.getY(), p1.getZ() );
			}
		}
		
		for ( int i = 0; i < segmentsR; ++ i ) 
		{
			for ( int j = 0; j < segmentsT; ++ j ) 
			{
				int ip = ( i + 1 ) % segmentsR;
				int jp = ( j + 1 ) % segmentsT;

				int a = grid[ i ][ j ];
				int b = grid[ ip ][ j ];
				int c = grid[ ip ][ jp ];
				int d = grid[ i ][ jp ];

				UV uva = new UV(           i / (double)segmentsR,           j / (double)segmentsT );
				UV uvb = new UV( ( i + 1.0 ) / (double)segmentsR,           j / (double)segmentsT );
				UV uvc = new UV( ( i + 1.0 ) / (double)segmentsR, ( j + 1.0 ) / (double)segmentsT );
				UV uvd = new UV(           i / (double)segmentsR, ( j + 1.0 ) / (double)segmentsT );

				getFaces().add( new Face4( a, b, c, d ) );
				getFaceVertexUvs().get( 0 ).add( Arrays.asList( uva, uvb, uvc, uvd ) );
			}
		}

		this.computeCentroids();
		this.computeFaceNormals();
		this.computeVertexNormals();
	}
	
	private int vert( double x, double y, double z ) 
	{
		getVertices().add( new Vector3( x, y, z ) );
		return getVertices().size() - 1;
	}

	private Vector3 getPos( double u, double v, int in_q, int in_p, int radius, int heightScale ) 
	{
		double cu = Math.cos( u );
		double su = Math.sin( u );
		double quOverP = in_q / (double)in_p * u;
		double cs = Math.cos( quOverP );

		return new Vector3( 
			radius * ( 2.0 + cs ) * cu * 0.5,
			radius * ( 2.0 + cs ) * su * 0.5,
			heightScale * radius * Math.sin( quOverP ) * 0.5
		);
	}
}
