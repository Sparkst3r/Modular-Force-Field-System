package mffs.common.module.fangyu;

import java.util.Set;

import mffs.api.IDefenseStation;
import mffs.api.IDefenseStationModule;
import mffs.common.MFFSConfiguration;
import mffs.common.module.ItemModule;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ItemModuleConfiscate extends ItemModule implements IDefenseStationModule
{
	public ItemModuleConfiscate(int i)
	{
		super(i, "moduleConfiscate");
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onDefend(IDefenseStation defenseStation, EntityLiving entityLiving)
	{
		Set<ItemStack> controlledStacks = defenseStation.getFilteredItems();

		int confiscationCount = 0;
		IInventory inventory = null;

		boolean hasPermission = false;

		if (!hasPermission && entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;
			inventory = player.inventory;
		}
		else if (entityLiving instanceof IInventory)
		{
			inventory = (IInventory) entityLiving;
		}

		if (inventory != null)
		{
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				// The ItemStack currently being checked.
				ItemStack checkStack = inventory.getStackInSlot(i);

				if (checkStack != null)
				{
					boolean stacksMatch = false;

					for (ItemStack itemStack : controlledStacks)
					{
						if (itemStack != null)
						{
							if (itemStack.isItemEqual(checkStack))
							{
								stacksMatch = true;
								break;
							}
						}
					}

					if ((defenseStation.getFilterMode() && stacksMatch) || (!defenseStation.getFilterMode() && !stacksMatch))
					{
						defenseStation.mergeIntoInventory(inventory.getStackInSlot(i));
						inventory.setInventorySlotContents(i, null);
						confiscationCount++;
					}
				}
			}

			if (confiscationCount > 0 && entityLiving instanceof EntityPlayer)
			{
				((EntityPlayer) entityLiving).addChatMessage("[" + defenseStation.getInvName() + "] " + confiscationCount + " of your items has been confiscated.");
			}
		}
		return false;
	}
}