/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.injection.mixins.minecraft.client;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.KeyEvent;
import net.ccbluex.liquidbounce.event.events.KeyboardCharEvent;
import net.ccbluex.liquidbounce.event.events.KeyboardKeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    /**
     * Hook key event
     */
    @Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", shift = At.Shift.BEFORE, ordinal = 0))
    private void hookKeyboardKey(long window, int key, int scancode, int i, int j, CallbackInfo callback) {
        // does if (window == this.client.getWindow().getHandle())
        EventManager.INSTANCE.callEvent(new KeyboardKeyEvent(key, scancode, i, j));
    }

    /**
     * Hook char event
     */
    @Inject(method = "onChar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", shift = At.Shift.BEFORE))
    private void hookKeyboardChar(long window, int codePoint, int modifiers, CallbackInfo callback) {
        // does if (window == this.client.getWindow().getHandle())
        EventManager.INSTANCE.callEvent(new KeyboardCharEvent(codePoint, modifiers));
    }

    /**
     * Hook key event
     */
    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;", shift = At.Shift.AFTER))
    private void hookKey(long window, int key, int scancode, int i, int j, CallbackInfo callback) {
        // does if (window == this.client.getWindow().getHandle())
        EventManager.INSTANCE.callEvent(new KeyEvent(InputUtil.fromKeyCode(key, scancode), i, j));
    }

}
