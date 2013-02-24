package mffs.common.upgrade;

import java.util.List;

import mffs.common.MFFSMachine;
import mffs.common.item.ItemMFFSBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

public class ItemProjectorFieldModulatorStrength extends ItemMFFSBase
{
	public ItemProjectorFieldModulatorStrength(int i)
	{
		super(i);
		setIconIndex(65);
		setMaxStackSize(64);
		this.setNoRepair();

	}

	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		if ((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54)))
		{
			info.add("compatible with:");
			info.add("MFFS " + MFFSMachine.Projector.getName());
			info.add("MFFS " + MFFSMachine.DefenceStation.getName());
		}
		else
		{
			info.add("compatible with: (Hold Shift)");
		}
	}
}