package omtteam.openmodularturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.blocks.BlockAbstractTileEntity;
import omtteam.omlib.util.IHasItemBlock;
import omtteam.omlib.util.MathUtil;
import omtteam.openmodularturrets.OpenModularTurrets;
import omtteam.openmodularturrets.handler.ConfigHandler;
import omtteam.openmodularturrets.init.ModBlocks;
import omtteam.openmodularturrets.items.blocks.ItemBlockExpander;
import omtteam.openmodularturrets.reference.Names;
import omtteam.openmodularturrets.reference.Reference;
import omtteam.openmodularturrets.tileentity.Expander;
import omtteam.openmodularturrets.tileentity.TurretBase;

import javax.annotation.Nullable;
import java.util.List;

import static omtteam.omlib.util.WorldUtil.getTouchingTileEntities;

/**
 * Created by Keridos on 19/07/16.
 * This Class
 */
public class BlockExpander extends BlockAbstractTileEntity implements IHasItemBlock {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 9);
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockExpander() {
        super(Material.GLASS);
        this.setCreativeTab(OpenModularTurrets.modularTurretsTab);
        if (!ConfigHandler.turretBreakable) {
            this.setBlockUnbreakable();
        }
        this.setResistance(3.0F);
        this.setHardness(3.0F);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
        this.setRegistryName(Reference.MOD_ID, Names.Blocks.expander);
    }

    @Override
    public ItemBlock getItemBlock(Block block) {
        return new ItemBlockExpander(block);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META, FACING);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Expander te = ((Expander) worldIn.getTileEntity(pos));
        if (te != null) {
            return state.withProperty(FACING, te.getOrientation());
        } else return state.withProperty(FACING, EnumFacing.NORTH);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(META) < 5) {
            return new Expander(state.getValue(META), false);
        } else {
            return new Expander(state.getValue(META), true);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState blockState = this.getActualState(state, source, pos);
        return MathUtil.rotateAABB(new AxisAlignedBB(1 / 8F, 1 / 8F, 0F, 7 / 8F, 7 / 8F, 3 / 8F), blockState.getValue(FACING).getOpposite());
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand.equals(EnumHand.OFF_HAND)) return true;
        Expander expander = (Expander) world.getTileEntity(pos);
        if (expander == null) {
            return true;
        }
        TurretBase base = expander.getBase();
        if (base != null && base.getTrustedPlayer(player.getUniqueID()) != null) {
            if (base.getTrustedPlayer(player.getUniqueID()).canOpenGUI && state.getValue(META) < 4) {
                player.openGui(OpenModularTurrets.instance, 7, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        if (base != null && player.getUniqueID().toString().equals(base.getOwner())) {
            if (player.isSneaking() && player.getHeldItemMainhand() == null) {
                world.destroyBlock(pos, true);
            } else if (state.getValue(META) < 4) {
                player.openGui(OpenModularTurrets.instance, 7, world, pos.getX(), pos.getY(), pos.getZ());
            } else {
                return true;
            }
        } else {
            player.addChatMessage(new TextComponentString(I18n.translateToLocal("status.ownership")));
        }
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (TileEntity tileEntity : getTouchingTileEntities(worldIn, pos)) {
            if (tileEntity instanceof TurretBase) return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            dropItems(worldIn, pos);
            super.breakBlock(worldIn, pos, state);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        Expander expander = (Expander) worldIn.getTileEntity(pos);
        if (expander != null) {
            expander.setOwnerName(expander.getBase().getOwnerName());
            expander.setOwner(expander.getBase().getOwner());
            expander.setSide();
        }
    }


    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List subItems) {
        for (int i = 0; i < 10; i++) {
            subItems.add(new ItemStack(ModBlocks.expander, 1, i));
        }
    }
}
