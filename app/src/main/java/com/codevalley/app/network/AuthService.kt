package com.codevalley.app.network

import com.codevalley.app.model.LoginRequestDTO
import com.codevalley.app.model.RegisterRequestDTO
import com.codevalley.app.model.TfCodeAuthDto
import com.codevalley.app.model.TokenResponse
import com.codevalley.app.model.UploadAvatarResponseDTO
import com.codevalley.app.model.UserResponseDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AuthService {
    @GET("/auth/me")
    suspend fun getMe(): UserResponseDTO

    @GET("/auth/profile/{id}")
    suspend fun getProfile(@Path("id") id: Int): UserResponseDTO

    @Multipart
    @POST("/auth/avatar")
    suspend fun uploadAvatar(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): UploadAvatarResponseDTO

    @POST("/auth/2fa/turn-on")
    suspend fun turnOnTwoFactorAuthentication()

    @POST("/auth/2fa/turn-off")
    suspend fun turnOffTwoFactorAuthentication()

    @POST("/auth/2fa/authenticate")
    suspend fun authenticateTwoFactor(@Body body: TfCodeAuthDto): TokenResponse

    @POST("/auth/2fa/generate")
    suspend fun generateTwoFactor(): Map<String, String>

    @POST("/auth/login")
    suspend fun login(@Body loginRequest: LoginRequestDTO): TokenResponse

    @POST("/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequestDTO): TokenResponse

    @GET("/auth/search/{username}")
    suspend fun searchUsers(@Path("username") username: String): List<UserResponseDTO>

    @POST("/auth/logout")
    suspend fun logout()
}
