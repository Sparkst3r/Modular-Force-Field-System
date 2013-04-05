package mffs.muoxing;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelCube extends ModelBase
{
	public static final ModelCube INSTNACE = new ModelCube();
	private ModelRenderer cube;

	public ModelCube()
	{
		this.cube = new ModelRenderer(this, 0, 0);
		int size = 16;
		this.cube.addBox(-size / 2, -size / 2, -size / 2, size, size, size);
		this.cube.setTextureSize(112, 70);
		this.cube.mirror = true;
	}

	public void render()
	{
		float f = 0.0625f;
		this.cube.render(f);
	}
}
