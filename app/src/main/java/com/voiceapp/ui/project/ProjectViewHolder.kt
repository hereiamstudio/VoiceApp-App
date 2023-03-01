package com.voiceapp.ui.project

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.voiceapp.R
import com.voiceapp.databinding.ViewProjectListItemBinding

class ProjectViewHolder(
    itemView: View,
    private val projectClickedListener: OnProjectClickedListener
)
    : RecyclerView.ViewHolder(itemView) {

    private val viewBinding = ViewProjectListItemBinding.bind(itemView)
    private var project: UiProject? = null

    init {
        viewBinding.root.setOnClickListener {
            handleItemClicked()
        }
    }

    fun populate(project: UiProject?) {
        this.project = project

        project?.let {
            viewBinding.tvProjectName.text = it.title
            viewBinding.tvProjectDescription.text = it.description
            viewBinding.interviewCountText.text =
                viewBinding.interviewCountText.resources.getQuantityString(
                    R.plurals.projectlist_item_interviewcount, it.interviewCount, it.interviewCount)
        } ?: run {
            viewBinding.tvProjectName.text = null
            viewBinding.tvProjectDescription.text = null
            viewBinding.interviewCountText.text = null
        }
    }

    private fun handleItemClicked() {
        project?.let(projectClickedListener::invoke)
    }
}