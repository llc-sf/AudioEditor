package com.san.audioeditor.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.AudioPickActivity
import com.san.audioeditor.databinding.FragmentCreateBinding
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log

class CreateFragment : BaseFragment() {

    companion object {
        const val TAG = "CreateFragment"
    }

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

        binding.create.setOnClickListener {
            val intent = Intent(context, AudioPickActivity::class.java)
            pickAudioResult.launch(intent)
        }
    }

    private val pickAudioResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var song = result.data?.getParcelableExtra<Song>(AudioPickFragment.PARAM_SONG)
                Log.i(TAG, "pick song: $song")
                if(song!= null){
                    AudioCutActivity.open(requireContext(), song!!)
                }
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }


}