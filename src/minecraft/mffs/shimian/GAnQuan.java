package mffs.shimian;

import java.util.List;

import mffs.ZhuYao;
import mffs.api.SecurityPermission;
import mffs.jiqi.t.TAnQuan;
import mffs.rongqi.CAnQuan;
import mffs.shimian.enniu.GEnNiu;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GAnQuan extends GuiMFFS
{
	private TAnQuan tileEntity;

	public GAnQuan(EntityPlayer player, TAnQuan tileEntity)
	{
		super(new CAnQuan(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(109, 92);
		super.initGui();
		this.buttonList.clear();

		int x = 0;
		int y = 0;

		for (int i = 0; i < SecurityPermission.values().length; i++)
		{
			x++;
			this.buttonList.add(new GEnNiu(i, this.width / 2 - 50 + 20 * x, this.height / 2 - 75 + 20 * y, new Vector2(18, 18 * i), this, SecurityPermission.values()[i].name));

			if (i % 3 == 0 && i != 0)
			{
				x = 0;
				y++;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.ySize / 2 - this.tileEntity.getInvName().length() * 4, 6, 4210752);

		this.drawTextWithTooltip("rights", "%1", 85, 22, x, y, 0);

		try
		{
			if (this.tileEntity.getManipulatingCard() != null)
			{
				if (ZhuYao.itKaShenFen.getUsername(this.tileEntity.getManipulatingCard()) != null)
				{
					for (int i = 0; i < this.buttonList.size(); i++)
					{
						GEnNiu button = (GEnNiu) this.buttonList.get(i);
						button.drawButton = true;

						if (ZhuYao.itKaShenFen.hasPermission(this.tileEntity.getManipulatingCard(), SecurityPermission.values()[i]))
						{
							button.stuck = true;
						}
						else
						{
							button.stuck = false;
						}
					}
				}
			}
			else
			{
				for (GEnNiu button : ((List<GEnNiu>) this.buttonList))
				{
					button.drawButton = false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.textFieldFrequency.drawTextBox();

		this.drawTextWithTooltip("master", 28, 90 + (this.fontRenderer.FONT_HEIGHT / 2), x, y);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		this.drawSlot(87, 90);

		this.drawSlot(7, 34);
		this.drawSlot(7, 90);

		// Internal Inventory
		for (int var4 = 0; var4 < 9; var4++)
		{
			this.drawSlot(8 + var4 * 18 - 1, 110);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);
		PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, 3, guiButton.id));
	}
}