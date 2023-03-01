package com.voiceapp.ui.project

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.voiceapp.R

typealias OnProjectClickedListener = (project: UiProject) -> Unit

class ProjectListAdapter(
    context: Context,
    private val projectClickedListener: OnProjectClickedListener
)
    : ListAdapter<UiProject, ProjectViewHolder>(ItemEquator()) {

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProjectViewHolder(inflater.inflate(R.layout.view_project_list_item, parent, false),
            projectClickedListener)

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.populate(getItem(position))
    }

    override fun getItemId(position: Int) = getItem(position)?.id?.hashCode()?.toLong() ?: -1L

    private class ItemEquator : DiffUtil.ItemCallback<UiProject>() {

        override fun areItemsTheSame(oldItem: UiProject, newItem: UiProject) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UiProject, newItem: UiProject) =
            oldItem == newItem
    }
}