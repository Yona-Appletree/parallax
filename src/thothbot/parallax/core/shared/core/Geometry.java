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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.objects.Bone;
import thothbot.parallax.core.shared.objects.Line;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.objects.Object3D;
import thothbot.parallax.core.shared.objects.ParticleSystem;
import thothbot.parallax.core.shared.objects.Ribbon;

import com.google.gwt.core.client.GWT;

/**
 * Base class for geometries
 * <pre>
 * {@code
 * Geometry geometry = new Geometry();
 * 
 * geometry.getVertices().add( new Vector3( -10, 10, 0 ) ); 
 * geometry.getVertices().add( new Vector3( -10, -10, 0 ) );
 * geometry.getVertices().add( new Vector3( 10, -10, 0 ) );  
 * geometry.getFaces().add( new Face3( 0, 1, 2 ) );  
 * 
 * geometry.computeBoundingSphere();
 * }
 * </pre>
 * 
 * @author thothbot
 *
 */
public class Geometry extends GeometryBuffer implements Geometric
{
	public class MorphColor
	{
		public String name;
		public List<Color> colors;
	}
	
	public class MorphNormal
	{
		public List<Vector3> faceNormals;
		public List<VertextNormal> vertexNormals;
	}
	
	public class VertextNormal
	{
		public Vector3 a;
		public Vector3 b;
		public Vector3 c;
		public Vector3 d;
	}
	
	public class MorphTarget
	{
		public String name;
		public List<Vector3> vertices;
	}
	
	// Array of vertices.
	private List<Vector3> vertices;
	
	private ArrayList<Vector3> tempVerticles;

	private List<Color> colors;

	private List<Face3> faces;

	private List<List<UV>> faceUvs;
	
	private List<List<List<UV>>> faceVertexUvs;
		
	// Array of materials.
	private List<Material> materials;

	private List<MorphTarget> morphTargets;
	private List<MorphColor> morphColors;
	private List<MorphNormal> morphNormals;

	private List<Vector4> skinWeights;

	private List<Vector4> skinIndices;
	
	private List<Vector3> skinVerticesA;
	private List<Vector3> skinVerticesB;

	public List<List<Integer>> sortArray;
	
	private boolean isMorphTargetsNeedUpdate;
	
	private List<GeometryGroup> geometryGroups;
	private Map<String, GeometryGroup> geometryGroupsCache;
		
	private Object3D debug;
	
	public Geometry() {
		super();

		this.vertices = new ArrayList<Vector3>();
		this.colors = new ArrayList<Color>(); // one-to-one vertex colors, used in ParticleSystem, Line and Ribbon

		this.faces = new ArrayList<Face3>();

		this.faceUvs = new ArrayList<List<UV>>();
		this.faceVertexUvs = new ArrayList<List<List<UV>>>();
		this.faceVertexUvs.add(new ArrayList<List<UV>>());

		this.morphTargets = new ArrayList<MorphTarget>();
		this.morphNormals = new ArrayList<MorphNormal>();
		this.morphColors = new ArrayList<MorphColor>();

		this.skinWeights = new ArrayList<Vector4>();
		this.skinIndices = new ArrayList<Vector4>();

		this.debug = new Object3D();
	}
	
	public Map<String, GeometryGroup> getGeometryGroupsCache() {
		return this.geometryGroupsCache;
	}
	
	public void setGeometryGroupsCache(Map<String, GeometryGroup> geometryGroups) {
		this.geometryGroupsCache = geometryGroups;
	}
	
	public List<GeometryGroup> getGeometryGroups() {
		return this.geometryGroups;
	}
	
	public void setGeometryGroups(List<GeometryGroup> geometryGroupsList) {
		this.geometryGroups = geometryGroupsList;
	}
	
	public Object3D getDebug() {
		return this.debug;
	}
	
	/**
	 * Gets the List of skinning weights, matching number and order of vertices.
	 */
	public List<Vector4> getSkinWeights() {
		return this.skinWeights;
	}
	
