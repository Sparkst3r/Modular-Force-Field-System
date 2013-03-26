package mffs.jiqi;

import mffs.jiqi.t.TAnQuan;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSecurityCenter extends BJiQi
{
	public BlockSecurityCenter(int i)
	{
		super(i, "securityCenter");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TAnQuan();
	}
}