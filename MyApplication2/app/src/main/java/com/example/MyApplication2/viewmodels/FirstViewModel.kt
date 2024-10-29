package com.example.MyApplication2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FirstViewModel : ViewModel() {

    // LiveData для хранения текста результата
    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> get() = _loginResult

    // Метод для обновления результата логина
    fun login(login: String, password: String) {
        if (login == "root" && password == "root") {
            _loginResult.value = "Success"
        } else {
            _loginResult.value = "Invalid login or password"
        }
    }
}
