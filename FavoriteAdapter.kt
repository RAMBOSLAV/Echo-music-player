package com.example.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.fragments.SongPlayingFragment

class FavoriteAdapter(_songDetails: ArrayList<Songs>, _context: Context) :
    RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>() {


    var songDetails: ArrayList<Songs>? = null
    var myContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.myContext = _context
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        val songObject = songDetails?.get(p1)
        p0.trackTitle?.text = songObject?.songTitle
        p0.trackArtist?.text = songObject?.artist

        p0.contentHolder?.setOnClickListener({
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songId?.toInt() as Int)
            args.putInt("songPosition", p1)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args

            ( myContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragmentFavorite")
                .commit()
        })
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, p0, false)
        return MyViewHolder(itemView)

    }

    override fun getItemCount(): Int {

        if (songDetails == null) {
            return 0
        } else {
            return (songDetails as ArrayList<Songs>).size
        }
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById(R.id.titleTrack) as TextView
            trackArtist = view.findViewById(R.id.trackArtist) as TextView
            contentHolder = view.findViewById(R.id.contentRow) as RelativeLayout
        }
    }
}
