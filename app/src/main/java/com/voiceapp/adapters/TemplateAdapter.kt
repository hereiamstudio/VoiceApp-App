package com.voiceapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voiceapp.R
import com.voiceapp.data.model.Template
import com.voiceapp.databinding.ViewTemplateListItemBinding

class TemplateAdapter(private val listener: TemplateListener) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {
    private val data: ArrayList<Template> = ArrayList()

    companion object {
        var checkedPosition = -1
    }

    init {
        checkedPosition = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_template_list_item, parent, false)
        return TemplateViewHolder(this, itemView, listener)
    }

    override fun onBindViewHolder(viewHolder: TemplateViewHolder, position: Int) {
        viewHolder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(templates: List<Template>?) {
        if (templates != null) {
            data.clear()
            data.addAll(templates)
        } else {
            data.clear()
        }
        notifyDataSetChanged()
    }

    class TemplateViewHolder(
        private val templateAdapter: TemplateAdapter,
        itemView: View,
        private val listener: TemplateListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding: ViewTemplateListItemBinding = ViewTemplateListItemBinding.bind(itemView)

        fun bind(template: Template) {
            binding.root.text = template.question_title
            binding.root.setOnCheckedChangeListener(null)
            binding.root.isChecked =  bindingAdapterPosition == checkedPosition

            binding.root.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    if (checkedPosition != -1){
                        templateAdapter.notifyItemChanged(checkedPosition)
                    }
                    checkedPosition = bindingAdapterPosition
                    listener.onTemplateClicked(template)
                }
            }
        }
    }

    interface TemplateListener {
        fun onTemplateClicked(template: Template)
    }

}