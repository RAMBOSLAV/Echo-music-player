package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.DropBoxManager
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.echo.CurrentSongHelper
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.Random as Random1
import kotlin.random.Random as Random2
import kotlin.random.Random as Random3


@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SongPlayingFragment : Fragment() {

    object stat{

        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var shuffleImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var playPauseImageButton: ImageButton? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null


        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var MY_PREFS_NAME = "ShakeFeature"

        var fab: ImageButton? = null

        var favoriteContent: EchoDatabase? = null

        var mSensorManager: SensorManager? = null
        var mSensorListner: SensorEventListener? = null

        var updateSongeTime = object : Runnable{
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong() )
                            - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong() ))))
                Handler().postDelayed(this, 1000)
            }

        }

    }

    object staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun playNext(check: String){

            if(check.equals("PlayNextNormal", true)){
                stat.currentPosition = stat.currentPosition + 1
            }else if(check.equals("PlayNextLikeNormalShuffle", true)){
                var randomObject: Random = Random
                var randomPosition = randomObject.nextInt(stat.fetchSongs?.size?.plus(1) as Int)
                stat.currentPosition = randomPosition
            }
            if (stat.currentPosition == stat.fetchSongs?.size){
                stat.currentPosition = 0
            }
            stat.currentSongHelper?.isLoop = false
            var nextSong = stat.fetchSongs?.get(stat.currentPosition)
            stat.currentSongHelper?.songTitle = nextSong?.songTitle
            stat.currentSongHelper?.songPath = nextSong?.songData
            stat.currentSongHelper?.currentPosition = stat.currentPosition
            stat.currentSongHelper?.songId = nextSong?.songId as Long

            updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

            stat.mediaPlayer?.reset()

            try{
                stat.mediaPlayer?.setDataSource( stat.myActivity, Uri.parse(stat.currentSongHelper?.songPath))
                stat.mediaPlayer?.prepare()
                stat.mediaPlayer?.start()
                processInformation(stat.mediaPlayer as MediaPlayer)

            }catch (e: Exception){
                e.printStackTrace()
            }

            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_on))
            }else{
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_off))
            }
        }

        fun onSongComplete(){

            if(stat.currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
                stat.currentSongHelper?.isPlaying = true
            }else{
                if (stat.currentSongHelper?.isLoop as Boolean){

                    stat.currentSongHelper?.isPlaying = true
                    var nextSong = stat.fetchSongs?.get(stat.currentPosition)
                    stat.currentSongHelper?.songTitle = nextSong?.songTitle
                    stat.currentSongHelper?.songPath = nextSong?.songData
                    stat.currentSongHelper?.currentPosition = stat.currentPosition
                    stat.currentSongHelper?.songId = nextSong?.songId as Long

                    updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

                    stat.mediaPlayer?.reset()

                    try{
                        stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(stat.currentSongHelper?.songPath))
                        stat.mediaPlayer?.prepare()
                        stat.mediaPlayer?.start()
                        processInformation(stat.mediaPlayer as MediaPlayer)

                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }else{
                    playNext("PlayNextNormal")
                    stat.currentSongHelper?.isPlaying = true
                }
            }
            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_on))
            }else{
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_off))
            }
        }

        fun updateTextView(songTitle: String, songArtist: String){
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if(songTitle.equals("<unknown>", true)){
                songTitleUpdated = "Unknown"
            }
            if(songArtist.equals("<unknown>", true)){
                songArtistUpdated = "Unknown"
            }

            stat.songTitleView?.setText(songTitleUpdated)
            stat.songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {

            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            stat.seekBar?.max = finalTime

            stat.startTimeText?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong())
                        - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))))

            stat.endTimeText?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong())
                        - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))))

            stat.seekBar?.setProgress(startTime)
            Handler().postDelayed(stat.updateSongeTime, 1000)
        }

    }

    var mAcc: Float = 0f
    var mAccCurrent: Float = 0f
    var mAccLast: Float = 0f





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        activity?.title = "Now Playing"
        stat.favoriteContent = EchoDatabase(stat.myActivity)
        setHasOptionsMenu(true)
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        stat.seekBar = view?.findViewById(R.id.seekBar)
        stat.startTimeText =  view?.findViewById(R.id.startTime)
        stat.endTimeText = view?.findViewById(R.id.endTime)
        stat.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        stat.nextImageButton = view?.findViewById(R.id.nextButton)
        stat.previousImageButton = view?.findViewById(R.id.previousButton)
        stat.loopImageButton = view?.findViewById(R.id.loopButton)
        stat.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        stat.songArtistView = view?.findViewById(R.id.songArtist)
        stat.songTitleView = view?.findViewById(R.id.songTitle)
        stat.glView = view?.findViewById(R.id.visualizer_view)
        stat.fab = view?.findViewById(R.id.favouritIcon)
        stat.fab?.alpha = 0.8f




       return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stat.audioVisualization = stat.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        stat.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        stat.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        stat.audioVisualization?.onResume()
        stat.mSensorManager?.registerListener(stat.mSensorListner,
            stat.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {

        stat.audioVisualization?.onPause()
        super.onPause()
        stat.mSensorManager?.unregisterListener(stat.mSensorListner)
    }

    override fun onDestroyView() {
        stat.audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stat.mSensorManager = stat.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcc = 0.0f
        mAccCurrent = SensorManager.GRAVITY_EARTH
        mAccLast = SensorManager.GRAVITY_EARTH
        bindShakeListner()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect -> {
                stat.myActivity?.onBackPressed()
                return false
            }

        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        stat.currentSongHelper = CurrentSongHelper()
        stat.currentSongHelper?.isPlaying = true
        stat.currentSongHelper?.isLoop = false
        stat.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String?
        var _songArtist: String?
        var songId: Long

        try {

            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()!!
            stat.currentPosition = arguments?.getInt("songPosition")!!
            stat.fetchSongs = arguments?.getParcelableArrayList( "songData")


            stat.currentSongHelper?.songPath = path
            stat.currentSongHelper?.songArtist = _songArtist
            stat.currentSongHelper?.songTitle = _songTitle
            stat.currentSongHelper?.songId = songId
            stat.currentSongHelper?.currentPosition = stat.currentPosition
            stat.currentSongHelper?.trackPosition

            staticated.updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)


        }catch (e: Exception){
            e.printStackTrace()
        }
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null){
            stat.mediaPlayer = FavouritFragment.stat.mediaPlayer
        }else {

            stat.mediaPlayer = MediaPlayer()
            stat.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {

                stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(path))
                stat.mediaPlayer?.prepare()

            } catch (e: Exception) {

                e.printStackTrace()
            }

            stat.mediaPlayer?.start()
        }
        staticated.processInformation(stat.mediaPlayer as MediaPlayer)

        if(stat.currentSongHelper?.isPlaying as Boolean){
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        stat.mediaPlayer?.setOnCompletionListener {
            staticated.onSongComplete()
        }

        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(stat.myActivity as Context, 0)
        stat.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if(isShuffleAllowed as Boolean){
            stat.currentSongHelper?.isShuffle = true
            stat.currentSongHelper?.isLoop = false
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)


        }else{
            stat.currentSongHelper?.isShuffle = false
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if(isLoopAllowed as Boolean){
            stat.currentSongHelper?.isShuffle = false
            stat.currentSongHelper?.isLoop = true
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)

        }else{
            stat.currentSongHelper?.isLoop = false
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_on))
        }else{
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_off))
        }
    }

    fun clickHandler(){

        stat.fab?.setOnClickListener({
            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_off))
                stat.favoriteContent?.deleteFavorite(stat.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(stat.myActivity,"Remove from favorites", Toast.LENGTH_SHORT).show()
            }else{
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_on))
                stat.favoriteContent?.storeAsFavorite(stat.currentSongHelper?.songId?.toInt() as Int, stat.currentSongHelper?.songArtist,
                    stat.currentSongHelper?.songTitle, stat.currentSongHelper?.songPath)
                Toast.makeText(stat.myActivity,"Added to favorites", Toast.LENGTH_SHORT).show()
            }
        })

        stat.shuffleImageButton?.setOnClickListener({

            var  editorShuffle: SharedPreferences.Editor? = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop: SharedPreferences.Editor? = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (stat.currentSongHelper?.isShuffle as Boolean)
            {
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                    stat.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()

            }else{
                stat.currentSongHelper?.isShuffle = true
                stat.currentSongHelper?.isLoop = false
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()

            }

        })

        stat.nextImageButton?.setOnClickListener({
            stat.currentSongHelper?.isPlaying = true
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if(stat.currentSongHelper?.isShuffle as Boolean){
                staticated.playNext("PlayNextLikeNormalShuffle")
            }else{
                staticated.playNext("PlayNextNormal")
            }

        })

        stat.previousImageButton?.setOnClickListener({
            stat.currentSongHelper?.isPlaying = true
            if (stat.currentSongHelper?.isLoop as Boolean){
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()


        })

        stat.loopImageButton?.setOnClickListener({

            var  editorShuffle: SharedPreferences.Editor? = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop: SharedPreferences.Editor? = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (stat.currentSongHelper?.isLoop as Boolean){
                stat.currentSongHelper?.isLoop = false
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }else{
                stat.currentSongHelper?.isLoop = true
                stat.currentSongHelper?.isShuffle = false
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }

        })

        stat.playPauseImageButton?.setOnClickListener({
            if(stat.mediaPlayer?.isPlaying as Boolean){
                stat.mediaPlayer?.pause()
                stat.currentSongHelper?.isPlaying = false
                stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                stat.mediaPlayer?.start()
                stat.currentSongHelper?.isPlaying = true
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }



    fun playPrevious(){
        stat.currentPosition = stat.currentPosition - 1
        if(stat.currentPosition == -1){
            stat.currentPosition = 0
        }
        if(stat.currentSongHelper?.isPlaying as Boolean){
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else{
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        stat.currentSongHelper?.isLoop = false
        var nextSong = stat.fetchSongs?.get(stat.currentPosition)
        stat.currentSongHelper?.songTitle = nextSong?.songTitle
        stat.currentSongHelper?.songPath = nextSong?.songData
        stat.currentSongHelper?.currentPosition = stat.currentPosition
        stat.currentSongHelper?.songId = nextSong?.songId as Long

        staticated.updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

        stat.mediaPlayer?.reset()

        try{
            stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(stat.currentSongHelper?.songPath))
            stat.mediaPlayer?.prepare()
            stat.mediaPlayer?.start()
            staticated.processInformation(stat.mediaPlayer as MediaPlayer)

        }catch (e: Exception){
            e.printStackTrace()
        }

        if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_on))
        }else{
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity!!, R.drawable.favorite_off))
        }
    }

    fun bindShakeListner(){
        stat.mSensorListner = object : SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {

                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]
                mAccLast = mAccCurrent
                mAccCurrent = Math.sqrt(((x*x + y*y + z*z).toDouble())).toFloat()
                var delta = mAccCurrent - mAccLast
                mAcc = mAcc*0.9f + delta
                if (mAcc > 12){
                    val prefs = stat.myActivity?.getSharedPreferences(stat.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean){
                        staticated.playNext("PlayNextNormal")
                    }
                }

            }

        }
    }

}
