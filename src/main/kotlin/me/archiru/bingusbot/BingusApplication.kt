package me.archiru.bingusbot

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("start-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 290.0, 500.0)
        stage.isResizable = false
        stage.title = "bingusBot startup"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}