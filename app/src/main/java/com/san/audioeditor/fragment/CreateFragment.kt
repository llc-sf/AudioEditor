package com.san.audioeditor.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.databinding.FragmentCreateBinding
import dev.android.player.framework.base.BaseFragment

class CreateFragment : BaseFragment() {


    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreatedCompat(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedCompat(view, savedInstanceState)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }


}