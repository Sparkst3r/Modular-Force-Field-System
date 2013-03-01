package mffs.common.module;

import java.util.ArrayList;
import java.util.List;

import mffs.common.ProjectorTypes;
import mffs.common.item.ItemMFFS;
import mffs.common.mode.ItemModeAdvancedCube;
import mffs.common.mode.ItemModeContainment;
import mffs.common.mode.ItemModeCube;
import mffs.common.mode.ItemModeDeflector;
import mffs.common.mode.ItemModeDiagonalWall;
import mffs.common.mode.ItemModeSphere;
import mffs.common.mode.ItemModeTube;
import mffs.common.mode.ItemModeWall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

public abstract class ItemOptionBase extends ItemMFFS
{

	private static List instances = new ArrayList();

	public ItemOptionBase(int i, String name)
	{
		super(i, name);
		setMaxStackSize(8);
		instances.add(this);
		this.setNoRepair();
	}

	public static List<ItemOptionBase> get_instances()
	{
		return instances;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		if ((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54)))
		{
			info.add("compatible with:");

			if (ItemModeWall.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.wall));
			}
			if (ItemModeDiagonalWall.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.diagonallywall));
			}
			if (ItemModeDeflector.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.deflector));
			}
			if (ItemModeTube.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.tube));
			}
			if (ItemModeSphere.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.sphere));
			}
			if (ItemModeCube.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.cube));
			}
			if (ItemModeAdvancedCube.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.AdvCube));
			}
			if (ItemModeContainment.supportsOption(this))
			{
				info.add(ProjectorTypes.getdisplayName(ProjectorTypes.containment));
			}
		}
		else
		{
			info.add("compatible with: (Hold Shift)");
		}
	}
}