	/**
	 * Gets the List of skinning indices, matching number and order of vertices.
	 */
	public List<Vector4> getSkinIndices() {
		return this.skinIndices;
	}
	
	public List<Vector3> getSkinVerticesA() {
		return this.skinVerticesA;
	}
	
	public void setSkinVerticesA(List<Vector3> skinVerticesA) {
		this.skinVerticesA = skinVerticesA;
	}
	
	public List<Vector3> getSkinVerticesB() {
		return this.skinVerticesB;
	}
	
	public void setSkinVerticesB(List<Vector3>  skinVerticesB) {
		this.skinVerticesB = skinVerticesB;
	}

	public boolean isMorphTargetsNeedUpdate() {
		return isMorphTargetsNeedUpdate;
	}

	public void setMorphTargetsNeedUpdate(boolean isMorphTargetsNeedUpdate) {
		this.isMorphTargetsNeedUpdate = isMorphTargetsNeedUpdate;
	}

	public void setFaceUvs(List<List<UV>> faceUvs) {
		this.faceUvs = faceUvs;
	}

	/**
	 * Gets the List of face {@link UV} layers.
	 * Each UV layer is an List of {@link UV} matching order and number of faces.
	 */
	public List<List<UV>> getFaceUvs() {
		return faceUvs;
	}

	public void setColors(List<Color> colors) {
		this.colors = colors;
	}

	/**
	 * Gets List of vertex {@link Color}s, matching number and order of vertices.
	 * <p>
	 * Used in {@link ParticleSystem}, {@link Line} and {@link Ribbon}.<br>
	 * {@link Mesh}es use per-face-use-of-vertex colors embedded directly in faces.
	 */
	public List<Color> getColors() {
		return colors;
	}

	public void setFaces(List<Face3> faces) {
		this.faces = faces;
	}

	/**
	 * Gets the List of triangles: {@link Face3} or/and quads: {@link Face4}
	 */
	public List<Face3> getFaces() {
		return faces;
	}
	
	/**
	 * Gets the List of {@link Material}s.
	 */
	public List<Material> getMaterials() 
	{
		return this.materials;
	}
	
	public void setMaterials(List<Material> materials) 
	{
		this.materials = materials;
	}
	
	public void setVertices(List<Vector3> vertices) 
	{
		this.vertices = vertices;
	}

	/**
	 * Gets List of {@link Vector3}.
	 */
	public List<Vector3> getVertices() 
	{
		return vertices;
	}

	/**
	 * Gets the List of {@link Geometry.MorphTarget}. 
	 * Morph vertices match number and order of primary vertices.
	 */
	public List<MorphTarget> getMorphTargets() {
		return morphTargets;
	}

	public List<MorphNormal> getMorphNormals() {
		return morphNormals;
	}
	
	/**
	 * Gets the List of {@link Geometry.MorphColor}. Morph colors have similar structure as {@link Geometry.MorphTarget}.
	 * Morph colors can match either number and order of faces (face colors) or number of vertices (vertex colors).
	 */
	public List<MorphColor> getMorphColors() {
		return this.morphColors;
	}
	
	/**
	 * Gets the List of face {@link UV} layers.
	 * Each UV layer is an List of UV matching order and number of vertices in faces.
	 */
	public List<List<List<UV>>> getFaceVertexUvs(){
		return this.faceVertexUvs;
	}
	
	public void setFaceVertexUvs(List<List<List<UV>>> faceVertexUvs) {
		this.faceVertexUvs = faceVertexUvs;
	}

