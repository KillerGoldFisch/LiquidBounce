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
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.api.ClientUpdate.gitInfo
import net.ccbluex.liquidbounce.api.ClientUpdate.hasUpdate
import net.ccbluex.liquidbounce.api.IpInfoApi
import net.ccbluex.liquidbounce.base.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.base.ultralight.theme.ThemeManager
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.events.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.events.ClientStartEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.chat.Chat
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.cosmetic.CapeService
import net.ccbluex.liquidbounce.features.misc.AccountManager
import net.ccbluex.liquidbounce.features.misc.FriendManager
import net.ccbluex.liquidbounce.features.misc.ProxyManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.tabs.Tabs
import net.ccbluex.liquidbounce.render.Fonts
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.block.ChunkScanner
import net.ccbluex.liquidbounce.utils.block.WorldChangeNotifier
import net.ccbluex.liquidbounce.utils.client.IS_MAC
import net.ccbluex.liquidbounce.utils.client.SessionHandler
import net.ccbluex.liquidbounce.utils.combat.CombatManager
import net.ccbluex.liquidbounce.utils.combat.globalEnemyConfigurable
import net.ccbluex.liquidbounce.utils.item.InventoryTracker
import net.ccbluex.liquidbounce.utils.mappings.McMappings
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import org.apache.logging.log4j.LogManager
import org.lwjgl.util.tinyfd.TinyFileDialogs
import kotlin.system.exitProcess

/**
 * LiquidBounce
 *
 * A free mixin-based injection hacked-client for Minecraft using FabricMC.
 *
 * @author kawaiinekololis (@team CCBlueX)
 */
object LiquidBounce : Listenable {

    /**
     * CLIENT INFORMATION
     *
     * WARNING: Please read the GNU General Public License
     */
    const val CLIENT_NAME = "LiquidBounce"
    const val CLIENT_AUTHOR = "CCBlueX"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    val clientVersion = gitInfo["git.build.version"]?.toString() ?: "unknown"
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "nextgen"

    /**
     * Defines if the client is in development mode. This will enable update checking on commit time instead of semantic versioning.
     *
     * TODO: Replace this approach with full semantic versioning.
     */
    const val IN_DEVELOPMENT = true

    /**
     * Client logger to print out console messages
     */
    val logger = LogManager.getLogger(CLIENT_NAME)!!

    /**
     * Client update information
     */
    val updateAvailable by lazy { hasUpdate() }

    /**
     * Should be executed to start the client.
     */
    val startHandler = handler<ClientStartEvent> {
        runCatching {
            logger.info("Launching $CLIENT_NAME v$clientVersion by $CLIENT_AUTHOR")
            logger.debug("Loading from cloud: '$CLIENT_CLOUD'")

            // Restrict OS (to notify user that macOS is not supported)
            if (IS_MAC) {
                TinyFileDialogs.tinyfd_messageBox(
                    "LiquidBounce Nextgen",
                    "LiquidBounce Nextgen is not supported on macOS. Please use Windows or Linux instead.",
                    "ok",
                    "error",
                    true
                )
                logger.error("LiquidBounce Nextgen is not supported on macOS. Please use Windows or Linux instead.")
                exitProcess(1)
            }

            // Load mappings
            McMappings.load()

            // Initialize client features
            EventManager

            // Config
            ConfigSystem
            globalEnemyConfigurable

            ChunkScanner
            WorldChangeNotifier

            // Features
            ModuleManager
            CommandManager
            ThemeManager
            ScriptManager
            RotationManager
            CombatManager
            FriendManager
            ProxyManager
            AccountManager
            InventoryTracker
            WorldToScreen
            Tabs
            Chat
            SessionHandler

            // Loads up fonts (requires connection to the internet on first launch)
            Fonts

            // Load up a web platform
            UltralightEngine.init()

            // Register commands and modules
            CommandManager.registerInbuilt()
            ModuleManager.registerInbuilt()

            // Load user scripts
            ScriptManager.loadScripts()

            // Load config system from disk
            ConfigSystem.load()

            // Check for newest version
            if (updateAvailable) {
                logger.info("Update available! Please download the latest version from https://liquidbounce.net/")
            }

            // Refresh local IP info
            logger.info("Refreshing local IP info...")
            IpInfoApi.refreshLocalIpInfo()

            // Login into known token if not empty
            if (CapeService.knownToken.isNotBlank()) {
                runCatching {
                    CapeService.login(CapeService.knownToken)
                }.onFailure {
                    logger.error("Failed to login into known cape token.", it)
                }.onSuccess {
                    logger.info("Successfully logged in into known cape token.")
                }
            }

            // Refresh cape service
            CapeService.refreshCapeCarriers {
                logger.info("Successfully loaded ${CapeService.capeCarriers.size} cape carriers.")
            }

            // Connect to chat server
            Chat.connectAsync()
        }.onSuccess {
            logger.info("Successfully loaded client!")
        }.onFailure {
            logger.error("Unable to load client.", it)
            TinyFileDialogs.tinyfd_messageBox(
                "LiquidBounce Nextgen",
                "LiquidBounce Nextgen failed to launch.\n" +
                    "If the issue persists, report to the developers.\n" +
                    "${it.message}",
                "ok",
                "error",
                true
            )
            exitProcess(1)
        }
    }

    /**
     * Should be executed to stop the client.
     */
    val shutdownHandler = handler<ClientShutdownEvent> {
        logger.info("Shutting down client...")
        ConfigSystem.storeAll()
        UltralightEngine.shutdown()

        ChunkScanner.ChunkScannerThread.stopThread()
    }

}
