package mffs.common.tileentity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mffs.api.IDefenseStation;
import mffs.api.IDefenseStationModule;
import mffs.api.SecurityPermission;
import mffs.common.ZhuYao;
import mffs.common.module.IModule;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

public class TFangYu extends TileEntityFortron implements IDefenseStation
{
	public enum ActionMode
	{
		WARN, CONFISCATE, ASSASINATE, ANTI_HOSTILE, ANTI_FRIENDLY, ANTIBIOTIC;

		public ActionMode toggle()
		{
			int newOrdinal = this.ordinal() + 1;

			if (newOrdinal >= this.values().length)
			{
				newOrdinal = 0;
			}
			return this.values()[newOrdinal];
		}

	}

	private static final int BAN_LIST_START = 4;

	/**
	 * True if the current confiscation mode is for "banning selected items".
	 */
	private boolean isBanMode = true;

	public TFangYu()
	{
		this.fortronTank.setCapacity(20 * LiquidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.isPoweredByRedstone())
			{
				if (!this.isActive())
				{
					this.setActive(true);
				}
			}
			else
			{
				if (this.isActive())
				{
					this.setActive(false);
				}
			}

			if (this.isActive() || (this.getStackInSlot(0) != null && this.getStackInSlot(0).itemID == ZhuYao.itemCardInfinite.itemID))
			{
				if (this.ticks % 10 == 0)
				{
					if (this.requestFortron(this.getFortronCost(), true) > 0)
					{
						this.scan();
					}
				}
			}

			if (this.playersUsing > 0)
			{
				PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
			}
		}
	}

	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(this.isBanMode);
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream)
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == 1)
		{
			this.isBanMode = dataStream.readBoolean();
		}
		else if (packetID == 4)
		{
			this.isBanMode = !this.isBanMode;
		}
	}

	public boolean isBanMode()
	{
		return this.isBanMode;
	}

	@Override
	public int getActionRange()
	{
		if ((getStackInSlot(3) != null) && (getStackInSlot(3).getItem() == ZhuYao.itemModuleScale))
		{
			return getStackInSlot(3).stackSize;
		}

		return 0;
	}

	@Override
	public int getWarningRange()
	{
		if ((getStackInSlot(2) != null) && (getStackInSlot(2).getItem() == ZhuYao.itemModuleScale))
		{
			return this.getActionRange() + (getStackInSlot(2).stackSize + 3);
		}

		return this.getActionRange() + 3;
	}

	public void scan()
	{
		try
		{
			TAnQuan securityStation = this.getLinkedSecurityStation();

			int xmininfo = this.xCoord - getWarningRange();
			int xmaxinfo = this.xCoord + getWarningRange() + 1;
			int ymininfo = this.yCoord - getWarningRange();
			int ymaxinfo = this.yCoord + getWarningRange() + 1;
			int zmininfo = this.zCoord - getWarningRange();
			int zmaxinfo = this.zCoord + getWarningRange() + 1;

			int xminaction = this.xCoord - getActionRange();
			int xmaxaction = this.xCoord + getActionRange() + 1;
			int yminaction = this.yCoord - getActionRange();
			int ymaxaction = this.yCoord + getActionRange() + 1;
			int zminaction = this.zCoord - getActionRange();
			int zmaxaction = this.zCoord + getActionRange() + 1;

			List<EntityLiving> infoLivinglist = this.worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(xmininfo, ymininfo, zmininfo, xmaxinfo, ymaxinfo, zmaxinfo));
			List<EntityLiving> actionLivinglist = this.worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(xminaction, yminaction, zminaction, xmaxaction, ymaxaction, zmaxaction));

			Set<EntityLiving> warnList = new HashSet<EntityLiving>();
			Set<EntityLiving> actionList = new HashSet<EntityLiving>();

			for (EntityLiving entityLiving : actionLivinglist)
			{
				double distance = Vector3.distance(new Vector3(this), new Vector3(entityLiving));
				if (distance <= getActionRange())
				{
					if (entityLiving instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer) entityLiving;
						actionList.add(entityLiving);
					}
				}
			}

			for (EntityLiving entityLiving : infoLivinglist)
			{
				if (entityLiving instanceof EntityPlayer && !actionList.contains(entityLiving))
				{
					EntityPlayer player = (EntityPlayer) entityLiving;
					double distance = Vector3.distance(new Vector3(this), new Vector3(entityLiving));

					if (distance <= getWarningRange())
					{
						if (!warnList.contains(player))
						{
							warnList.add(player);

							boolean isGranted = false;

							if (securityStation != null && securityStation.isAccessGranted(player.username, SecurityPermission.DEFENSE_STATION_STAY))
							{
								isGranted = true;
								// TODO: CHECK MFFS NOTIFICATION SETTING < MODE 3
							}

							if (!isGranted)
							{
								player.addChatMessage("[" + this.getInvName() + "] Warning! You are in scanning range!");
								player.attackEntityFrom(ZhuYao.areaDefense, 1);
							}
						}
					}
				}
			}

			if (this.worldObj.rand.nextInt(5) == 0)
			{
				Iterator<EntityLiving> it = actionList.iterator();

				while (it.hasNext())
				{
					doDefense(it.next());
				}
			}

		}
		catch (Exception e)
		{
			ZhuYao.LOGGER.severe("Defense Station has an error!");
			e.printStackTrace();
		}
	}

	public void doDefense(EntityLiving entityLiving)
	{
		boolean hasPermission = false;

		/**
		 * Check for security permission to see if this player should be ignored.
		 */
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;

			TAnQuan securityStation = getLinkedSecurityStation();

			if (securityStation != null && securityStation.isAccessGranted(player.username, SecurityPermission.DEFENSE_STATION_STAY))
			{
				hasPermission = true;
			}
		}

		for (ItemStack itemStack : this.getModuleStacks())
		{
			IDefenseStationModule module = (IDefenseStationModule) itemStack.getItem();

			if (module.onDefend(this, entityLiving) || entityLiving.isDead)
			{
				break;
			}
		}
	}

	@Override
	public boolean mergeIntoInventory(ItemStack itemStack)
	{
		for (int dir = 0; dir < 5; dir++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(dir);
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), direction);

			if (tileEntity instanceof IInventory)
			{
				IInventory inventory = (IInventory) tileEntity;

				for (int i = 0; i < inventory.getSizeInventory(); i++)
				{
					ItemStack checkStack = inventory.getStackInSlot(i);

					if (checkStack == null)
					{
						inventory.setInventorySlotContents(i, itemStack);
						return true;
					}
					else if (checkStack.isItemEqual(itemStack))
					{
						int freeSpace = checkStack.getMaxStackSize() - checkStack.stackSize;

						checkStack.stackSize += Math.min(itemStack.stackSize, freeSpace);
						itemStack.stackSize -= freeSpace;

						if (itemStack.stackSize <= 0)
						{
							itemStack = null;
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public int getSizeInventory()
	{
		return 2 + 9 * 3;
	}

	@Override
	public int getFortronCost()
	{
		float cost = 2;

		for (ItemStack itemStack : this.getModuleStacks())
		{
			if (itemStack != null)
			{
				cost += itemStack.stackSize * ((IModule) itemStack.getItem()).getFortronCost();
			}
		}

		return Math.round(cost);
	}

	@Override
	public Set<ItemStack> getFilteredItems()
	{
		Set<ItemStack> stacks = new HashSet<ItemStack>();

		for (int i = BAN_LIST_START; i < this.getSizeInventory() - 1; i++)
		{
			if (this.getStackInSlot(i) != null)
			{
				stacks.add(this.getStackInSlot(i));
			}
		}
		return stacks;
	}

	@Override
	public boolean getFilterMode()
	{
		return this.isBanMode;
	}

	@Override
	public ItemStack getModule(IModule module)
	{
		ItemStack returnStack = new ItemStack((Item) module, 0);

		for (ItemStack comparedModule : getModuleStacks())
		{
			if (comparedModule.getItem() == module)
			{
				returnStack.stackSize += comparedModule.stackSize;
			}
		}

		return returnStack;
	}

	@Override
	public int getModuleCount(IModule module, int... slots)
	{
		int count = 0;

		if (slots != null && slots.length > 0)
		{
			for (int slotID : slots)
			{
				if (this.getStackInSlot(slotID) != null)
				{
					if (this.getStackInSlot(slotID).getItem() == module)
					{
						count += this.getStackInSlot(slotID).stackSize;
					}
				}
			}
		}
		else
		{
			for (ItemStack itemStack : getModuleStacks())
			{
				if (itemStack.getItem() == module)
				{
					count += itemStack.stackSize;
				}
			}
		}

		return count;
	}

	@Override
	public Set<ItemStack> getModuleStacks()
	{
		Set<ItemStack> modules = new HashSet<ItemStack>();

		for (int slotID = 2; slotID <= this.getSizeInventory() - 1; slotID++)
		{
			ItemStack itemStack = this.getStackInSlot(slotID);

			if (itemStack != null)
			{
				if (itemStack.getItem() instanceof IDefenseStationModule)
				{
					modules.add(itemStack);
				}
			}
		}

		return modules;
	}

	@Override
	public Set<IModule> getModules()
	{
		Set<IModule> modules = new HashSet<IModule>();

		for (int slotID = 2; slotID < this.getSizeInventory() - 1; slotID++)
		{
			ItemStack itemStack = this.getStackInSlot(slotID);

			if (itemStack != null)
			{
				if (itemStack.getItem() instanceof IModule)
				{
					modules.add((IModule) itemStack.getItem());
				}
			}
		}

		return modules;
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID >= BAN_LIST_START)
		{
			return true;
		}

		return itemStack.getItem() instanceof IDefenseStationModule;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.isBanMode = nbt.getBoolean("isBanMode");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isBanMode", this.isBanMode);
	}

}