	/**
	 * Computes centroids for all faces.
	 */
	public void computeCentroids()
	{
		for (Face3 face: this.faces) {
			face.getCentroid().set(0,0,0);

			if (face.getClass() == Face3.class) 
			{
				Face3 face3 = (Face3)face;
				face3.getCentroid().add(this.vertices.get(face3.getA()));
				face3.getCentroid().add(this.vertices.get(face3.getB()));
				face3.getCentroid().add(this.vertices.get(face3.getC()));
				face3.getCentroid().divide(3);

			} 
			else if (face.getClass() == Face4.class) 
			{
				Face4 face4 = (Face4)face;
				face4.getCentroid().add(this.vertices.get(face4.getA()));
				face4.getCentroid().add(this.vertices.get(face4.getB()));
				face4.getCentroid().add(this.vertices.get(face4.getC()));
				face4.getCentroid().add(this.vertices.get(face4.getD()));
				face4.getCentroid().divide(4);
			}

		}
	}

	/**
	 * Computes vertex normals by averaging face normals.
	 * Face normals must be existing / computed beforehand.
	 */
	public void computeVertexNormals()
	{
		// create internal buffers for reuse when calling this method repeatedly
		// (otherwise memory allocation / deallocation every frame is big resource hog)

		if (this.tempVerticles == null) 
		{

			this.tempVerticles = new ArrayList<Vector3>(this.vertices.size());

			for (int v = 0, vl = this.vertices.size(); v < vl; v++)
				this.tempVerticles.add(v, new Vector3());


			for (Face3 face : this.faces) 
			{

				if (face.getClass() == Face3.class)
				{
					List<Vector3> normals = face.getVertexNormals();
					normals.clear();
					normals.add(new Vector3());
					normals.add(new Vector3());
					normals.add(new Vector3());
				} 
				else if (face.getClass() == Face4.class) 
				{
					List<Vector3> normals = face.getVertexNormals();
					normals.clear();
					normals.add(new Vector3());
					normals.add(new Vector3());
					normals.add(new Vector3());
					normals.add(new Vector3());
				}
			}
		} 
		else 
		{
			for (int v = 0, vl = this.vertices.size(); v < vl; v++)
				this.tempVerticles.get(v).set(0,0,0);
		}

		for (Face3 face : this.faces) 
		{
			if (face.getClass() == Face3.class) 
			{
				Face3 face3 = face;
				this.tempVerticles.get(face3.getA()).add(face3.getNormal());
				this.tempVerticles.get(face3.getB()).add(face3.getNormal());
				this.tempVerticles.get(face3.getC()).add(face3.getNormal());

			}
			else if (face.getClass() == Face4.class) 
			{
				Face4 face4 = (Face4)face;
				this.tempVerticles.get(face4.getA()).add(face4.getNormal());
				this.tempVerticles.get(face4.getB()).add(face4.getNormal());
				this.tempVerticles.get(face4.getC()).add(face4.getNormal());
				this.tempVerticles.get(face4.getD()).add(face4.getNormal());
			}
		}

		for (int v = 0, vl = this.vertices.size(); v < vl; v ++ )
			this.tempVerticles.get(v).normalize();

		for (Face3 face : this.faces) 
		{
			if (face.getClass() == Face3.class) 
			{
				Face3 face3 = face;
				face3.getVertexNormals().get(0).copy(this.tempVerticles.get(face3.getA()));
				face3.getVertexNormals().get(1).copy(this.tempVerticles.get(face3.getB()));
				face3.getVertexNormals().get(2).copy(this.tempVerticles.get(face3.getC()));

			} 
			else if (face.getClass() == Face4.class) 
			{
				Face4 face4 = (Face4)face;
				face4.getVertexNormals().get(0).copy(this.tempVerticles.get(face4.getA()));
				face4.getVertexNormals().get(1).copy(this.tempVerticles.get(face4.getB()));
				face4.getVertexNormals().get(2).copy(this.tempVerticles.get(face4.getC()));
				face4.getVertexNormals().get(3).copy(this.tempVerticles.get(face4.getD()));
			}

		}
	}
	
	/**
	 * Computes face normals.
	 */
	public void computeFaceNormals()
	{
		computeFaceNormals(false);
	}

