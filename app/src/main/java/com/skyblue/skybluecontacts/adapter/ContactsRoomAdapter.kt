package com.skyblue.skybluecontacts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.model.ContactsRoom

class ContactsRoomAdapter(private var contacts: List<ContactsRoom>) : RecyclerView.Adapter<ContactViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(contacts[position])


            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position

                // Only update if clicked a different item
                if (previousPosition != selectedPosition) {
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                }
            }


        if (position == selectedPosition) {
            holder.optionsLayout.visibility = View.VISIBLE
        } else {
            holder.optionsLayout.visibility = View.GONE
        }
    }

    override fun getItemCount() = contacts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContacts: List<ContactsRoom>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}

class ContactViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_contact, parent, false)) {
    private var firstNameTextView: TextView = itemView.findViewById(R.id.name)
    private var phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumber)
    var optionsLayout: LinearLayout = itemView.findViewById(R.id.optionsLayout)

    fun bind(contact: ContactsRoom) {
        firstNameTextView.text = contact.firstName
        phoneNumberTextView.text = contact.phoneNumber
    }
}