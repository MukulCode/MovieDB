package com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.service.account

import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.service.common.ErrorResponse
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.utils.NetworkResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface AccountService {

    @GET("account")
    fun getAccountDetails(): Single<NetworkResponse<AccountDetailsResponse, ErrorResponse>>

    @GET("account/{accountId}/watchlist/movies")
    fun getMoviesWatchList(@Path("accountId") accountId: Int): Single<NetworkResponse<MovieWatchlistResponse, ErrorResponse>>

    @GET("account/{accountId}/favorite/movies")
    fun getFavouriteMovies(@Path("accountId") accountId: Int): Single<NetworkResponse<FavouriteMoviesResponse, ErrorResponse>>

    @POST("account/{accountId}/favorite")
    fun toggleMediaFavouriteStatus(
        @Path("accountId") accountId: Int,
        @Body request: ToggleMediaFavouriteStatusRequest
    ): Single<ToggleFavouriteResponse>

    @POST("account/{accountId}/watchlist")
    fun toggleMediaWatchlistStatus(
        @Path("accountId") accountId: Int,
        @Body request: ToggleMediaWatchlistStatusRequest
    ): Single<ToggleWatchlistResponse>
}

