package com.skyblue.skybluecontacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.model.Contacts

class ContactAdapter(private var contacts: List<Contacts>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.name)
        val phoneText: TextView = view.findViewById(R.id.phoneNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameText.text = contact.firstName
        holder.phoneText.text = contact.phoneNumber

        holder.itemView.setOnClickListener {
            // Handle item click
            Toast.makeText(holder.itemView.context, "Clicked on ${contact.firstName}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateData(newContacts: List<Contacts>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}