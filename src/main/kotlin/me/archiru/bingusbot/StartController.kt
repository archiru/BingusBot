package me.archiru.bingusbot

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StartController {

    @FXML
    lateinit var tokenField: PasswordField

    @FXML
    lateinit var serverIdField: TextField

    @FXML
    lateinit var channelIdField: TextField

    @FXML
    lateinit var userIdField: TextField

    @FXML
    lateinit var puroPuroCheckBox: CheckBox

    @FXML
    lateinit var magpieCheckBox: CheckBox

    @FXML
    lateinit var ninjaCheckBox: CheckBox

    @FXML
    lateinit var dragonCheckBox: CheckBox

    @FXML
    lateinit var crystalCheckBox: CheckBox

    @FXML
    lateinit var luckyCheckBox: CheckBox

    private var bingusBot: BingusBot = BingusBot()

    fun startBotOnAction(actionEvent: ActionEvent) {
        GlobalScope.launch { bingusBot.startBot(
            tokenField.text,
            serverIdField.text,
            channelIdField.text,
            puroPuroCheckBox.isSelected,
            userIdField.text,
            magpieCheckBox.isSelected,
            ninjaCheckBox.isSelected,
            dragonCheckBox.isSelected,
            crystalCheckBox.isSelected,
            luckyCheckBox.isSelected)
        }
    }
}