	public void computeFaceNormals(Boolean useVertexNormals)
	{
		Vector3 cb = new Vector3(), ab = new Vector3();

		for (Face3 face: this.faces) 
		{
			if (useVertexNormals && face.getVertexNormals().size() > 0) 
			{
				cb.set(0,0,0);
				for(Vector3 vertexNormal: face.getVertexNormals())
					cb.add(vertexNormal);

				cb.divide(3);
				if (!cb.isZero())
					cb.normalize();

				face.getNormal().copy(cb);
			} 
			else 
			{
				Vector3 vA = this.vertices.get(face.getA());
				Vector3 vB = this.vertices.get(face.getB());
				Vector3 vC = this.vertices.get(face.getC());

				cb.sub(vC, vB);
				ab.sub(vA, vB);
				cb.cross(ab);

				if (!cb.isZero())
					cb.normalize();

				face.getNormal().copy(cb);
			}
		}
	}
	
	public void computeMorphNormals() 
	{	
		// save original normals
		// - create temp variables on first access
		//   otherwise just copy (for faster repeated calls)
		for (Face3 face: getFaces()) 
		{		
			face.getOriginalNormal().copy(face.getNormal());

			for (int i = 0; i < face.getVertexNormals().size(); i++) 
			{
				if ( face.getOriginalVertexNormals().size() <= i 
						|| face.getOriginalVertexNormals().get( i ) == null)
					face.getOriginalVertexNormals().add( i, face.getVertexNormals().get( i ).clone());
				else
					face.getOriginalVertexNormals().get( i ).copy( face.getVertexNormals().get( i ) );
			}
		}

		// Use temp geometry to compute face and vertex normals for each morph
		Geometry tmpGeo = new Geometry();
		tmpGeo.faces = this.faces;

		for (int j = 0; j < this.morphTargets.size(); j++) 
		{
			// Create on first access
			if ( this.morphNormals.size() == j ) 
			{
				MorphNormal morphNormal = new MorphNormal();
				morphNormal.faceNormals = new ArrayList<Vector3>();
				morphNormal.vertexNormals = new ArrayList<VertextNormal>();

				for (Face3 face: getFaces()) 
				{		
					VertextNormal vertexNormals = new VertextNormal();
					if ( face instanceof Face3 )
					{
						vertexNormals.a = new Vector3();
						vertexNormals.b = new Vector3();
						vertexNormals.c = new Vector3();
					}
					else
					{
						vertexNormals.a = new Vector3();
						vertexNormals.b = new Vector3();
						vertexNormals.c = new Vector3();
						vertexNormals.c = new Vector3();
					}

					morphNormal.faceNormals.add( new Vector3() );
					morphNormal.vertexNormals.add( vertexNormals );
				}
				
				this.morphNormals.add( morphNormal );
			}

			MorphNormal morphNormals = this.morphNormals.get( j );

			// Set vertices to morph target
			tmpGeo.setVertices( this.morphTargets.get( j ).vertices );

			// Compute morph normals
			tmpGeo.computeFaceNormals();
			tmpGeo.computeVertexNormals();

			// Store morph normals
			for ( int f = 0, fl = getFaces().size(); f < fl; f ++ ) 
			{
				Face3 face = getFaces().get(f);

				Vector3 faceNormal = morphNormals.faceNormals.get(f);
				VertextNormal vertexNormals = morphNormals.vertexNormals.get(f);

				faceNormal.copy( face.getNormal() );

				if ( face instanceof Face3 ) 
				{
					vertexNormals.a.copy( face.getVertexNormals().get(0) );
					vertexNormals.b.copy( face.getVertexNormals().get(1) );
					vertexNormals.c.copy( face.getVertexNormals().get(2) );
				} 
				else 
				{
					vertexNormals.a.copy( face.getVertexNormals().get(0) );
					vertexNormals.b.copy( face.getVertexNormals().get(1) );
					vertexNormals.c.copy( face.getVertexNormals().get(2) );
					vertexNormals.d.copy( face.getVertexNormals().get(3) );
				}
			}
		}

		// Restore original normals
		for ( int f = 0, fl = getFaces().size(); f < fl; f ++ ) 
		{
			Face3 face = getFaces().get(f);
			face.setNormal( face.getOriginalNormal() );
			face.setVertexNormals( face.getOriginalVertexNormals() );
		}
	}
	
