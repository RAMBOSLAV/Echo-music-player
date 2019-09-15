package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.adapters.FavoriteAdapter
import com.example.echo.databases.EchoDatabase
import com.example.echo.fragments.SongPlayingFragment.stat.mediaPlayer
import com.example.echo.fragments.SongPlayingFragment.stat.playPauseImageButton
import kotlinx.android.synthetic.main.fragment_song_playing.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@Suppress("DEPRECATION")
class FavouritFragment : Fragment() {

    var myActivity: Activity? = null

    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null

    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null

    object stat{
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_favourit, container, false)
        activity?.title = "Favorites"
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseFavButton)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivity)
        displayFavoritesBySearching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

            val item = menu?.findItem(R.id.action_sort)
            item?.isVisible = false

    }

    fun getSongsFromPhone(): ArrayList<Songs>{

        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if(songCursor!=null && songCursor.moveToFirst()){

            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)


            while (songCursor.moveToNext()){
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDateAdded = songCursor.getLong(songDateAdded)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDateAdded))
            }
        }
        return arrayList
    }

    fun bottomBarSetup(){

        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.stat.currentSongHelper?.songTitle)
            SongPlayingFragment.stat.mediaPlayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.stat.currentSongHelper?.songTitle)
                SongPlayingFragment.staticated.onSongComplete()
            })

            if(SongPlayingFragment.stat.mediaPlayer?.isPlaying as Boolean){
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }else{
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            stat.mediaPlayer = SongPlayingFragment.stat.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.stat.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.stat.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.stat.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.stat.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition",SongPlayingFragment.stat.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.stat.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")
                ?.commit()
        })
        playPauseImageButton?.setOnClickListener({
            if(SongPlayingFragment.stat.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.stat.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.stat.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.stat.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.stat.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun displayFavoritesBySearching(){
        if (favoriteContent?.checkSize() as Int > 0){
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favoriteContent?.queryDBList()
            var fetchListFromDevice = getSongsFromPhone()
            if (fetchListFromDevice != null){
                for (i in 0..fetchListFromDevice?.size - 1){
                    for (j in 0..getListFromDatabase?.size as Int - 1){
                        if ((getListFromDatabase?.get(j)?.songId) === (fetchListFromDevice?.get(i)?.songId)){
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])

                        }
                    }
                }
            }else{

            }

            if (refreshList == null){

                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE

            }else{
                var favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }

}
