package com.codevalley.app.repository

import android.annotation.SuppressLint
import com.codevalley.app.model.UserResponseDTO
import okhttp3.MultipartBody
import retrofit2.Retrofit
import javax.inject.Inject
import android.content.Context
import android.net.Uri
import com.codevalley.app.model.ApiAuthResponse
import com.codevalley.app.model.ApiError
import com.codevalley.app.model.LoginRequestDTO
import com.codevalley.app.model.RegisterRequestDTO
import com.codevalley.app.model.TfCodeAuthDto
import com.codevalley.app.model.TokenResponse
import com.codevalley.app.network.AuthService
import com.codevalley.app.network.createAuthorizedApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

class UserRepository @Inject constructor(
    private val retrofit: Retrofit,
    private val context: Context
) {
    private val authService = retrofit.newBuilder()
        .client(OkHttpClient.Builder().build())
        .build()
        .create(AuthService::class.java)

    private fun createAuthorizedApiService(): AuthService {
        return createAuthorizedApiService(retrofit, AuthService::class.java)
    }

    suspend fun getProfile(id: Int): UserResponseDTO {
        val authorizedApiService = createAuthorizedApiService()
        return authorizedApiService.getProfile(id)
    }

    suspend fun getMe(): UserResponseDTO {
        val authorizedApiService = createAuthorizedApiService()
        return authorizedApiService.getMe()
    }

    @SuppressLint("Recycle")
    @Throws(IOException::class)
    suspend fun uploadAvatar(userId: Int, fileUri: Uri): String {
        val authorizedApiService = createAuthorizedApiService()

        val userIdRequestBody = userId.toString().toRequestBody(MultipartBody.FORM)
        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(fileUri)
        val inputStream = contentResolver.openInputStream(fileUri)
            ?: throw IOException("Unable to open input stream for URI: $fileUri")
        val fileBytes = inputStream.readBytes()
        val fileRequestBody = fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "avatar-${Date().time}.jpg", fileRequestBody)

        return try {
            val response = authorizedApiService.uploadAvatar(userIdRequestBody, filePart)
            response.avatarUrl
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            throw IOException("HTTP ${e.code()} ${e.message()}: $errorBody")
        }
    }

    suspend fun turnOnTwoFactorAuthentication() {
        val authorizedApiService = createAuthorizedApiService()
        authorizedApiService.turnOnTwoFactorAuthentication()
    }

    suspend fun turnOffTwoFactorAuthentication() {
        val authorizedApiService = createAuthorizedApiService()
        authorizedApiService.turnOffTwoFactorAuthentication()
    }

    suspend fun authenticateTwoFactor(body: TfCodeAuthDto): TokenResponse {
        val authorizedApiService = createAuthorizedApiService()
        return authorizedApiService.authenticateTwoFactor(body)
    }

    suspend fun generateTwoFactor(): Pair<String, String> {
        val authorizedApiService = createAuthorizedApiService()
        val response = authorizedApiService.generateTwoFactor()
        val qrCodeUrl = response["qrCodeUrl"] ?: throw IOException("Failed to generate QR code")
        val otpAuthUrl = response["setupKey"] ?: throw IOException("Failed to retrieve OTP URL")
        return Pair(qrCodeUrl, otpAuthUrl)
    }

    suspend fun logout() {
        val authorizedApiService = createAuthorizedApiService()
        authorizedApiService.logout()
    }

    suspend fun login(email: String, password: String): ApiAuthResponse<TokenResponse> {
        return try {
            val response = authService.login(LoginRequestDTO(email, password))
            ApiAuthResponse.Success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val gson = Gson()
            val type = object : TypeToken<ApiError>() {}.type
            val errorResponse: ApiError? = gson.fromJson(errorBody, type)
            errorResponse?.let {
                ApiAuthResponse.Error(it)
            } ?: ApiAuthResponse.Error(ApiError("An unexpected error occured when trying to login to your account", "Error", e.code()))
        } catch (e: Exception) {
            ApiAuthResponse.Error(ApiError(e.localizedMessage ?: "An unexpected error occured when trying to login to your account", "Error", -1))
        }
    }

    suspend fun register(username: String, email: String, password: String): ApiAuthResponse<TokenResponse> {
        return try {
            val response = authService.register(RegisterRequestDTO(username, email, password))
            ApiAuthResponse.Success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val gson = Gson()
            val type = object : TypeToken<ApiError>() {}.type
            val errorResponse: ApiError? = gson.fromJson(errorBody, type)
            errorResponse?.let {
                ApiAuthResponse.Error(it)
            } ?: ApiAuthResponse.Error(ApiError("An unexpected error occured during the creation of your account", "Error", e.code()))
        } catch (e: Exception) {
            ApiAuthResponse.Error(ApiError(e.localizedMessage ?: "An unexpected error occured during the creation of your account", "Error", -1))
        }
    }
}
