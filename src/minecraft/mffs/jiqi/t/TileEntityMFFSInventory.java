package mffs.jiqi.t;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

/**
 * All TileEntities that have an inventory should extend this.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityMFFSInventory extends TileEntityMFFS implements IInventory
{
	/**
	 * The inventory of the TileEntity.
	 */
	protected ItemStack[] inventory = new ItemStack[this.getSizeInventory()];

	/**
	 * The amount of players using the inventory.
	 */
	protected int playersUsing = 0;

	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		objects.add(nbt);
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream)
	{
		final boolean prevActivate = this.isActive();

		super.onReceivePacket(packetID, dataStream);

		if (packetID == 1 && this.worldObj.isRemote)
		{
			try
			{
				this.readFromNBT(PacketManager.readNBTTagCompound(dataStream));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Inventory Methods
	 */
	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventory[i];
	}

	@Override
	public String getInvName()
	{
		return this.getBlockType().getLocalizedName();
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventory[i] = itemstack;
		if ((itemstack != null) && (itemstack.stackSize > getInventoryStackLimit()))
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventory[i] != null)
		{
			if (this.inventory[i].stackSize <= j)
			{
				ItemStack itemstack = this.inventory[i];
				this.inventory[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = this.inventory[i].splitStack(j);
			if (this.inventory[i].stackSize == 0)
			{
				this.inventory[i] = null;
			}
			return itemstack1;
		}
		return null;
	}

	@Override
	public void openChest()
	{
		this.playersUsing++;
	}

	@Override
	public void closeChest()
	{
		this.playersUsing--;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistance(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID)
	{
		if (this.inventory[slotID] != null)
		{
			ItemStack itemstack = this.inventory[slotID];
			this.inventory[slotID] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
	{
		return true;
	}

	/**
	 * @return Returns if a specific slot is valid to input a specific itemStack.
	 * 
	 */
	public boolean canIncreaseStack(int slotID, ItemStack itemStack)
	{
		if (this.getStackInSlot(slotID) == null)
		{
			return true;
		}
		else
		{
			if (this.getStackInSlot(slotID).stackSize + 1 <= 64)
			{
				return this.getStackInSlot(slotID).isItemEqual(itemStack);
			}
		}

		return false;
	}

	public void incrStackSize(int slot, ItemStack itemStack)
	{
		if (this.getStackInSlot(slot) == null)
		{
			this.setInventorySlotContents(slot, itemStack.copy());
		}
		else if (this.getStackInSlot(slot).isItemEqual(itemStack))
		{
			this.getStackInSlot(slot).stackSize++;
		}
	}

	public Set<ItemStack> getCards()
	{
		Set<ItemStack> cards = new HashSet<ItemStack>();
		cards.add(this.getStackInSlot(0));
		return cards;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		NBTTagList nbtTagList = nbttagcompound.getTagList("Items");
		this.inventory = new ItemStack[this.getSizeInventory()];
		for (int i = 0; i < nbtTagList.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtTagList.tagAt(i);

			byte byte0 = nbttagcompound1.getByte("Slot");
			if ((byte0 >= 0) && (byte0 < this.inventory.length))
			{
				this.inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);

		NBTTagList nbtTagList = new NBTTagList();
		for (int i = 0; i < this.inventory.length; i++)
		{
			if (this.inventory[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
				nbtTagList.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbtTagList);
	}
}