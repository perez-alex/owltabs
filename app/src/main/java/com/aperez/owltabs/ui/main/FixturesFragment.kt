package com.aperez.owltabs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.aperez.owltabs.R
import com.aperez.owltabs.ui.main.adapter.FixturesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FixturesFragment : Fragment(), FilterDialog.CompetitionClickedListener {
    @BindView(R.id.fixtures_list)
    lateinit var fixturesList: RecyclerView
    @BindView(R.id.progress_view)
    lateinit var progressView: View
    @BindView(R.id.filter_button)
    lateinit var filterButton: View

    private lateinit var pageViewModel: PageViewModel
    private var listAdapter: FixturesAdapter? = null
    lateinit var competitions: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java)
    }

    @OnClick(R.id.filter_button)
    fun onFilterClicked() {
        val dialog = FilterDialog(competitions.keys.toList(), this)
        dialog.show(fragmentManager, "FILTER")
    }

    override fun onCompetitionClicked(competition: String) {
        listAdapter?.setSelectedCompetition(competition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_fixtures, container, false)
        ButterKnife.bind(this, root)
        pageViewModel.fixturesView().observe(this, Observer { response ->
            when (response) {
                is PageViewModel.FixturesListView.Loading -> {
                    progressView.visibility = VISIBLE
                    filterButton.visibility = GONE
                }
                is PageViewModel.FixturesListView.Success -> {
                    listAdapter = FixturesAdapter(context!!, response.fixtures, arguments?.getString(ARG_SECTION_TITLE))
                    fixturesList.adapter = listAdapter
                    progressView.visibility = GONE
                    competitions = HashMap()
                    for (fixture in response.fixtures) {
                        competitions[fixture.competitionStage.competition.name] = "1"
                    }
                    filterButton.visibility = VISIBLE
                }
                is PageViewModel.FixturesListView.Error -> {
                    progressView.visibility = GONE
                    filterButton.visibility = GONE
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        pageViewModel.loadData(this.context!!, arguments?.getString(ARG_SECTION_TITLE))
        return root
    }

    companion object {
        private const val ARG_SECTION_TITLE = "section_title"

        @JvmStatic
        fun newInstance(title: String): FixturesFragment {
            return FixturesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SECTION_TITLE, title)
                }
            }
        }
    }
}

class FilterDialog(private val competitionsList: List<String>, val listener: CompetitionClickedListener) :
    BottomSheetDialogFragment() {

    @BindView(R.id.competitions_list)
    lateinit var competitions: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_filter, container, false)
        ButterKnife.bind(this, view)

        competitions.adapter = CompetitionsAdapter(competitionsList)
        return view
    }

    inner class CompetitionsAdapter(private val competitions: List<String>) :
        RecyclerView.Adapter<CompetitionsAdapter.CompetitionViewHolder>() {
        override fun getItemCount(): Int {
            return competitions.size + 1
        }

        override fun onBindViewHolder(holder: CompetitionViewHolder, position: Int) {
            if (position == 0) {
                holder.competition.text = getString(R.string.all)
            } else {
                holder.competition.text = competitions[position - 1]
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompetitionViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            val view = inflater.inflate(R.layout.item_competition, parent, false)
            return CompetitionViewHolder(view)
        }

        inner class CompetitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @BindView(R.id.competition)
            lateinit var competition: TextView

            init {
                ButterKnife.bind(this, itemView)
                itemView.setOnClickListener {
                    if (adapterPosition == 0) {
                        listener.onCompetitionClicked(getString(R.string.all))
                    } else {
                        listener.onCompetitionClicked(competitions[adapterPosition - 1])
                    }
                    dismiss()
                }
            }
        }
    }

    interface CompetitionClickedListener {
        fun onCompetitionClicked(competition: String)
    }
}