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

package com.waicool20.wai2k.views.tabs.preferences

import com.waicool20.wai2k.config.Configurations
import com.waicool20.wai2k.views.ViewNode
import com.waicool20.waicoolutils.javafx.AlertFactory
import com.waicool20.waicoolutils.javafx.addListener
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import org.controlsfx.control.MasterDetailPane
import tornadofx.*

class PreferencesTabView : View() {
    override val root: HBox by fxml("/views/tabs/preferences/preferences-tab.fxml")
    private val preferencesTreeView: TreeView<String> by fxid()
    private val preferencesPane: MasterDetailPane by fxid()
    private val saveButton: Button by fxid()
    private val configs: Configurations by inject()

    private val defaultMasterNode = hbox(alignment = Pos.CENTER) {
        label("Choose something to configure on the left!")
    }

    init {
        title = "Preferences"
        preferencesPane.masterNode = defaultMasterNode
        saveButton.setOnAction {
            configs.wai2KConfig.save()
            AlertFactory.info(content = "Preferences saved!").showAndWait()
        }
    }

    override fun onDock() {
        super.onDock()
        initializeTree()
    }

    fun initializeTree() {
        preferencesTreeView.root = TreeItem<String>().apply {
            value = "Preferences"
            isExpanded = true
        }
        PreferencesViewMappings.list.forEach { node ->
            node.isExpanded = true
            if (node.parent == null) {
                preferencesTreeView.root.children.add(node)
            } else {
                PreferencesViewMappings.list.find { it.view == node.parent }?.children?.add(node)
            }
        }
        preferencesTreeView.focusModel.focusedItemProperty().addListener("PreferenceFocused") { newVal ->
            preferencesPane.apply {
                if (newVal is ViewNode) {
                    val pos = preferencesPane.dividerPosition
                    masterNode = find(newVal.view).root
                    dividerPosition = pos
                    isAnimated = true
                    isShowDetailNode = true
                } else {
                    masterNode = defaultMasterNode
                    isAnimated = false
                    isShowDetailNode = false
                }
            }
        }
    }
}