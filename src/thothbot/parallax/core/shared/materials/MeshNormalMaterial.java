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

import thothbot.parallax.core.client.shaders.NormalShader;
import thothbot.parallax.core.client.shaders.Shader;
import thothbot.parallax.core.shared.cameras.Camera;

public final class MeshNormalMaterial extends Material implements HasWireframe
{
	private boolean isWireframe;
	private int wireframeLineWidth;
	
	public MeshNormalMaterial()
	{
		setWireframe(false);
		setWireframeLineWidth(1);
		
		setShading(Material.SHADING.FLAT);
	}

	public Shader getAssociatedShader()
	{
		return new NormalShader();
	}
	
	@Override
	public boolean isWireframe() {
		return this.isWireframe;
	}

	@Override
	public void setWireframe(boolean wireframe) {
		this.isWireframe = wireframe;
	}

	@Override
	public int getWireframeLineWidth() {
		return this.wireframeLineWidth;
	}

	@Override
	public void setWireframeLineWidth(int wireframeLineWidth) {
		this.wireframeLineWidth = wireframeLineWidth;
	}
	
	@Override
	public void refreshUniforms(Camera camera, boolean isGammaInput) 
	{
		super.refreshUniforms(camera, isGammaInput);
		
		getShader().getUniforms().get("opacity").setValue( getOpacity() );
	}
}
