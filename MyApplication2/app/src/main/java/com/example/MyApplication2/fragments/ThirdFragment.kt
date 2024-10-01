package com.example.MyApplication2.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.task_2.R

class ThirdFragment : Fragment() {

    private lateinit var textView: TextView
    private lateinit var checkBox: CheckBox

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third, container, false)

        textView = view.findViewById(R.id.textView)
        checkBox = view.findViewById(R.id.checkbox_cheers)


        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.text = "cheers"
            } else {
                textView.text = ""
            }
        }

        val buttonNavBack: Button = view.findViewById(R.id.buttonNavBack)
        buttonNavBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val buttonManBack: Button = view.findViewById(R.id.buttonManBack)
        buttonManBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}
