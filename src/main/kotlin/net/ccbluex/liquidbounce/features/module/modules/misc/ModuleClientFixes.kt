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
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * A module which applies fixes to the client that might cause issues on some servers or anti cheats.
 */
object ModuleClientFixes : Module("ClientFixes", Category.MISC) {

    /**
     * A fix for old anti-cheats which check the packet order by 1.8 standards, while we are at 1.20+.
     *
     * interactItem() will always send an additional PlayerMoveC2SPacket, which will cause many anti-cheats to flag you.
     * This fix will remove the PlayerMoveC2SPacket, which will cause the anti-cheat
     * to think that the packet order is correct.
     * But it might cause the interact item use to be off by one tick, as the rotation was not updated in the same tick.
     *
     * Of course this is something to be fixed by the anti-cheat, but we cannot rely on that and have to fix it
     * ourselves.
     */
    val fixInteractRotationUse by boolean("RmInteractItemMoveC2S", false)

}
