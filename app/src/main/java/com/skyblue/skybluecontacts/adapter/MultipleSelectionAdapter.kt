package com.skyblue.skybluecontacts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.ContactsDiffCallback
import com.skyblue.skybluecontacts.databinding.ItemContactBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.Options

class MultipleSelectionAdapter(private var contacts: MutableList<ContactsRoom>,
                          private val onClick: (Options) -> Unit
) : RecyclerView.Adapter<ContactViewHolder1>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    private var expandedPosition: Int? = null
    private val TAG = "RoomAdapter_"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder1 {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder1(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder1, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(contacts[position])


    }

    override fun getItemCount() = contacts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContacts: List<ContactsRoom>) {
        contacts = newContacts.toMutableList()
        notifyDataSetChanged()
    }

    fun updateList(newList: List<ContactsRoom>) {
        val diffCallback = ContactsDiffCallback(contacts, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        contacts.clear()
        contacts.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }


    fun removeItem(contact: ContactsRoom) {
        val newList = contacts.toMutableList()
        newList.remove(contact)
        updateList(newList)
    }
}



class ContactViewHolder1(val binding: ItemContactBinding) :
    RecyclerView.ViewHolder(binding.root){
    fun bind(contact: ContactsRoom) {
        binding.name.text = contact.firstName
        binding.phoneNumber.text = contact.phoneNumber

        binding.root.setOnClickListener {
           // onItemClick(contact)
        }
    }
}