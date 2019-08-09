package com.skydoves.preferenceroomdemo.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.skydoves.preferenceroomdemo.R
import com.skydoves.preferenceroomdemo.models.ItemProfile
import java.util.*

class ListViewAdapter(context: Context, private val layout: Int) : BaseAdapter() {
    private val inflater: LayoutInflater
    private val profileList: MutableList<ItemProfile>

    init {
        this.profileList = ArrayList()
        this.inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount() = profileList.size

    override fun getItem(i: Int) = profileList[i]

    override fun getItemId(i: Int) = i.toLong()

    fun addItem(itemProfile: ItemProfile) {
        this.profileList.add(itemProfile)
        notifyDataSetChanged()
    }

    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null)
            view = this.inflater.inflate(layout, viewGroup, false)

        val itemProfile = profileList[index]

        val title = view!!.findViewById<TextView>(R.id.item_profile_title)
        title.text = itemProfile.title

        val content = view.findViewById<TextView>(R.id.item_profile_content)
        content.text = itemProfile.content
        return view
    }
}
