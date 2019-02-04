package com.kshitijchauhan.haroldadmin.moviedb.ui.auth

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.ApiManager
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.SessionIdInterceptor
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.service.account.AccountDetailsResponse
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.service.auth.CreateSessionRequest
import com.kshitijchauhan.haroldadmin.moviedb.repository.data.remote.utils.NetworkResponse
import com.kshitijchauhan.haroldadmin.moviedb.utils.Constants
import com.kshitijchauhan.haroldadmin.moviedb.utils.SharedPreferencesDelegate
import com.kshitijchauhan.haroldadmin.moviedb.utils.SingleLiveEvent
import com.kshitijchauhan.haroldadmin.moviedb.utils.extensions.disposeWith
import com.kshitijchauhan.haroldadmin.moviedb.utils.extensions.safe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AuthenticationViewModel(sharedPreferences: SharedPreferences,
                              private val sessionIdInterceptor: SessionIdInterceptor,
                              private val apiManager: ApiManager) : ViewModel() {

    private val _requestToken = MutableLiveData<String>()
    private val _authSuccess = SingleLiveEvent<Boolean>()
    private val _accountDetails = MutableLiveData<AccountDetailsResponse>()
    private val compositeDisposable = CompositeDisposable()
    private var sessionId by SharedPreferencesDelegate(sharedPreferences, Constants.KEY_SESSION_ID, "")
    private var isAuthenticated by SharedPreferencesDelegate(sharedPreferences, Constants.KEY_IS_AUTHENTICATED, false)
    private var accountId by SharedPreferencesDelegate(sharedPreferences, Constants.KEY_ACCOUNT_ID, -1)

    val requestToken: LiveData<String>
        get() = _requestToken

    val authSuccess: LiveData<Boolean>
        get() = _authSuccess

    val accountDetails: LiveData<AccountDetailsResponse>
        get() = _accountDetails

    fun getRequestToken() {
        apiManager
            .getRequestToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { response ->
                if (response.success) {
                    _requestToken.value = response.requestToken
                }
            }
            .subscribe()
            .disposeWith(compositeDisposable)
    }

    fun createSession(request: CreateSessionRequest) {
        apiManager
            .createSession(request)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .toObservable()
            .doOnNext { response ->
                sessionId = response.sessionId
                setNewSessionIdToInterceptor(response.sessionId)
                isAuthenticated = true
            }
            .switchMapSingle {
                apiManager.getAccountDetails()
            }
            .doOnNext {
                _authSuccess.postValue(true)
            }
            .subscribe()
            .disposeWith(compositeDisposable)
    }

    fun getAccountDetails() {
        apiManager
            .getAccountDetails()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .doOnSuccess { response ->
                accountId = response.id
                _accountDetails.postValue(response)
            }
            .subscribe()
            .disposeWith(compositeDisposable)
    }

    fun setNewSessionIdToInterceptor(newId: String) {
        sessionIdInterceptor.setSessionId(newId)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}