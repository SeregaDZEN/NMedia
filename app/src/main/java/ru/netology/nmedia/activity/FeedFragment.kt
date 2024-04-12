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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var isButtonClicked = false


    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(
            object : OnInteractionListener {

                override fun bigPhoto(url: String) {
                    val args = Bundle()
                    args.putString("image_url", url)
                    findNavController().navigate(R.id.action_feedFragment_to_photoFragment, args)
                }

                override fun onEdit(post: Post) {

                    findNavController()
                        .navigate(
                            R.id.action_feedFragment_to_newPostFragment,
                            Bundle().apply {
                                putString("EXTRA_CONTENT", post.content)
                            }
                        )
                    postViewModel.edit(post)
                }

                override fun onLike(post: Post) {

                    if (authViewModel.authenticated) {
                        postViewModel.like(post)
                    } else {
                        MaterialAlertDialogBuilder(requireActivity()).apply {
                            setTitle(R.string.authentication)
                            setMessage(R.string.authenticate_now)
                            setPositiveButton(R.string.yes) { _, _ ->
                                findNavController().navigate(R.id.authFragment)
                            }
                            setNegativeButton(R.string.no) { _, _ -> }
                            setCancelable(true)
                        }.create().show()

                    }
                }


                override fun onRemove(post: Post) {
                    postViewModel.removeById(post.authorId)
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
            },
        )
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() }
        )



        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        /**


         */

        /*       val insertToTopListener = object : RecyclerView.AdapterDataObserver() {
                   override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                       if (positionStart == 0) {
                           binding.list.smoothScrollToPosition(0)
                           // Дело сделано, удаляем слушатель до следующего клика, чтобы не было лишних скроллов
                           adapter.unregisterAdapterDataObserver(this)
                       }
                   }
               }
       */
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


        binding.scrollTop.setOnClickListener {
            binding.list.smoothScrollToPosition(0)
            binding.scrollTop.visibility = View.GONE
            isButtonClicked = true

        }



        binding.buttonScroll.setOnClickListener {
            binding.list.smoothScrollToPosition(0)
            binding.buttonScroll.visibility = View.GONE
            binding.bell.visibility = View.GONE
            binding.countPost.visibility = View.GONE
            postViewModel.showAll()

            // Ждём, когда разница между новым и старым списком рассчитается
            //adapter.registerAdapterDataObserver(insertToTopListener)

        }//AppInspector давно не работает писал об этом  2 раза игнор  Андроид студио нужно обновить, это проблема совместимости жирафа с 34 апи, +Там либо апи понижать, либо библиотеку какую-то, но не помню точно какую+


        lifecycleScope.launchWhenCreated {
            postViewModel.newerCount.collectLatest { postCount ->
                if (postCount > 0) {
                    binding.buttonScroll.visibility = View.VISIBLE
                    binding.bell.visibility = View.VISIBLE
                    binding.countPost.visibility = View.VISIBLE
                    binding.countPost.text = postCount.toString()
                }

            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.data.collectLatest { adapter.refresh() }
            }
        }



        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }



        postViewModel.dataState.observe(viewLifecycleOwner) { state ->

            if (state.errorMessage.isNotEmpty()) {
                Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                        "ok"
                    ) {
                        Toast.makeText(view?.context, R.string.Button_pressed, Toast.LENGTH_SHORT)
                            .show()
                    }.setAnchorView(binding.fab).show()

            }
            if (state.error) {
                Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        adapter.refresh()
                    }.setAnchorView(binding.fab).show()
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing =
                    it.refresh is LoadState.Loading
            }
        }

        binding.fab.setOnClickListener {
            if (authViewModel.authenticated) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                MaterialAlertDialogBuilder(requireActivity()).apply {
                    setTitle(R.string.authentication)
                    setMessage(R.string.authenticate_now)
                    setPositiveButton(R.string.yes) { _, _ ->
                        findNavController().navigate(R.id.authFragment)
                    }
                    setNegativeButton(R.string.no) { _, _ -> }
                    setCancelable(true)
                }.create().show()
            }
        }

        return binding.root

    }


}
