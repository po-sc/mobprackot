package com.example.MyApplication2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.MyApplication2.viewmodels.FirstViewModel
import com.example.task_2.R
import android.util.Log
import androidx.navigation.fragment.findNavController

class FirstFragment : Fragment() {

    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var textViewResult: TextView

    // Инициализируем ViewModel
    private val viewModel: FirstViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // Найдем элементы управления
        editTextLogin = view.findViewById(R.id.editTextLogin)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        textViewResult = view.findViewById(R.id.textViewResult)
        val buttonLogin: Button = view.findViewById(R.id.buttonLogin)

        // Установим наблюдатель для отображения результата
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            textViewResult.text = result
        })

        // Обработчик нажатия кнопки входа
        buttonLogin.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()
            viewModel.login(login, password)
        }
        val buttonManually: Button = view.findViewById(R.id.buttonManually)
        buttonManually.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, SecondFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        // Navigation button
        val buttonNavigate: Button = view.findViewById(R.id.buttonNavi)
        buttonNavigate.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
            Log.d("FirstFragment", "Navigated to SecondFragment")
        }

        return view
    }
}
