package com.skyblue.skybluecontacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.skyblue.skybluecontacts.R
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.ContactsRoom

class ContactsRoomAdapter(private var contacts: List<ContactsRoom>) : RecyclerView.Adapter<ContactViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount() = contacts.size

    fun updateData(newContacts: List<ContactsRoom>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}

class ContactViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_contact, parent, false)) {
    private var firstNameTextView: TextView = itemView.findViewById(R.id.name)
    private var phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumber)


    fun bind(contact: ContactsRoom) {
        firstNameTextView.text = contact.firstName
        phoneNumberTextView.text = contact.phoneNumber
    }
}