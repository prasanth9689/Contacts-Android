package com.skyblue.skybluecontacts.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.model.ContactVcf

class ContactVcfAdapter(
    private var contacts: List<ContactVcf>,
    private val onItemClick: (Int) -> Unit  // <- You MUST declare this
) : RecyclerView.Adapter<ContactVcfAdapter.ViewHolder>() {

    fun updateList(newList: List<ContactVcf>) {
        contacts = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvName)
        val phoneText: TextView = view.findViewById(R.id.tvPhone)
        val container: View = view.findViewById(R.id.container)

        init {
            // Call the onItemClick lambda with the current adapter position
            container.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_vcf, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameText.text = contact.name
        holder.phoneText.text = contact.phone

        holder.container.setBackgroundColor(
            if (contact.isSelected)
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            else
                Color.TRANSPARENT
        )
    }
}
