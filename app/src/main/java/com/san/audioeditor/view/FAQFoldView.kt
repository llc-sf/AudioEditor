package com.san.audioeditor.view
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.san.audioeditor.R
import com.san.audioeditor.config.AnswerViewProvider
import com.san.audioeditor.config.FAQHelper
import com.san.audioeditor.databinding.FaqFoldViewBinding
import dev.android.player.framework.data.model.FAQItemData
import dev.android.player.framework.utils.TrackerMultiple
import dev.android.player.framework.utils.ViewUtils
import dev.android.player.framework.utils.dimen


class FAQFoldView @JvmOverloads constructor(
        context: Context, attributeSet: AttributeSet? = null
) : LinearLayout(context, attributeSet) {

    private val faqAnswerView: FaqAnswer by lazy {
        createAnswer()
    }
    private var binding: FaqFoldViewBinding
    private var isFold = true //是否折叠(默认折叠状态)。true为展开，false为折叠
    private var faqItemData: FAQItemData? = null
    var foldCallback: ((itemData: FAQItemData?, isFold: Boolean) -> Unit)? = null

    init {
        binding = FaqFoldViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding.faqImgFold.setImageResource(R.drawable.ic_faq_down)
        binding.faqFoldLayout.setOnClickListener { changeFoldState() }
        updateQuestionStyle(true)

    }

    private fun changeFoldState() {
        if (isFold) {
            //每次展开Answer前，关闭其他Answer
            foldOthers()
            unFoldAnswer()
        } else {
            foldAnswer()
        }
        foldCallback?.invoke(faqItemData, isFold)
    }

    private fun foldOthers() {
        for (faqView in (this.parent as ViewGroup).children) {
            if (faqView is FAQFoldView && !faqView.isFold) {
                faqView.foldAnswer()
            }
        }
    }

    fun setData(faq: FAQItemData, dataCount: Int) {
        faqItemData = faq
        binding.faqFoldIcon.setImageResource(faq.iconId)
        binding.faqQuestionTitle.text = faq.question
        this.tag = faq.iconId
    }

    /**
     * 展开Answer布局
     */
    private fun unFoldAnswer() {
        isFold = false
        checkUnFoldView()
        updateQuestionStyle(false)
        val allSize = ViewUtils.onMeasureAtMost(binding.faqAnswer)
        doViewHeightAnimation(binding.faqAnswer, 1, allSize[1]) {
            // 确保展开动效结束后 布局完整
            binding.faqAnswer.updateLayoutParams<ViewGroup.LayoutParams> {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            // 展开动效结束后，微调当前展开 item 位置，使其尽量可见
            val scrollView = findScrollViewParent(this@FAQFoldView) ?: return@doViewHeightAnimation
            if (isFullDisplay(scrollView, binding.root)) return@doViewHeightAnimation
            val scrollValue = getViewScreenTop(binding.root) - getViewScreenTop(scrollView)
            scrollView.smoothScrollBy(0, scrollValue)
        }
        binding.faqImgFold.setImageResource(R.drawable.ic_faq_up)
        TrackerMultiple.onEvent("FAQ", "Open_${FAQHelper.getFaqNumber(context, faqItemData)}")
    }

    private fun updateQuestionStyle(isFold: Boolean) {
        binding.faqQuestionTitle.run {
            if (isFold) {
                binding.clContainer.background = null
                binding.faqFoldLayout.setPadding(0, dimen(R.dimen.dp_10), 0, dimen(R.dimen.dp_10))
                paint.isFakeBoldText = false
                typeface = Typeface.DEFAULT
                binding.vDivider.visibility = VISIBLE
            } else {
                binding.clContainer.setBackgroundResource(R.drawable.bg_faq_item)
                binding.faqFoldLayout.setPadding(0, dimen(R.dimen.dp_13), 0, dimen(R.dimen.dp_13))
                paint.isFakeBoldText = true
                typeface = Typeface.DEFAULT_BOLD
                binding.vDivider.visibility = GONE
            }
        }
    }

    private fun checkUnFoldView() {
        if (!binding.faqAnswer.contains(faqAnswerView)) {
            // 设置 faqAnswer 的初始高度（避免初始跳变）
            binding.faqAnswer.updateLayoutParams<ViewGroup.LayoutParams> {
                height = 1
            }
            binding.faqAnswer.addView(faqAnswerView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun isFullDisplay(scrollView: NestedScrollView, itemView: View): Boolean {
        val itemViewTop = getViewScreenTop(itemView)
        val itemViewBottom = itemViewTop + itemView.height
        val scrollViewTop = getViewScreenTop(scrollView)
        val scrollViewBottom = scrollViewTop + scrollView.height
        return itemViewTop >= scrollViewTop && itemViewBottom <= scrollViewBottom
    }

    /**
     * 折叠Answer布局
     */
    private fun foldAnswer() {
        isFold = true
        updateQuestionStyle(true)
        val allSize = ViewUtils.onMeasureAtMost(binding.faqAnswer)
        doViewHeightAnimation(binding.faqAnswer, allSize[1], 1)
        binding.faqImgFold.setImageResource(R.drawable.ic_faq_down)
    }

    /**
     * 执行高度动画
     */
    private fun doViewHeightAnimation(
            view: ViewGroup,
            start: Int,
            end: Int,
            endListener: (() -> Unit)? = null
    ) {
        ValueAnimator.ofInt(start, end).run {
            addUpdateListener { animation ->
                view.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = animation.animatedValue as Int
                }
            }
            duration = 200  // 设置动画时长，200毫秒
            doOnEnd {
                endListener?.invoke()
            }
            start()
        }
    }

    private fun getViewScreenTop(view: View): Int {
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        return viewLocation[1]
    }

    /**
     * 根据icon的id创建AnswerView
     */
    private fun createAnswer() = AnswerViewProvider().getAnswerView(context, faqItemData) { contentClick(it) }

    private fun contentClick(it: FAQItemData?) {
        it?.run {
            if(it.iconId == R.drawable.ic_faq_repeat) {
                // 关闭此问题，然后展开 R.drawable.ic_faq_songlist 问题
                val parent = this@FAQFoldView.parent as? View ?: return
                val targetView = parent.findViewWithTag<FAQFoldView>(R.drawable.ic_faq_songlist) ?: return
                targetView.foldOthers()
                targetView.unFoldAnswer()
            }
        }
    }

    private fun findScrollViewParent(view: View): NestedScrollView? {
        // 获取 View 的父类
        val parent = view.parent as? View ?: return null
        // 如果父类是 ScrollView，则返回该 ScrollView
        if (parent is NestedScrollView) {
            return parent
        }
        // 如果父类不是 ScrollView，则继续递归查找其父类
        return findScrollViewParent(parent)
    }
}

