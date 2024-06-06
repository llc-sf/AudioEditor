package com.san.audioeditor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.san.audioeditor.R
import com.san.audioeditor.databinding.FragmentFaqBinding
import com.san.audioeditor.view.FAQFoldView
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.base.viewBinding
import dev.android.player.framework.data.model.FAQItemData

class FAQFragment : BaseFragment() {

    val viewBinding by viewBinding(FragmentFaqBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = viewBinding.root

    override fun onLazyLoad() {
        addFAQItemViews()
    }

    private fun addFAQItemViews() {
        //FAQ初始化
        for (view in initItemViews()) {
            viewBinding.llQuestionContainer.addView(view)
        }
    }

    private fun initItemViews(): ArrayList<FAQFoldView> {
        val questions = resources.getStringArray(R.array.faq_questions)
        val questionIcons = resources.obtainTypedArray(R.array.faq_icons)
        val questionCount = questions.size
        val faqFoldViewList = ArrayList<FAQFoldView>()
        for (i in 0 until questionCount) {
            val itemData = FAQItemData(i, questionIcons.getResourceId(i, R.drawable.ic_faq_download), questions[i])
            val faqFoldView = FAQFoldView(requireContext()).apply {
                setData(itemData, questionCount)
            }
            faqFoldViewList.add(faqFoldView)
        }
        questionIcons.recycle()
        return faqFoldViewList
    }
}