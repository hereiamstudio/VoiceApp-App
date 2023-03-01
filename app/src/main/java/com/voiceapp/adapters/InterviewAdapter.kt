package com.voiceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voiceapp.R
import com.voiceapp.data.model.Interview
import com.voiceapp.data.model.Project
import com.voiceapp.data.model.simple.SimpleTrackingHelper
import com.voiceapp.databinding.ViewInterviewListHeaderBinding
import com.voiceapp.databinding.ViewInterviewListItemBinding
import java.util.*

class InterviewAdapter(private val project: Project, private val listener: InteviewListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = ArrayList<Interview>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val headerView = LayoutInflater.from(parent.context).inflate(R.layout.view_interview_list_header, parent, false)
            return HeaderViewHolder(headerView)
        } else if (viewType == TYPE_ITEM) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_interview_list_item, parent, false)
            return InterviewViewHolder(itemView, listener)
        }
        throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is HeaderViewHolder) {
            viewHolder.bind(project)
        } else if (viewHolder is InterviewViewHolder) {
            viewHolder.bind(data[position - 1])
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    fun setData(interviews: List<Interview>?) {
        if (interviews != null) {
            data.clear()
            data.addAll(interviews)
            project.interviews = interviews
            notifyDataSetChanged()
        } else {
            data.clear()
            notifyDataSetChanged()
        }
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewInterviewListHeaderBinding.bind(itemView)

        fun bind(project: Project) {
            binding.headerTitleText.text = project.description
            binding.assignedInterviewsText.text = binding.root.resources.getQuantityString(
                R.plurals.interviewlist_item_assigned_interviews,
                project.interviews.size,
                project.interviews.size)
        }
    }

    internal class InterviewViewHolder(itemView: View, private val listener: InteviewListener) : RecyclerView.ViewHolder(itemView) {

        private var interview: Interview? = null
        private val binding = ViewInterviewListItemBinding.bind(itemView)

        fun bind(interview: Interview) {
            this.interview = interview
            binding.tvInterviewIssue.text = interview.title
            binding.tvInterviewDescription.text = interview.description

            if (interview.getQuestionCount() == null) {
                binding.tvQuestionsCount.visibility = View.GONE
            } else {
                binding.tvQuestionsCount.visibility = View.VISIBLE
                binding.tvQuestionsCount.text = binding.root.resources.getQuantityString(
                    R.plurals.interviews_item_questions_count,
                    interview.getQuestionCount() ?: 0,
                    interview.getQuestionCount() ?: 0)
            }
            if (interview.responses_count > 0) {
                binding.tvInterviewsCompleted.visibility = View.VISIBLE
                binding.tvInterviewsCompleted.text = binding.root.resources.getQuantityString(
                    R.plurals.interviewlist_item_completed_interviews,
                    interview.responses_count,
                    interview.responses_count)
            }

            binding.newIcon.visibility = if (interview.seen) View.GONE else View.VISIBLE

            SimpleTrackingHelper.updateSeen(interview.id!!)

            itemView.setOnClickListener {
                listener.onInterviewClicked(interview)
            }
        }
    }

    interface InteviewListener {
        fun onInterviewClicked(interview: Interview)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

}