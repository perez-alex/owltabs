package com.aperez.owltabs.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.aperez.owltabs.R
import com.aperez.owltabs.model.Fixture
import com.aperez.owltabs.model.PlayStatus
import com.aperez.owltabs.model.State
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class FixturesAdapter(
    private val context: Context,
    val fixtures: List<Fixture>,
    val title: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataWithSections: ArrayList<DataHolder> = ArrayList()
    private val sectionView = 1
    private val fixtureView = 2

    private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
    private val dateOnlyFormatter = SimpleDateFormat("d", Locale.getDefault())
    private val sectionFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    init {
        //set timezone, this allow the time to be shown on the user's timezone
        dateParser.timeZone = TimeZone.getTimeZone("Zulu")

        createSections(fixtures)
    }

    private fun createSections(fixtures: List<Fixture>) {
        //Make sure list is sorted by date
        val sorted = fixtures.sortedWith(Comparator { p0, p1 -> return@Comparator p0?.date?.compareTo(p1?.date!!)!! })

        //Add items to identify different sections
        val first = sorted[0]
        var section = first.date.substring(0, 7)
        dataWithSections.add(DataHolder(null, first.date))
        for (fixture in sorted) {
            val thisSection = fixture.date.substring(0, 7)
            if (section != thisSection) {
                section = thisSection
                dataWithSections.add(DataHolder(null, fixture.date))
            }
            dataWithSections.add(DataHolder(fixture, null))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == fixtureView) {
            val view = inflater.inflate(R.layout.item_fixture, parent, false)
            FixtureViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_section, parent, false)
            SectionViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return dataWithSections.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataHolder = dataWithSections[position]
        if (dataHolder.fixture != null) {
            val fixture = dataHolder.fixture
            holder as FixtureViewHolder
            holder.competition.text = fixture.competitionStage.competition.name
            val fixtureDate = dateParser.parse(fixture.date)
            setBaseView(holder, fixtureDate, fixture)
            val formattedDate = dateFormatter.format(fixtureDate)
            if (fixture.state != State.postponed) {
                holder.venueDate.text = context.getString(R.string.venue_date_format, fixture.venue.name, formattedDate)
                holder.postponedBadge.visibility = GONE
            } else {
                holder.venueDate.text = HtmlCompat.fromHtml(
                    context.getString(R.string.venue_date_format_postponed, fixture.venue.name, formattedDate)
                    , HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                holder.postponedBadge.visibility = VISIBLE
            }
            holder.homeTeam.text = fixture.homeTeam.name
            holder.awayTeam.text = fixture.awayTeam.name

        } else {
            holder as SectionViewHolder
            val sectionDate = dateParser.parse(dataHolder.sectionDate)
            holder.name.text = sectionFormatter.format(sectionDate)
        }
    }

    private fun setBaseView(holder: RecyclerView.ViewHolder, fixtureDate: Date, fixture: Fixture) {
        holder as FixtureViewHolder
        if (title == context.getString(R.string.fixtures)) {
            holder.awayScore.visibility = GONE
            holder.homeScore.visibility = GONE
            holder.dayOfWeek.visibility = VISIBLE
            holder.date.visibility = VISIBLE
            holder.dateDivider.visibility = VISIBLE

            holder.date.text = dateOnlyFormatter.format(fixtureDate)
            holder.dayOfWeek.text = dayFormatter.format(fixtureDate)
        } else {
            holder.awayScore.visibility = VISIBLE
            holder.homeScore.visibility = VISIBLE
            holder.dayOfWeek.visibility = GONE
            holder.date.visibility = GONE
            holder.dateDivider.visibility = GONE

            when {
                fixture.score?.winner == PlayStatus.home -> {
                    holder.homeScore.setTextColor(
                        ResourcesCompat.getColor(
                            context.resources,
                            android.R.color.holo_blue_dark,
                            null
                        )
                    )
                    holder.awayScore.setTextColor(ResourcesCompat.getColor(context.resources, R.color.navyBlue, null))
                }
                fixture.score?.winner == PlayStatus.away -> {
                    holder.awayScore.setTextColor(
                        ResourcesCompat.getColor(
                            context.resources,
                            android.R.color.holo_blue_dark,
                            null
                        )
                    )
                    holder.homeScore.setTextColor(ResourcesCompat.getColor(context.resources, R.color.navyBlue, null))
                }
                else -> {
                    holder.awayScore.setTextColor(ResourcesCompat.getColor(context.resources, R.color.navyBlue, null))
                    holder.homeScore.setTextColor(ResourcesCompat.getColor(context.resources, R.color.navyBlue, null))
                }
            }
            holder.homeScore.text = fixture.score?.home.toString()
            holder.awayScore.text = fixture.score?.away.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataWithSections[position].sectionDate == null) {
            fixtureView
        } else {
            sectionView
        }

    }

    fun setSelectedCompetition(competition: String) {
        dataWithSections.clear()
        val filtered = if (competition == context.getString(R.string.all)) {
            fixtures
        } else {
            fixtures.filter { it.competitionStage.competition.name == competition }
        }
        createSections(filtered)
        notifyDataSetChanged()
    }

    inner class FixtureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.competition)
        lateinit var competition: TextView
        @BindView(R.id.venue_and_date)
        @Nullable
        lateinit var venueDate: TextView
        @BindView(R.id.home_team)
        lateinit var homeTeam: TextView
        @BindView(R.id.away_team)
        lateinit var awayTeam: TextView
        @BindView(R.id.date)
        lateinit var date: TextView
        @BindView(R.id.day_of_the_week)
        lateinit var dayOfWeek: TextView
        @BindView(R.id.postponed_badge)
        lateinit var postponedBadge: View
        @BindView(R.id.home_score)
        lateinit var homeScore: TextView
        @BindView(R.id.away_score)
        lateinit var awayScore: TextView
        @BindView(R.id.date_divider)
        lateinit var dateDivider: View

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.section_name)
        @Nullable
        lateinit var name: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    inner class DataHolder(
        val fixture: Fixture?,
        val sectionDate: String?
    )
}