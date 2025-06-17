package com.skyblue.skybluecontacts.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.skyblue.skybluecontacts.model.ContactVcf
import java.io.BufferedReader
import java.io.InputStreamReader

class ContactVcfRepository {

    fun parseVcfFile(uri: Uri, context: Context): List<ContactVcf> {
        val contacts = mutableListOf<ContactVcf>()
        var name: String? = null
        var phone: String? = null

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.forEachLine { line ->
                Log.d("VCF", "Line: $line") // already confirmed this works

                when {
                    line.startsWith("FN") -> {
                        name = line.substringAfter(":").trim()
                    }
                    line.startsWith("TEL") -> {
                        phone = line.substringAfter(":").trim()
                    }
                    line.startsWith("END:VCARD") -> {
                        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
                            val contact = ContactVcf(name!!, phone!!)
                            contacts.add(contact)
                            Log.d("VCF", "Added: $contact")
                        }
                        name = null
                        phone = null
                    }
                }
            }

            reader.close()
        } catch (e: Exception) {
            Log.e("VCF", "Error reading vcf: ${e.message}")
        }

        Log.d("VCF", "Parsed Contacts: $contacts")
        return contacts
    }

}
