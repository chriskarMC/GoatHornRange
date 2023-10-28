package dev.chriskar.ghrange.mixin;

import dev.chriskar.ghrange.GoatHornRangeMod;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.Instruments;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(GoatHornItem.class)
public class GoatHornItemMixin {
    @Unique
    private static final GameRules.Key<GameRules.IntRule> GOAT_HORN_RANGE = GameRuleRegistry.register("goatHornRange", GameRules.Category.MISC, GameRuleFactory.createIntRule(Instruments.GOAT_HORN_RANGE));
    @Unique
    private static final Random random = Random.create();

    @Redirect(method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/GoatHornItem;playSound(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/Instrument;)V"))
    private void playSound(World world, PlayerEntity player, Instrument instrument) {
        float volume = world.getGameRules().getInt(GOAT_HORN_RANGE) / 16.0f;

        try {
            PlayerLookup.all(Objects.requireNonNull(world.getServer())).stream()
                    .filter(p -> p.getWorld().getRegistryKey() == player.getWorld().getRegistryKey())
                    .filter(p -> p.getPos().distanceTo(player.getPos()) <= world.getGameRules().getInt(GOAT_HORN_RANGE))
                    .forEach(p -> {
                        Vec3d directionToPlayer = player.getPos().subtract(p.getPos()).normalize().multiply(50);
                        Vec3d soundPosition = new Vec3d(p.getX() + directionToPlayer.x, player.getY(), p.getZ() + directionToPlayer.z);

                        p.networkHandler.sendPacket(new PlaySoundS2CPacket(instrument.soundEvent(), SoundCategory.RECORDS, soundPosition.getX(), soundPosition.getY(), soundPosition.getZ(), volume, 1, random.nextLong()));
                    });
        } catch (NullPointerException ignored) {
            world.playSoundFromEntity(player, player, instrument.soundEvent().value(), SoundCategory.RECORDS, volume, 1.0f);
            GoatHornRangeMod.LOGGER.warn("PlayerLookup failed, reverting to default behaviour");
        }
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}