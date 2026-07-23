package com.speehive.speehiveaihub.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import okhttp3.MultipartBody

interface SpeehiveApiService {

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // Campaigns

    @GET("api/Campaigns")
    suspend fun getCampaigns(): List<CampaignResponse>

    // Events

    @GET("api/Events")
    suspend fun getEvents(): List<EventResponse>

    @PUT("api/Events/{id}/cancel")
    suspend fun cancelEvent(
        @Path("id") id: String
    ): retrofit2.Response<Unit>
    // Admin

    @GET("api/Admin/users")
    suspend fun getUsers(): List<UserResponse>

    @POST("api/Admin/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): retrofit2.Response<Unit>

    @PUT("api/Admin/users/{id}/activate")
    suspend fun activateUser(
        @Path("id") id: String
    ): retrofit2.Response<Unit>

    @PUT("api/Admin/users/{id}/deactivate")
    suspend fun deactivateUser(
        @Path("id") id: String
    ): retrofit2.Response<Unit>

    @GET("api/Admin/auditlogs")
    suspend fun getAuditLogs(): List<AuditLogResponse>

    @POST("api/Approval/approve")
    suspend fun approveCampaign(
        @Body request: ApprovalRequest
    ): retrofit2.Response<okhttp3.ResponseBody>

    @POST("api/Approval/reject")
    suspend fun rejectCampaign(
        @Body request: ApprovalRequest
    ): retrofit2.Response<okhttp3.ResponseBody>

    @GET("api/Approval/{eventId}/schedule")
    suspend fun getCampaignSchedule(
        @Path("eventId") eventId: String
    ): CampaignScheduleResponse

    @PUT("api/Approval/{eventId}/schedule/{platform}")
    suspend fun updatePlatformSchedule(
        @Path("eventId") eventId: String,
        @Path("platform") platform: String,
        @Body request: UpdatePlatformScheduleRequest
    ): retrofit2.Response<Unit>

    // Designer

    @Multipart
    @POST("api/designer/events/{eventId}/image")
    suspend fun uploadDesignerImage(
        @Path("eventId") eventId: String,
        @Part image: MultipartBody.Part
    ): UploadImageResponse

    @PUT("api/designer/campaigns/{eventId}")
    suspend fun editCampaign(
        @Path("eventId") eventId: String,
        @Body request: EditCampaignRequest
    ): retrofit2.Response<Unit>

    @Multipart
    @POST("api/designer/campaigns/{eventId}/image")
    suspend fun uploadCampaignImage(
        @Path("eventId") eventId: String,
        @Part image: MultipartBody.Part
    ): UploadImageResponse

    @GET("api/SocialMediaCredentials")
    suspend fun getSocialMediaCredentials(): List<SocialMediaCredentialResponse>

    @PUT("api/SocialMediaCredentials/{provider}")
    suspend fun updateSocialMediaCredential(
        @Path("provider") provider: String,
        @Body request: UpdateSocialMediaCredentialRequest
    ): retrofit2.Response<Unit>

    @GET("api/platform-postings/{campaignId}")
    suspend fun getPlatformPostings(
        @Path("campaignId") campaignId: String
    ): retrofit2.Response<List<PlatformPostingResponse>>
}