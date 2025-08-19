package com.skyblue.skybluecontacts.retrofit

import com.skyblue.skybluecontacts.model.ContactPayload
import com.skyblue.skybluecontacts.model.ContactResponse
import com.skyblue.skybluecontacts.model.DeleteAccount
import com.skyblue.skybluecontacts.model.DeleteAccountResponse
import com.skyblue.skybluecontacts.model.DeleteSingleCloud
import com.skyblue.skybluecontacts.model.DeleteSingleCloudResponse
import com.skyblue.skybluecontacts.model.DeleteTrashCloud
import com.skyblue.skybluecontacts.model.DeleteTrashCloudResponse
import com.skyblue.skybluecontacts.model.Login
import com.skyblue.skybluecontacts.model.RenameCloudContact
import com.skyblue.skybluecontacts.model.RenameResponse
import com.skyblue.skybluecontacts.model.SaveResponse
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.TrashResponse
import com.skyblue.skybluecontacts.model.TrashRestoreCloud
import com.skyblue.skybluecontacts.model.TrashRestoreCloudResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIInterface {

    @POST("contacts.php")
    suspend fun renameContact(@Body renameContact: RenameCloudContact): RenameResponse

    @POST("contacts.php")
    suspend fun deleteAccount(@Body deleteAccount: DeleteAccount): DeleteAccountResponse

    @POST("contacts.php")
    suspend fun restoreTrashCloudContact(@Body trashRestoreCloud: TrashRestoreCloud): TrashRestoreCloudResponse

    @POST("contacts.php")
    suspend fun deleteTrashCloudContact(@Body deletetTrashCloud: DeleteTrashCloud): DeleteTrashCloudResponse

    @POST("contacts.php")
    suspend fun getTrashContacts(@Body trashRequest: TrashRequest): TrashResponse

    @POST("contacts.php")
    suspend fun deleteCloudContact(@Body deleteSingleCloud: DeleteSingleCloud): DeleteSingleCloudResponse

    @POST("contacts.php")
    suspend fun saveSingleContact(@Body requestBody: RequestBody): SaveResponse

    @POST("contacts.php")
    fun sendContacts(@Body payload: ContactPayload): Call<ResponseBody>

    @POST("contacts.php")
    suspend fun getContacts(@Body requestBody: RequestBody): ContactResponse

    @POST("contacts.php")
    fun login(@Body requestBody: RequestBody): Call<Login>
}