package com.example.MyApplication2.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.task_2.R
import kotlin.random.Random

class SecondFragment : Fragment() {

    private lateinit var slotTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var spinButton: Button

    // Список трехзначных чисел
    private val numbers = listOf(123, 234, 674, 899, 453, 777)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // Кнопки навигации - оставляем без изменений
        val buttonManually2: Button = view.findViewById(R.id.buttonManually2)
        buttonManually2.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, ThirdFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        val buttonNavigate2: Button = view.findViewById(R.id.buttonNavi2)
        buttonNavigate2.setOnClickListener {
            findNavController().navigate(R.id.action_secondFragment_to_thirdFragment)
            Log.d("SecondFragment", "Переход на ThirdFragment")
        }

        val buttonNavBack: Button = view.findViewById(R.id.buttonNavBack)
        buttonNavBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val buttonManBack: Button = view.findViewById(R.id.buttonManBack)
        buttonManBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Функциональность слота
        slotTextView = view.findViewById(R.id.slotTextView)
        resultTextView = view.findViewById(R.id.resultTextView)
        spinButton = view.findViewById(R.id.spinButton)

        spinButton.setOnClickListener {
            val randomNumber = numbers.random()
            slotTextView.text = randomNumber.toString()
            if (randomNumber == 777) {
                resultTextView.text = "Win!"
            } else {
                resultTextView.text = ""
            }
        }

        return view
    }
}
