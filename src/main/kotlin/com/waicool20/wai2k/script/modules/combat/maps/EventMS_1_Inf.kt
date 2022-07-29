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
import com.waicool20.wai2k.util.readText
import com.waicool20.waicoolutils.logging.loggerFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.roundToLong
import kotlin.random.Random

class EventMS_1_Inf(scriptComponent: ScriptComponent) :
    HomographyMapRunner(scriptComponent),
    EventMapRunner {
    private val logger = loggerFor<EventMS_1_Inf>()

    override suspend fun enterMap() {
        if (gameState.requiresMapInit) {
            // Exit chapter arrow for if we start in a different chapter
            delay(500)
            region.subRegion(50, 455, 100, 100).click()
            delay(2500)

            logger.info("Zoom out")
            region.pinch(
                Random.nextInt(900, 1000),
                Random.nextInt(300, 400),
                15.0,
                500
            )
            delay(500)

            logger.info("Pan down")
            var r = region.subRegion(600, 950, 100, 100)
            repeat(2) {
                r.swipeTo(r.copy(y = r.y - 800), duration = 500)
                delay(300)
            }

            logger.info("Entering Chapter 1")
            region.subRegion(1100, 640, 100, 100).click()
            delay(2500)

            logger.info("Panning right through map list")
            r = region.subRegion(2000, 250, 50, 50)
            repeat(2) {
                r.swipeTo(r.copy(x = r.x - 1400), duration = 500)
                delay(300)
            }

            val difficultyRegion = region.subRegion(230, 925, 79, 24)
            val difficulty =
                ocr.readText((difficultyRegion), threshold = 0.4, pad = 0, trim = false)
            logger.info("Current difficulty: $difficulty")
            if (!difficulty.lowercase()
                    .replace(" ", "")
                    .contains("normal")
            ) {
                logger.info("Changing difficulty")
                difficultyRegion.click()
            }
        }

        // Click on map pin
        region.subRegion(1000, 455, 99, 30).click()
        delay((900 * gameState.delayCoefficient).roundToLong())

        // Enter
        region.subRegion(1832, 590, 232, 110).click()
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

            logger.info("Pan up")
            val r = region.subRegion(400, 885, 50, 50)
            r.swipeTo(r.copy(y = r.y + 300))
            delay((500 * gameState.delayCoefficient).roundToLong())
            gameState.requiresMapInit = false
        }
        val rEchelons = deployEchelons(nodes[0])
        mapRunnerRegions.startOperation.click(); yield()
        waitForGNKSplash()
        resupplyEchelons(rEchelons)

        enterPlanningMode()

        if (rEchelons.isEmpty()) {
            logger.info("Selecting echelon at ${nodes[0]}")
            nodes[0].findRegion().click()
        }

        logger.info("Selecting echelon at ${nodes[1]}")
        nodes[1].findRegion().click()

        logger.info("Executing plan")
        mapRunnerRegions.executePlan.click()

        waitForTurnEnd(5)
        handleBattleResults()
    }
}