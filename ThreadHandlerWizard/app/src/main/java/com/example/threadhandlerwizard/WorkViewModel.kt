package com.example.threadhandlerwizard

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val handlerThread = HandlerThread("VM-Work").apply { start() }
    private val worker = Handler(handlerThread.looper)

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Idle")
    val status: LiveData<String> = _status

    @Volatile
    private var running = false

    private var mediaPlayer: MediaPlayer? = null

    private fun startMusic() {
        stopMusic()
        mediaPlayer = MediaPlayer.create(getApplication(), R.raw.bgm)?.apply {
            isLooping = true
            start()
        }
    }

    private fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    fun start() {
        if (running) return
        running = true
        _status.postValue("準備中…")
        _progress.postValue(0)
        worker.post {
            try {
                // 準備階段（約 3 秒，可依需求調整）
                for (step in 1..30) {
                    if (!running) break
                    Thread.sleep(100)
                }
                if (!running) {
                    _status.postValue("Canceled")
                    return@post
                }

                // 開始真正背景工作並播放背景音樂
                _status.postValue("工作中…")
                startMusic()

                for (i in 1..100) {
                    if (!running) break
                    Thread.sleep(200) // 模擬背景工作進度（每 0.2 秒 +1%，總計約 20 秒跑完；老師原範例為 3500ms）
                    _progress.postValue(i)
                }

                stopMusic()
                if (running) {
                    _status.postValue("背景工作結束")
                } else {
                    _status.postValue("Canceled")
                }
                running = false
            } catch (_: InterruptedException) {
                stopMusic()
                _status.postValue("Canceled")
                running = false
            }
        }
    }

    fun cancel() {
        running = false
        stopMusic()
        _status.postValue("Canceled")
    }

    override fun onCleared() {
        running = false
        stopMusic()
        handlerThread.quitSafely()
        super.onCleared()
    }
}
