package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import  ru.netology.nmedia.repository.*
import java.net.HttpURLConnection

class FeedFragment : Fragment() {

    private var isButtonClicked = false



    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener{

            override fun bigPhoto(url : String) {
                val args = Bundle()
                args.putString("image_url", url)
               findNavController().navigate(R.id.action_feedFragment_to_photoFragment, args)
            }
            override fun onEdit(post: Post) {
                viewModel.edit(post)

            }

            override fun onLike(post: Post) {
                viewModel.like(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        }, )
        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }

/**


 */
        val insertToTopListener = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                    // Дело сделано, удаляем слушатель до следующего клика, чтобы не было лишних скроллов
                    adapter.unregisterAdapterDataObserver(this)
                }
            }
        }

        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var previousScrollY = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentScrollY = recyclerView.computeVerticalScrollOffset()

                // Показываем кнопку только если прокрутка идёт вверх, и кнопка не была нажата
                if (!isButtonClicked && currentScrollY < previousScrollY && currentScrollY > 0) {
                    // Задержка поможет избежать моргания кнопки при быстрой прокрутке вниз
                    binding.scrollTop.postDelayed({
                        if (!isButtonClicked) {
                            binding.scrollTop.visibility = View.VISIBLE
                        }
                    }, 200)
                } else if (currentScrollY >= previousScrollY) {
                    // Скрываем кнопку при прокрутке вниз
                    binding.scrollTop.visibility = View.GONE
                }
                previousScrollY = currentScrollY
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // Если прокрутка остановилась и кнопка была нажата
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isButtonClicked) {
                    // Сбрасываем флаг, так как прокрутка завершена
                    isButtonClicked = false
                }
            }
        })


        binding.scrollTop.setOnClickListener{
            binding.list.smoothScrollToPosition(0)
            binding.scrollTop.visibility = View.GONE
            isButtonClicked = true

        }



        binding.buttonScroll.setOnClickListener {
            binding.list.smoothScrollToPosition(0)
            binding.buttonScroll.visibility = View.GONE
            binding.bell.visibility = View.GONE
            binding.countPost.visibility = View.GONE
            viewModel.showAll()

            // Ждём, когда разница между новым и старым списком рассчитается
            adapter.registerAdapterDataObserver(insertToTopListener)

        }

        /**
        следит за кол-вом СКРЫТЫХ постов ( указывая их кол-во)
         */
        viewModel.newerCount.observe(viewLifecycleOwner) { postCount ->
            if (postCount > 0) {
                binding.buttonScroll.visibility = View.VISIBLE
                binding.bell.visibility = View.VISIBLE
                binding.countPost.visibility = View.VISIBLE
                binding.countPost.text = postCount.toString()
            }
        }

        /**
        следит за добавлением ВСЕХ постов (КРОМЕ СКРЫТЫХ!!!)
         */
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
        }



        viewModel.dataState.observe(viewLifecycleOwner) { state ->

            if (state.errorMessage.isNotEmpty()) {
                Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                        "ok"
                    ) {
                        Toast.makeText(view?.context, "Кнопка нажата!", Toast.LENGTH_SHORT)
                            .show()
                    }.setAnchorView(binding.fab).show()

            }
            if (state.error) {
                Snackbar.make(binding.root, "ошибка сети", Snackbar.LENGTH_LONG)
                    .setAction("retry") {
                        viewModel.loadPosts()
                    }.setAnchorView(binding.fab).show()
            }
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.loading


        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root

    }

}
