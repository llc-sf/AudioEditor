package com.san.audioeditor.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.android.app.AppProvider
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.AudioPickActivity
import com.san.audioeditor.activity.MultiTrackerAudioEditorActivity
import com.san.audioeditor.databinding.FragmentCreateBinding
import com.san.audioeditor.permission.StoragePermissionManager
import com.san.audioeditor.storage.AudioSyncService
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.dp
import dev.android.player.framework.utils.getLocationOnScreen
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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isMulti = false
    override fun onViewCreatedCompat(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedCompat(view, savedInstanceState)

        binding.trim.setOnClickListener {
            isMulti = false
            val intent = Intent(context, AudioPickActivity::class.java) //            pickAudioResult.launch(intent)
            activity?.startActivity(intent)
        }
        requestPermission()

        adapterView()
    }

    private fun adapterView() {
        binding.des1.post {
            if (activity === null) {
                return@post
            }
            if (activity?.isFinishing == true) {
                return@post
            }
            if (!isAdded) {
                return@post
            }
            var rectBottom = binding.des21.getLocationOnScreen()
            var rectRoot = binding.root.getLocationOnScreen()
            var remainHeight = rectRoot.bottom - rectBottom.bottom
            binding.imgCut.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (remainHeight / (24f + 14f + 28f + 23) * 24).toInt()
            }

            binding.icTrimIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (remainHeight / (24f + 14f + 28f + 23) * 14).toInt()
            }

            binding.des1.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (remainHeight / (24f + 14f + 28f + 23) * 28).toInt()
            }
        }
    }


    private fun requestPermission() {
        StoragePermissionManager.onAfterRequestPermission(childFragmentManager, AndroidUtil.getStoragePermissionPermissionStringAdapter33(), {
            Log.i(AudioSyncService.TAG, "onDeny")
        }) {
            Log.i(AudioSyncService.TAG, "onGrant")
            AudioSyncService.sync(requireContext())
        }
    }

    private val pickAudioResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            var song = result.data?.getParcelableExtra<Song>(AudioPickFragment.PARAM_SONG)
            Log.i(TAG, "pick song: $song")
            if (song != null) {
                if (isMulti) {
                    MultiTrackerAudioEditorActivity.open(requireContext(), song)
                } else {
                    AudioCutActivity.open(requireContext(), song!!)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }


}