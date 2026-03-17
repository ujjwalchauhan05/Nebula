package com.example.nebula944422944422
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.*
import android.content.*
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
class MainActivity : AppCompatActivity() {
    private val songs = listOf(
      R.raw.phir_bhi_tum_ko, R.raw.filhall, R.raw.aashiq_hoon,R.raw.song1, R.raw.kinna_sona,R.raw.lo_maan_liya,R.raw.milne_hai_mujhse_ayi,
        R.raw.piya_aye,R.raw.song2, R.raw.song3,R.raw.sanam_teri_kasam,R.raw.bhuladenamujhe,R.raw.bewajah,
        R.raw.teri_aadat2,R.raw.banjaara,R.raw.humnava_mere,R.raw.bol_do,R.raw.galliya,
        R.raw.thoda_thoda_pyaar,R.raw.hone_laga_tumse,R.raw.tera_chehra,R.raw.tereliye,
        R.raw.thodi_zagahe,R.raw.tum_hi_ana,R.raw.ukha_hi_banana,R.raw.zaroorat,R.raw.galti,R.raw.deewaane,R.raw.deewaniyat,R.raw.tu_hi_haqeeqat,R.raw.hua_hain_aj_pehlibaar)
    private val songNames = listOf(
      "Fir bhi tum ko ","Filhall", "Ashiq hu", "Song 1","Kinna Sona","Lo Maan Liya","Milne Hai Mujhse Aayi","Piya Aye","Song 2","Song 3",
        "Sanam Teri Kasam","Bhula Dena Mujhe","Bewajah","Teri Aadat 2","Banjaara","Humnava Mere","Bol Do","Galliyan",
        "Thoda Thoda Pyaar","Hone Laga Tumse","Tera Chehra","Tere Liye","Thodi Jagah","Tum Hi Aana",
        "Ukha Hi Banana","Zaroorat","galti",
        "deewaane",
        "deewaniyat",
        "tu hi haqeeqat",
        "hua hain aj pehlibaar")
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pauseSong()
            }
        }
    }
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button
    private lateinit var seekBar: SeekBar
    private lateinit var txtSong: TextView
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val ACTION_PLAY_PAUSE = "PLAY_PAUSE"
        private const val ACTION_NEXT = "NEXT"
        private const val ACTION_PREV = "PREV"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 🔹 bind views
        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        seekBar = findViewById(R.id.seekBar)
        txtSong = findViewById(R.id.txtSong)
        prepareSong()
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (isPlaying) pauseSong() else playSong()
            }
            ACTION_NEXT -> playNext()
            ACTION_PREV -> playPrevious()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        // ▶ Play / Pause
        btnPlayPause.setOnClickListener {
            if (isPlaying) pauseSong() else playSong() }
        // ⏭ Next
        btnNext.setOnClickListener {
            playNext() }
        // ⏮ Previous
        btnPrev.setOnClickListener {
            playPrevious() }
        // 🎚 SeekBar drag
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress) }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}})
        // 🔹 Song list (RecyclerView)
        val recycler = findViewById<RecyclerView>(R.id.recyclerSongs)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = SongListAdapter(songNames) { index ->
            currentIndex = index
            prepareSong()
            playSong()} }
    // 🔹 prepare song
    private fun prepareSong() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, songs[currentIndex])
        seekBar.max = mediaPlayer!!.duration
        seekBar.progress = 0
        txtSong.text = songNames[currentIndex]
        mediaPlayer?.setOnCompletionListener {
            playNext() }
        btnPlayPause.text = "▶"
        isPlaying = false }
    // 🔹 play
    private fun playSong() {
        mediaPlayer?.start()
        isPlaying = true
        btnPlayPause.text = "⏸"
        showNotification()
        updateSeekBar() }
    // 🔹 pause
    private fun pauseSong() {
        mediaPlayer?.pause()
        isPlaying = false
        btnPlayPause.text = "▶"
        showNotification()
    }
    // 🔹 next
    private fun playNext() {
        currentIndex = (currentIndex + 1) % songs.size
        prepareSong()
        playSong()
        showNotification()}
    // 🔹 previous
    private fun playPrevious() {
        currentIndex =
            if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
        prepareSong()
        playSong()
        showNotification()}
    // 🔹 seekbar sync
    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer != null && isPlaying) {
                    seekBar.progress = mediaPlayer!!.currentPosition
                    handler.postDelayed(this, 500) } } }) }
    // 🔹 notification
    private fun showNotification() {

        val playPauseIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val nextIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_NEXT
        }
        val prevIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_PREV
        }

        val playPausePendingIntent = PendingIntent.getActivity(
            this, 0, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getActivity(
            this, 1, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getActivity(
            this, 2, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(songNames[currentIndex])
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_previous, "Prev", prevPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play,
                "PlayPause",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }
    override fun onDestroy() {
        mediaPlayer?.release()
        unregisterReceiver(noisyReceiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        NotificationManagerCompat.from(this).cancel(1)} }