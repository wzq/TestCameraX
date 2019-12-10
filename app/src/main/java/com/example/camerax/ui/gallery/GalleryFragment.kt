package com.example.camerax.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.camerax.R
import java.io.File

class GalleryFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val listView = view?.findViewById<RecyclerView>(R.id.list)
        val adapter = GalleryAdapter()
        listView?.adapter = adapter
        val dir = requireActivity().externalMediaDirs.first()
        if (dir.isDirectory) {
            val lastImage = dir.listFiles()?.toMutableList()
            adapter.submitList(lastImage)
        }
    }
}


class GalleryAdapter : ListAdapter<File, RecyclerView.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        return object : RecyclerView.ViewHolder(view) {
            init {
                view.setOnLongClickListener { v ->
                    val file = v.tag as? File
                    val list =
                        arrayListOf<File>().apply { addAll(currentList.filterNot { it == file }) }
                    submitList(list)
                    file?.delete() ?: false
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val img = holder.itemView.findViewById<ImageView>(R.id.item)
        Glide.with(holder.itemView).load(getItem(position)).into(img)
        holder.itemView.tag = getItem(position)
    }


    class DiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

    }

}