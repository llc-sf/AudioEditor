package dev.audio.timeruler.imp

import dev.android.player.framework.utils.service.PlayerService
import dev.audio.timeruler.player.PlayerManager

class PlayerServiceImp : PlayerService {
    override fun stop() {
        PlayerManager.stop()
    }

    override fun clearProgress() {
        PlayerManager.clearProgress()
    }
}