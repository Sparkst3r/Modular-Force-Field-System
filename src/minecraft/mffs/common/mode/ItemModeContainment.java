package mffs.common.mode;

import java.util.Set;

import mffs.api.IProjector;
import mffs.api.PointXYZ;
import mffs.common.module.ItemModule;
import mffs.common.module.ItemOptionAntibiotic;
import mffs.common.module.ItemOptionCamoflage;
import mffs.common.module.ItemOptionCutter;
import mffs.common.module.ItemOptionDefenseStation;
import mffs.common.module.ItemOptionFieldFusion;
import mffs.common.module.ItemOptionJammer;
import mffs.common.module.ItemOptionSponge;
import mffs.common.tileentity.TileEntityProjector;
import net.minecraft.item.Item;

public class ItemModeContainment extends ItemProjectorMode
{
	public ItemModeContainment(int i)
	{
		super(i, "moduleContainment");
		this.setIconIndex(54);
	}

	@Override
	public boolean supportsDistance()
	{
		return true;
	}

	@Override
	public boolean supportsStrength()
	{
		return true;
	}

	@Override
	public boolean supportsMatrix()
	{
		return true;
	}

	@Override
	public void calculateField(IProjector projector, Set ffLocs, Set ffInterior)
	{
		int tpx = 0;
		int tpy = 0;
		int tpz = 0;

		int xMout = projector.countItemsInSlot(IProjector.Slots.FocusLeft);
		int xPout = projector.countItemsInSlot(IProjector.Slots.FocusRight);
		int zMout = projector.countItemsInSlot(IProjector.Slots.FocusDown);
		int zPout = projector.countItemsInSlot(IProjector.Slots.FocusUp);
		int distance = projector.countItemsInSlot(IProjector.Slots.Distance);
		int Strength = projector.countItemsInSlot(IProjector.Slots.Strength) + 1;

		for (int y1 = 0; y1 <= Strength; y1++)
		{
			for (int x1 = 0 - xMout; x1 < xPout + 1; x1++)
			{
				for (int z1 = 0 - zPout; z1 < zMout + 1; z1++)
				{
					if (((TileEntityProjector) projector).getDirection().ordinal() == 0)
					{
						tpy = y1 - y1 - y1 - distance - 1;
						tpx = x1;
						tpz = z1;
					}

					if (((TileEntityProjector) projector).getDirection().ordinal() == 1)
					{
						tpy = y1 + distance + 1;
						tpx = x1;
						tpz = z1;
					}

					if (((TileEntityProjector) projector).getDirection().ordinal() == 2)
					{
						tpz = y1 - y1 - y1 - distance - 1;
						tpy = z1 - z1 - z1;
						tpx = x1 - x1 - x1;
					}

					if (((TileEntityProjector) projector).getDirection().ordinal() == 3)
					{
						tpz = y1 + distance + 1;
						tpy = z1 - z1 - z1;
						tpx = x1;
					}

					if (((TileEntityProjector) projector).getDirection().ordinal() == 4)
					{
						tpx = y1 - y1 - y1 - distance - 1;
						tpy = z1 - z1 - z1;
						tpz = x1;
					}
					if (((TileEntityProjector) projector).getDirection().ordinal() == 5)
					{
						tpx = y1 + distance + 1;
						tpy = z1 - z1 - z1;
						tpz = x1 - x1 - x1;
					}

					if ((y1 == 0) || (y1 == Strength) || (x1 == 0 - xMout) || (x1 == xPout) || (z1 == 0 - zPout) || (z1 == zMout))
					{
						ffLocs.add(new PointXYZ(tpx, tpy, tpz, 0));
					}
					else
					{
						ffInterior.add(new PointXYZ(tpx, tpy, tpz, 0));
					}
				}
			}
		}
	}

	public static boolean supportsOption(ItemModule item)
	{
		if ((item instanceof ItemOptionCamoflage))
		{
			return true;
		}
		if ((item instanceof ItemOptionDefenseStation))
		{
			return true;
		}
		if ((item instanceof ItemOptionFieldFusion))
		{
			return true;
		}
		if ((item instanceof ItemOptionJammer))
		{
			return true;
		}
		if ((item instanceof ItemOptionAntibiotic))
		{
			return true;
		}
		if ((item instanceof ItemOptionSponge))
		{
			return true;
		}
		if ((item instanceof ItemOptionCutter))
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean supportsOption(Item item)
	{
		if ((item instanceof ItemOptionCamoflage))
		{
			return true;
		}
		if ((item instanceof ItemOptionDefenseStation))
		{
			return true;
		}
		if ((item instanceof ItemOptionFieldFusion))
		{
			return true;
		}
		if ((item instanceof ItemOptionJammer))
		{
			return true;
		}
		if ((item instanceof ItemOptionAntibiotic))
		{
			return true;
		}
		if ((item instanceof ItemOptionSponge))
		{
			return true;
		}
		if ((item instanceof ItemOptionCutter))
		{
			return true;
		}

		return false;
	}
}