	/**
	 * Computes vertex tangents.<br>
	 * Based on <a href="http://www.terathon.com/code/tangent.html">terathon.com</a>
	 * <p>
	 * Geometry must have vertex {@link UV}s (layer 0 will be used).
	 */
	@Override
	public void computeTangents()
	{
		Face3 face;
		UV[] uv = new UV[0];
		int v, vl, f, fl, i, vertexIndex;
		List<Vector3> tan1 = new ArrayList<Vector3>(), 
				tan2 = new ArrayList<Vector3>();
		Vector3 tmp = new Vector3(), tmp2 = new Vector3();

		for (v = 0,vl = this.vertices.size(); v<vl; v++) 
		{
			tan1.add(v, new Vector3());
			tan2.add(v, new Vector3());
		}
		
		for (f = 0, fl = this.faces.size(); f < fl; f++) 
		{

			face = this.faces.get(f);
			uv = this.faceVertexUvs.get(0).get(f).toArray(uv); // use UV layer 0 for tangents

			if (face.getClass() == Face3.class) {
				handleTriangle(face.getA(), face.getB(), face.getC(), 0, 1, 2, uv, tan1, tan2);

			} else if (face.getClass() == Face4.class) {
				Face4 face4 = (Face4)face;
				handleTriangle(face4.getA(), face4.getB(), face4.getC(), 0, 1, 2, uv, tan1, tan2);
				handleTriangle(face4.getA(), face4.getB(), face4.getD(), 0, 1, 3, uv, tan1, tan2);

			}
		}

		for (f = 0, fl = this.faces.size(); f < fl; f ++ ) 
		{

			face = this.faces.get(f);

			for (i = 0; i < face.getVertexNormals().size(); i++) {

				Vector3 n = new Vector3();
				n.copy(face.getVertexNormals().get(i));

				vertexIndex = face.getFlat()[i];

				Vector3 t = tan1.get(vertexIndex);

				// Gram-Schmidt orthogonalize

				tmp.copy(t);
				n.multiply(n.dot(t));
				tmp.sub(n);
				tmp.normalize();

				// Calculate handedness

				tmp2.cross(face.getVertexNormals().get(i), t);
				double test = tmp2.dot(tan2.get(vertexIndex));
				double w = (test < 0.0) ? -1.0 : 1.0;
				
				face.getVertexTangents().add(i, new Vector4(tmp.x,tmp.y,tmp.z,w));
			}
		}

		setHasTangents(true);
	}

	/**
	 * Computes bounding box of the geometry.
	 */
	@Override
	public void computeBoundingBox() 
	{

		if ( getBoundingBox() == null )
			setBoundingBox( new BoundingBox() );

		BoundingBox boundingBox = getBoundingBox();
		if(this.vertices.size() == 0 )
		{
			boundingBox.min.set( 0, 0, 0 );
			boundingBox.max.set( 0, 0, 0 );
			return;
		}

		Vector3 firstPosition = this.vertices.get( 0 );

		boundingBox.min.copy( firstPosition );
		boundingBox.max.copy( firstPosition );

		Vector3 min = boundingBox.min;
		Vector3 max = boundingBox.max;

		for(Vector3 position: this.vertices) 
		{
			if ( position.x < min.x ) {
				min.x = position.x;

			} else if ( position.x > max.x ) {
				max.x = position.x;
			}

			if ( position.y < min.y ) {
				min.y = position.y;
			} else if ( position.y > max.y ) {
				max.y = position.y;
			}

			if ( position.z < min.z ) {
				min.z = position.z;
			} else if ( position.z > max.z ) {
				max.z = position.z;
			}
		}
	}

