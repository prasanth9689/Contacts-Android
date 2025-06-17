package com.skyblue.skybluecontacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.model.ContactVcf

class ContactVcfAdapter(private var contacts: List<ContactVcf>) :
    RecyclerView.Adapter<ContactVcfAdapter.ViewHolder>() {

    fun updateList(newList: List<ContactVcf>) {
        contacts = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvName)
        val phoneText: TextView = view.findViewById(R.id.tvPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_vcf, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameText.text = contacts[position].name
        holder.phoneText.text = contacts[position].phone
    }
}
