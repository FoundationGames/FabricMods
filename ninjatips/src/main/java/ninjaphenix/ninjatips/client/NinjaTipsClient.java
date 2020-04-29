package ninjaphenix.ninjatips.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ItemTooltipCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.List;

import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static ninjaphenix.ninjatips.NinjaTips.ITEM_CHAT;

public class NinjaTipsClient implements ClientModInitializer
{
    public static void modifyToolTip(@NonNull final ItemStack stack, @NonNull final List<Text> tooltip)
    {
        if (stack.hasTag())
        {
            final CompoundTag tag = stack.getTag();
            if (Screen.hasControlDown())
            {
                tooltip.add(new TranslatableText(getTranslationId("nbt"), "").formatted(GRAY));
                @SuppressWarnings("ConstantConditions") final String[] lines = tag.toText().asFormattedString().split("\\n");
                for (String line : lines) { tooltip.add(new LiteralText(line)); }
            }
            else
            {
                final String hold = getTranslationId(MinecraftClient.IS_SYSTEM_MAC ? "hold_cmd" : "hold_ctrl");
                tooltip.add(new TranslatableText(getTranslationId("nbt"), new TranslatableText(hold).formatted(DARK_GRAY)).formatted(GRAY));
            }
        }


        final Collection<Identifier> tags = ItemTags.getContainer().getTagsFor(stack.getItem());
        if (tags.size() == 0) { return; }
        if (Screen.hasAltDown())
        {
            tooltip.add(new TranslatableText(getTranslationId("data"), "").formatted(GRAY));
            tags.forEach((identifier) -> tooltip.add(new LiteralText(" #" + identifier.toString()).formatted(DARK_GRAY)));
        }
        else
        {
            tooltip.add(new TranslatableText(getTranslationId("data"),
                    new TranslatableText(getTranslationId("hold_alt")).formatted(DARK_GRAY)).formatted(GRAY));
        }
    }

    @NonNull
    private static String getTranslationId(@NonNull String string) { return "ninjatips.text." + string; }

    public static void chatItem(ItemStack stack)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeItemStack(stack);
        ClientSidePacketRegistry.INSTANCE.sendToServer(ITEM_CHAT, buf);
    }

    @Override
    public void onInitializeClient()
    {
        ItemTooltipCallback.EVENT.register((stack, context, list) ->
        {
            list.removeIf(text -> text instanceof TranslatableText && ((TranslatableText) text).getKey().equals("item.nbt_tags"));
            modifyToolTip(stack, list);
        });
    }
}
