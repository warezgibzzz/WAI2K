/*
 * GPLv3 License
 *
 *  Copyright (c) WAI2K by waicool20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.waicool20.wai2k.script.modules.combat.maps

import com.waicool20.wai2k.script.ScriptComponent
import com.waicool20.wai2k.script.modules.combat.EventMapRunner
import com.waicool20.wai2k.script.modules.combat.HomographyMapRunner
import com.waicool20.wai2k.util.loggerFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.roundToLong
import kotlin.random.Random

class EventPR_1_Inf_FARA83(scriptComponent: ScriptComponent) : EventPR_1_Inf(scriptComponent)
class EventPR_1_Inf_KSVK(scriptComponent: ScriptComponent) : EventPR_1_Inf(scriptComponent)
class EventPR_1_Inf_SPP1(scriptComponent: ScriptComponent) : EventPR_1_Inf(scriptComponent)

sealed class EventPR_1_Inf(scriptComponent: ScriptComponent) :
    HomographyMapRunner(scriptComponent),
    EventMapRunner {
    private val logger = loggerFor<EventPR_1_Inf>()

    override suspend fun enterMap() {
        if (gameState.requiresMapInit) {
            PRUtils.enterInfinity(this, PRUtils.Difficulty.NORMAL)
        }

        // Click map entry
        region.subRegion(724, 693, 197, 55).click()

        delay(500)

        // Confirm start
        region.subRegion(1832, 589, 232, 111).click()
    }

    override suspend fun begin() {
        if (gameState.requiresMapInit) {
            logger.info("Zoom out")
            region.pinch(
                Random.nextInt(900, 1000),
                Random.nextInt(300, 400),
                15.0,
                500
            )
            delay((900 * gameState.delayCoefficient).roundToLong()) //Wait to settle
            mapH = null
            gameState.requiresMapInit = false
        }
        deployEchelons(nodes[0])
        mapRunnerRegions.startOperation.click(); yield()
        waitForGNKSplash()
        resupplyEchelons(nodes[0])
        delay(500)
        enterPlanningMode()

        logger.info("Selecting ${nodes[1]}")
        nodes[1].findRegion().click()

        logger.info("Selecting ${nodes[2]}")
        nodes[2].findRegion().click()

        logger.info("Executing plan")
        mapRunnerRegions.executePlan.click()
        waitForTurnEnd(3)
        handleBattleResults()
    }
}