	/**
	 * Computes bounding sphere of the geometry.
	 * <p>
	 * Neither bounding boxes or bounding spheres are computed by default. 
	 * They need to be explicitly computed, otherwise they are null.
	 */
	@Override
	public void computeBoundingSphere()
	{	
		double maxRadiusSq = 0;

		if ( getBoundingSphere() == null ) 
			setBoundingSphere( new BoundingSphere(0) );
		
		BoundingSphere boundingSphere = getBoundingSphere();

		for ( int i = 0, l = this.vertices.size(); i < l; i ++ ) 
		{
			double radiusSq = this.vertices.get( i ).lengthSq();
			if ( radiusSq > maxRadiusSq ) 
				maxRadiusSq = radiusSq;
		}

		boundingSphere.radius = Math.sqrt( maxRadiusSq );
	}
	
	private void handleTriangle(int a, int b, int c, int ua, int ub, int uc, UV[] uv, List<Vector3> tan1, List<Vector3> tan2)
	{
		Vector3 vA = this.vertices.get(a);
		Vector3 vB = this.vertices.get(b);
		Vector3 vC = this.vertices.get(c);
		
		UV uvA = uv[ua];
		UV uvB = uv[ub];
		UV uvC = uv[uc];
		
		double x1 = vB.x - vA.x;
		double x2 = vC.x - vA.x;
		double y1 = vB.y - vA.y;
		double y2 = vC.y - vA.y;
		double z1 = vB.z - vA.z;
		double z2 = vC.z - vA.z;
		
		double s1 = uvB.getU() - uvA.getU();
		double s2 = uvC.getU() - uvA.getU();
		double t1 = uvB.getV() - uvA.getV();
		double t2 = uvC.getV() - uvA.getV();
		
		double r = 1.0 / (s1 * t2 - s2 * t1);
		
		Vector3 sdir = new Vector3();
		sdir.set((t2*x1-t1*x2)*r,
				  (t2*y1-t1*y2)*r,
				  (t2*z1-t1*z2)*r);
		
		Vector3 tdir = new Vector3();
		tdir.set((s1*x2 - s2*x1)*r,
				  (s1*y2 - s2*y1)*r,
				  (s1*z2 - s2*z1)*r);
		
		tan1.get(a).add(sdir);
		tan1.get(b).add(sdir);
		tan1.get(c).add(sdir);

		tan2.get(a).add(tdir);
		tan2.get(b).add(tdir);
		tan2.get(c).add(tdir);
	}


	/**
	 * Makes matrix transform directly into vertex coordinates.	
	 */
	public void applyMatrix(Matrix4 matrix)
	{
		Matrix4 matrixRotation = new Matrix4();
		matrixRotation.extractRotation(matrix);

		for(Vector3 verticle: this.vertices)
			matrix.multiplyVector3(verticle);

		for(Face3 face: this.faces) {
			matrixRotation.multiplyVector3(face.normal);
			for(Vector3 vertexNormal: face.vertexNormals)
				matrixRotation.multiplyVector3(vertexNormal);
			
			matrix.multiplyVector3(face.centroid);
		}		
	}

