package com.san.audioeditor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.san.audioeditor.R
import com.san.audioeditor.activity.FAQActivity
import com.san.audioeditor.activity.SearchActivity
import com.san.audioeditor.activity.SettingsActivity
import com.san.audioeditor.databinding.FragmentIndexBinding
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.utils.ImmerseDesignPadding
import dev.android.player.framework.utils.PreferencesUtility
import dev.audio.timeruler.utils.AudioFileUtils


class IndexFragment : BaseFragment() {

    companion object {
        const val PAGE_COUNT = 2

        const val INDEX_FRAGMENT_CREATE = 0
        const val INDEX_FRAGMENT_OUTPUT = 1
    }


    private var _binding: FragmentIndexBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentIndexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreatedCompat(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedCompat(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.navView.itemIconTintList = null //设置ViewPager的滑动监听
        binding.viewPager.registerOnPageChangeCallback(OnPageChangeListener(binding.navView))
        binding.navView.setOnItemSelectedListener(OnSelectedListener(binding.viewPager))

        //init ViewPager
        //设置默认打开的页面
        binding.viewPager.adapter = ContentAdapter(this)
        val index = PreferencesUtility.getInstance(view.context).mainViewPagerIndex
        binding.viewPager.setCurrentItem(index, false) //沉浸式设计
        binding.root.ImmerseDesignPadding()
    }

    /**
     * @param expanded    true:展开，false:折叠
     * @param isAnimation true:展示动画
     */
    fun setAppbarExpanded(expanded: Boolean, isAnimation: Boolean) {
        binding.appbarLayout.setExpanded(expanded, isAnimation)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private var settingItemMenu: MenuItem? = null
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        settingItemMenu = menu.findItem(R.id.action_setting)
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_setting -> {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }

            R.id.action_search -> {
                SearchActivity.open(requireContext(), SearchActivity.FROM_HOME, AudioFileUtils.OUTPUT_FOLDER)
            }

            R.id.action_question -> FAQActivity.open(requireContext(), FAQActivity.OPEN_FROM_INDEX_HOME)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }


    /**
     * ViewPager的滑动监听
     */
    private inner class OnPageChangeListener(private val navView: BottomNavigationView) :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int,
                                    positionOffset: Float,
                                    positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            PreferencesUtility.getInstance(navView.context).mainViewPagerIndex = position
            navView.selectedItemId = when (position) {
                INDEX_FRAGMENT_CREATE -> R.id.navigation_create
                INDEX_FRAGMENT_OUTPUT -> R.id.navigation_output
                else -> R.id.navigation_create
            }
            if (position == 0) {
                val menu = navView.menu
                menu.findItem(R.id.navigation_create).setIcon(R.drawable.ic_tabbar_create_selected)
                menu.findItem(R.id.navigation_output).setIcon(R.drawable.ic_tabbar_outputs)
            } else {
                val menu = navView.menu
                menu.findItem(R.id.navigation_output).setIcon(R.drawable.ic_tabbar_outputs_selected)
                menu.findItem(R.id.navigation_create).setIcon(R.drawable.ic_tabbar_create)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    /**
     * BottomNavigationView的点击事件
     */
    private class OnSelectedListener(private val viewPager: ViewPager2) :
        BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            var value = "Bar_Home"

            when (item.itemId) {
                R.id.navigation_create -> {
                    if (viewPager.currentItem != INDEX_FRAGMENT_CREATE) {
                        viewPager.setCurrentItem(INDEX_FRAGMENT_CREATE, true) //todo
                        //                        TrackerMultiple.onEvent("Home", value)
                    }
                }

                R.id.navigation_output -> {
                    if (viewPager.currentItem != INDEX_FRAGMENT_OUTPUT) {
                        value = "Bar_Library"
                        viewPager.setCurrentItem(INDEX_FRAGMENT_OUTPUT, true) //todo
                        //                        TrackerMultiple.onEvent("Home", value)
                    }
                }

            }
            return true
        }
    }


    inner class ContentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = PAGE_COUNT

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CreateFragment()
                1 -> OutputFragment()
                else -> CreateFragment()
            }
        }
    }


    fun index2Fragment(index: Int) {
        if (index > 1 || index < 0) {
            return
        }
        binding.viewPager.currentItem = index
    }


}