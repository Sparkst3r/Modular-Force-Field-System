package mffs.common.tileentity;

import java.util.LinkedList;
import java.util.List;

import mffs.api.PointXYZ;
import mffs.common.FrequencyGrid;
import mffs.common.MachineTypes;
import mffs.common.ModularForceFieldSystem;
import mffs.common.NBTTagCompoundHelper;
import mffs.common.card.ItemCardDataLink;
import mffs.common.card.ItemCardSecurityLink;
import mffs.common.container.ContainerControlSystem;
import mffs.network.server.NetworkHandlerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileEntityControlSystem extends TileEntityMFFS implements ISidedInventory
{

	private TileEntityMFFS remote = null;
	protected String RemoteDeviceName = "";
	protected String RemoteDeviceTyp = "";
	protected boolean RemoteActive = false;
	protected boolean RemoteSwitchValue = false;
	protected short RemoteSwitchModi = 0;
	protected boolean RemoteSecurityStationlink = false;
	protected boolean RemotehasPowersource = false;
	protected boolean RemoteGUIinRange = false;
	protected int RemotePowerleft = 0;
	private ItemStack[] inventory;

	public TileEntityControlSystem()
	{
		this.inventory = new ItemStack[40];
	}

	@Override
	public List getFieldsForUpdate()
	{
		List NetworkedFields = new LinkedList();
		NetworkedFields.clear();

		NetworkedFields.addAll(super.getFieldsForUpdate());
		NetworkedFields.add("RemoteDeviceName");
		NetworkedFields.add("RemoteDeviceTyp");
		NetworkedFields.add("RemoteActive");
		NetworkedFields.add("RemoteSwitchModi");
		NetworkedFields.add("RemoteSwitchValue");
		NetworkedFields.add("RemoteSecurityStationlink");
		NetworkedFields.add("RemotehasPowersource");
		NetworkedFields.add("RemotePowerleft");
		NetworkedFields.add("RemoteGUIinRange");

		return NetworkedFields;
	}

	@Override
	public void invalidate()
	{
		FrequencyGrid.getWorldMap(this.worldObj).getControlSystem().remove(Integer.valueOf(getDeviceID()));
		super.invalidate();
	}

	@Override
	public void dropPlugins()
	{
		for (int a = 0; a < this.inventory.length; a++)
		{
			dropPlugins(a, this);
		}
	}

	@Override
	public void updateEntity()
	{
		if (!this.worldObj.isRemote)
		{
			if (getTicker() == 20)
			{
				if ((getLinkedSecurityStation() != null) && (!isActive()))
				{
					setActive(true);
				}
				if ((getLinkedSecurityStation() == null) && (isActive()))
				{
					setActive(false);
				}
				refreshRemoteData();

				setTicker((short) 0);
			}
			setTicker((short) (getTicker() + 1));
		}

		super.updateEntity();
	}

	public TileEntityMFFS getRemote()
	{
		return this.remote;
	}

	@Override
	public Container getContainer(InventoryPlayer inventoryplayer)
	{
		return new ContainerControlSystem(inventoryplayer.player, this);
	}

	@Override
	public TileEntitySecurityStation getLinkedSecurityStation()
	{
		return ItemCardSecurityLink.getLinkedSecurityStation(this, 0, this.worldObj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
		this.inventory = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);

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
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.inventory.length; i++)
		{
			if (this.inventory[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbttaglist);
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventory[i];
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
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
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventory[i] = itemstack;
		if ((itemstack != null) && (itemstack.stackSize > getInventoryStackLimit()))
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInvName()
	{
		return "ControlSystem";
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public int getSlotStackLimit(int slt)
	{
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack, int Slot)
	{
		switch (Slot)
		{
			case 0:
				if ((par1ItemStack.getItem() instanceof ItemCardSecurityLink))
				{
					return true;
				}

				break;
		}

		if ((par1ItemStack.getItem() instanceof ItemCardDataLink))
		{
			return true;
		}

		return false;
	}

	@Override
	public void onNetworkHandlerEvent(int key, String value)
	{
		if ((key == 103) && (this.remote != null) && (getRemoteGUIinRange()))
		{
			EntityPlayer player = this.worldObj.getPlayerEntityByName(value);
			if (player != null)
			{
				player.openGui(ModularForceFieldSystem.instance, 0, this.worldObj, this.remote.xCoord, this.remote.yCoord, this.remote.zCoord);
			}

		}

		if ((key == 102) && (this.remote != null))
		{
			this.remote.toggelSwitchValue();
		}

		if ((key == 101) && (this.remote != null))
		{
			this.remote.toogleSwitchModi();
		}

		super.onNetworkHandlerEvent(key, value);
	}

	private void refreshRemoteData()
	{
		refreshRemoteData(1);
	}

	private void refreshRemoteData(int slot)
	{
		this.remote = getTargetMaschine(slot);

		if (this.remote != null)
		{
			if ((!this.remote.isActive()) == getRemoteActive())
			{
				setRemoteActive(this.remote.isActive());
			}
			if (!this.remote.getDeviceName().equalsIgnoreCase(getRemoteDeviceName()))
			{
				setRemoteDeviceName(this.remote.getDeviceName());
			}
			if (this.remote.getSwitchModi() != getRemoteSwitchModi())
			{
				setRemoteSwitchModi(this.remote.getSwitchModi());
			}
			if ((!this.remote.getSwitchValue()) == getRemoteSwitchValue())
			{
				setRemoteSwitchValue(this.remote.getSwitchValue());
			}
			if (this.remote.getLinkedSecurityStation() == null)
			{
				setRemoteSecurityStationlink(false);
			}
			else
			{
				setRemoteSecurityStationlink(true);
			}

			if ((!this.remote.hasPowerSource()) == getRemotehasPowersource())
			{
				setRemotehasPowersource(this.remote.hasPowerSource());
			}
			if (this.remote.getPercentageCapacity() != getRemotePowerleft())
			{
				setRemotePowerleft(this.remote.getPercentageCapacity());
			}
			if (!MachineTypes.fromTE(this.remote).getName().equalsIgnoreCase(getRemoteDeviceTyp()))
			{
				setRemoteDeviceTyp(MachineTypes.fromTE(this.remote).getName());
			}

			if ((PointXYZ.distance(getMachinePoint(), this.remote.getMachinePoint()) > 61.0D) && (getRemoteGUIinRange()))
			{
				setRemoteGUIinRange(false);
			}

			if ((PointXYZ.distance(getMachinePoint(), this.remote.getMachinePoint()) <= 61.0D) && (!getRemoteGUIinRange()))
			{
				setRemoteGUIinRange(true);
			}

		}
		else
		{
			if (getRemoteActive())
			{
				setRemoteActive(false);
			}
			if (getRemoteSwitchModi() != 0)
			{
				setRemoteSwitchModi((short) 0);
			}
			if (!getRemoteDeviceName().equalsIgnoreCase("-"))
			{
				setRemoteDeviceName("-");
			}
			if (!getRemoteDeviceTyp().equalsIgnoreCase("-"))
			{
				setRemoteDeviceTyp("-");
			}
		}
	}

	private TileEntityMFFS getTargetMaschine(int slot)
	{
		if ((getStackInSlot(slot) != null) && ((getStackInSlot(slot).getItem() instanceof ItemCardDataLink)))
		{
			int DeviceID = 0;
			NBTTagCompound tag = NBTTagCompoundHelper.getTAGfromItemstack(getStackInSlot(slot));
			if (tag.hasKey("DeviceID"))
			{
				DeviceID = tag.getInteger("DeviceID");
			}
			if (DeviceID != 0)
			{
				TileEntityMFFS device = FrequencyGrid.getWorldMap(this.worldObj).getTileEntityMachines(ItemCardDataLink.getDeviceTyp(getStackInSlot(slot)), DeviceID);
				if (device != null)
				{
					return device;
				}
			}
			setInventorySlotContents(slot, new ItemStack(ModularForceFieldSystem.itemCardEmpty));
		}
		return null;
	}

	public boolean getRemoteGUIinRange()
	{
		return this.RemoteGUIinRange;
	}

	public void setRemoteGUIinRange(boolean b)
	{
		this.RemoteGUIinRange = b;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteGUIinRange");
	}

	public int getRemotePowerleft()
	{
		return this.RemotePowerleft;
	}

	public void setRemotePowerleft(int i)
	{
		this.RemotePowerleft = i;
		NetworkHandlerServer.updateTileEntityField(this, "RemotePowerleft");
	}

	public boolean getRemotehasPowersource()
	{
		return this.RemotehasPowersource;
	}

	public void setRemotehasPowersource(boolean b)
	{
		this.RemotehasPowersource = b;
		NetworkHandlerServer.updateTileEntityField(this, "RemotehasPowersource");
	}

	public boolean getRemoteSecurityStationlink()
	{
		return this.RemoteSecurityStationlink;
	}

	public void setRemoteSecurityStationlink(boolean b)
	{
		this.RemoteSecurityStationlink = b;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteSecurityStationlink");
	}

	public boolean getRemoteSwitchValue()
	{
		return this.RemoteSwitchValue;
	}

	public void setRemoteSwitchValue(boolean b)
	{
		this.RemoteSwitchValue = b;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteSwitchValue");
	}

	public short getRemoteSwitchModi()
	{
		return this.RemoteSwitchModi;
	}

	public void setRemoteSwitchModi(short s)
	{
		this.RemoteSwitchModi = s;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteSwitchModi");
	}

	public boolean getRemoteActive()
	{
		return this.RemoteActive;
	}

	public void setRemoteActive(boolean b)
	{
		this.RemoteActive = b;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteActive");
	}

	public String getRemoteDeviceTyp()
	{
		return this.RemoteDeviceTyp;
	}

	public void setRemoteDeviceTyp(String s)
	{
		this.RemoteDeviceTyp = s;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteDeviceTyp");
	}

	public String getRemoteDeviceName()
	{
		return this.RemoteDeviceName;
	}

	public void setRemoteDeviceName(String s)
	{
		this.RemoteDeviceName = s;
		NetworkHandlerServer.updateTileEntityField(this, "RemoteDeviceName");
	}
}