	/**
	 * Checks for duplicate vertices with hashmap.
	 * Duplicated vertices are removed and faces' vertices are updated.
	 */
	public int mergeVertices() 
	{
		// Hashmap for looking up vertice by position coordinates (and making sure they are unique)
		Map<String, Integer> verticesMap = GWT.isScript() ? 
				new FastMap<Integer>() : new HashMap<String, Integer>();
		List<Vector3> unique = new ArrayList<Vector3>();
		List<Integer> changes = new ArrayList<Integer>();

		// number of decimal points, eg. 4 for epsilon of 0.0001
		double precisionPoints = 4; 
		double precision = Math.pow( 10, precisionPoints );

		for ( int i = 0; i < this.vertices.size(); i ++ ) 
		{
			Vector3 v = this.vertices.get( i );
			String key = Math.round( v.x * precision ) + "_" + Math.round( v.y * precision ) + "_"  + Math.round( v.z * precision );

			if ( !verticesMap.containsKey(key)) 
			{
				verticesMap.put(key, i);
				unique.add(v);
				changes.add( i , unique.size() - 1);

			} 
			else 
			{
				//console.log('Duplicate vertex found. ', i, ' could be using ', verticesMap[key]);
				changes.add( i , changes.get( verticesMap.get( key ) ));
			}
		}


		// Start to patch face indices
		for ( int i = 0; i < this.faces.size(); i ++ ) 
		{
			Face3 face = this.faces.get( i );

			if ( face.getClass() == Face3.class ) 
			{
				Face3 face3 = (Face3)face;
				face3.setA(changes.get( face3.getA() ));
				face3.setB(changes.get( face3.getB() ));
				face3.setC(changes.get( face3.getC() ));

			} 
			else if ( face.getClass() == Face4.class ) 
			{
				Face4 face4 = (Face4)face;

				face4.setA(changes.get( face4.getA() ));
				face4.setB(changes.get( face4.getB() ));
				face4.setC(changes.get( face4.getC()));
				face4.setD(changes.get( face4.getD() ));
 
				// check dups in (a, b, c, d) and convert to -> face3
				List<Integer> o = Arrays.asList(face4.getA(), face4.getB(), face4.getC(), face4.getD());
				List<Integer> a = Arrays.asList(face4.getA(), face4.getB(), face4.getC(), face4.getD()); 

				for (int k=3; k>0; k--) 
				{
					if ( o.indexOf(a.get(k)) != k ) 
					{
						// console.log('faces', face.a, face.b, face.c, face.d, 'dup at', k);
						o.remove(k);
						this.faces.set( i, new Face3(o.get(0), o.get(1), o.get(2), face.getNormal(), face.getColor(), face.getMaterialIndex() ));
						
						for (int j=0,jl = this.faceVertexUvs.size(); j<jl; j++) 
						{
							List<UV> u = this.faceVertexUvs.get(j).get(i);
							if (u != null) 
								u.remove(k);
						}
						
						this.faces.get( i ).setVertexColors( face.getVertexColors() );
						
						break;
					}
				}
			}
		}

		// Use unique set of vertices
		int diff = this.vertices.size() - unique.size();
		this.vertices = unique;
		return diff;
	}
	
	public Geometry clone() 
	{
		Geometry cloneGeo = new Geometry();

		List<Vector3> vertices = getVertices();
		List<Face3> faces = getFaces();
		List<List<UV>> uvs = getFaceVertexUvs().get(0);

		// materials

		if ( getMaterials() != null ) 
		{
			cloneGeo.setMaterials(new ArrayList<Material>(getMaterials()));
		}

		// vertices

		for ( int i = 0, il = vertices.size(); i < il; i ++ ) 
		{
			Vector3 vertex = vertices.get(i);

			cloneGeo.getVertices().add( vertex.clone() );
		}

		// faces

		for ( int i = 0, il = faces.size(); i < il; i ++ ) 
		{
			Face3 face = faces.get(i);

			cloneGeo.getFaces().add( (face instanceof Face3) ? face.clone() : ((Face4)face).clone() );
		}

		// uvs

		for ( int i = 0, il = uvs.size(); i < il; i ++ ) 
		{
			List<UV> uv = uvs.get( i );
			List<UV> uvCopy = new ArrayList<UV>();

			for ( int j = 0, jl = uv.size(); j < jl; j ++ ) 
			{
				uvCopy.add( new UV( uv.get( j ).getU(), uv.get( j ).getV() ) );
			}

			cloneGeo.getFaceVertexUvs().get(0).add( uvCopy );
		}

		return cloneGeo;
	}

}
